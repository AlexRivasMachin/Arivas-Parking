package com.lksnext.arivas.view.fragment.reservas;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lksnext.arivas.R;
import com.lksnext.arivas.domain.ReservationWorker;
import com.lksnext.arivas.utils.FutureDateValidator;
import com.lksnext.arivas.adapters.PlazaAdapter;
import com.lksnext.arivas.domain.Reservation;
import com.lksnext.arivas.viewmodel.reservas.ReservasViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RealizarReservaFragment extends Fragment {

    private NavController navController;
    private ReservasViewModel mViewModel;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private PlazaAdapter adapter;
    private List<String> dataSet;
    private FirebaseFirestore firestore;
    private  String selectedChipType;
    private String selectedChip;
    private ProgressBar reservationProgressBar;



    public static RealizarReservaFragment newInstance() {
        return new RealizarReservaFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_realizar_reserva, container, false);
        firestore = FirebaseFirestore.getInstance();

        ChipGroup chipGroup = rootView.findViewById(R.id.chipGroup);
        reservationProgressBar = rootView.findViewById(R.id.reservationProgressIndicator);
        reservationProgressBar.setVisibility(View.VISIBLE);

        chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if(checkedIds.size() == 0){
                    rootView.findViewById(R.id.STD);
                }else {
                    int chipId = group.getCheckedChipId();
                    Chip selectedChip = rootView.findViewById(chipId);
                    selectedChipType = getCHipType((String) selectedChip.getText());
                    updateAdapterWithSelectedChipType();
                    updateRecyclerView(rootView, selectedChipType);
                }
            }
        });
        EditText etDate = rootView.findViewById(R.id.et_date);
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDatePicker(etDate);
            }
        });
        EditText etTimeEntry = rootView.findViewById(R.id.et_time_entry);
        etTimeEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTimePicker(etTimeEntry, null);
                setProgress(rootView);
            }
        });
        EditText etTimeExit = rootView.findViewById(R.id.et_time_exit);
        etTimeExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTimePicker(etTimeExit, etTimeEntry);
                setProgress(rootView);
            }
        });

        Button continuarReservaButton = rootView.findViewById(R.id.btnContinuar);
        continuarReservaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (areAllFieldsCompleted(rootView)) {
                    createDialog(rootView);
                } else {
                    Toast.makeText(getContext(), "Por favor, complete todos los campos.", Toast.LENGTH_LONG).show();
                    setProgress(rootView);
                }
            }
        });

        updateRecyclerView(rootView, "STD");

        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                setProgress(rootView);
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                setProgress(rootView);
            }
        });

        Button infoButton = rootView.findViewById(R.id.info_button);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog();
            }
        });

        return rootView;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    private void showInfoDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Información de Reservas")
                .setIcon(R.drawable.info)
                .setMessage("Las reservas solo se pueden realizar de 6:00 a 23:00 siendo la duración máxima de una reserva de 9 horas. \n\n" +
                        "Recuerda que solo se pueden realizar reservas con una antelación máxima de 7 días. \n\n")
                .setPositiveButton("Entendido", null)
                .show();
    }

    public void setProgress(View rootView) {
        int progress = 0;

        EditText etDate = rootView.findViewById(R.id.et_date);
        EditText etTimeEntry = rootView.findViewById(R.id.et_time_entry);
        EditText etTimeExit = rootView.findViewById(R.id.et_time_exit);
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerViewChips);
        PlazaAdapter adapter = (PlazaAdapter) recyclerView.getAdapter();

        boolean isAnyRecyclerViewChipSelected = adapter != null && adapter.isItemSelected();

        if (isAnyRecyclerViewChipSelected) {
            progress += 25;
        }
        if (!etDate.getText().toString().isEmpty()) {
            progress += 25;
        }
        if (!etTimeEntry.getText().toString().isEmpty()) {
            progress += 25;
        }
        if (!etTimeExit.getText().toString().isEmpty()) {
            progress += 25;
        }
        System.out.println(progress);
        ObjectAnimator animation = ObjectAnimator.ofInt(reservationProgressBar, "progress", reservationProgressBar.getProgress(), progress);
        animation.setDuration(500); // Duration of animation in milliseconds
        animation.start();
    }


    public void createDatePicker(EditText etDate) {
        long today = MaterialDatePicker.todayInUtcMilliseconds();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(new FutureDateValidator());
        Calendar futureMonth = Calendar.getInstance();

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Selecciona la fecha");
        builder.setSelection(today);
        builder.setCalendarConstraints(constraintsBuilder.build());

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);

                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH) + 1;
                int year = calendar.get(Calendar.YEAR);
                String selectedDate = day + "/" + month + "/" + year;

                Toast.makeText(getContext(), "Fecha seleccionada: " + selectedDate, Toast.LENGTH_LONG).show();
                etDate.setText(selectedDate);
                setProgress(getView());
            }
        });

        materialDatePicker.show(getChildFragmentManager(), "MATERIAL_DATE_PICKER");
    }


    public void createTimePicker(EditText etTime, @Nullable EditText etTimeEntry) {
        int hour = 6;
        int minute = 0;

        MaterialTimePicker.Builder timePickerBuilder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Selecciona la hora");

        MaterialTimePicker timePicker = timePickerBuilder.build();

        timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hourOfDay = timePicker.getHour();
                int minute = timePicker.getMinute();

                if (minute >= 30) {
                    minute = 30;
                } else {
                    minute = 0;
                }

                if (hourOfDay < 6) {
                    hourOfDay = 6;
                } else if (hourOfDay > 23) {
                    hourOfDay = 23;
                }

                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);

                if (etTimeEntry != null) {
                    String entryTime = etTimeEntry.getText().toString();
                    if (!entryTime.isEmpty()) {
                        String[] parts = entryTime.split(":");
                        int entryHour = Integer.parseInt(parts[0]);
                        int entryMinute = Integer.parseInt(parts[1]);

                        Calendar calendarEntry = Calendar.getInstance();
                        calendarEntry.set(Calendar.HOUR_OF_DAY, entryHour);
                        calendarEntry.set(Calendar.MINUTE, entryMinute);

                        Calendar calendarExit = Calendar.getInstance();
                        calendarExit.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendarExit.set(Calendar.MINUTE, minute);

                        long difference = calendarExit.getTimeInMillis() - calendarEntry.getTimeInMillis();
                        if (difference < 0) {
                            Toast.makeText(RealizarReservaFragment.this.getContext(), "La hora de salida no puede ser anterior a la de entrada", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (difference > 8 * 60 * 60 * 1000) {
                            calendarExit.setTimeInMillis(calendarEntry.getTimeInMillis() + 9 * 60 * 60 * 1000);
                            hourOfDay = calendarExit.get(Calendar.HOUR_OF_DAY);
                            minute = calendarExit.get(Calendar.MINUTE);
                            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        }
                    }
                }

                etTime.setText(selectedTime);
                Toast.makeText(RealizarReservaFragment.this.getContext(), "Hora seleccionada: " + selectedTime, Toast.LENGTH_SHORT).show();
            }
        });

        timePicker.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    private void updateAdapterWithSelectedChipType() {
        if (adapter != null) {
            adapter.setChipType(selectedChipType);
            adapter.notifyDataSetChanged();
            setProgress(getView());
        }
    }

    public  void updateRecyclerView(View rootView, String selectedChipType){
        recyclerView = rootView.findViewById(R.id.recyclerViewChips);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        dataSet = new ArrayList<String>();
        adapter = new PlazaAdapter(dataSet, getContext(), selectedChipType);
        recyclerView.setAdapter(adapter);

        firestore.collection("parking_slots")
                .whereEqualTo("type", selectedChipType)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String numeroPlaza = id.replaceAll("[^0-9]","");
                            dataSet.add(numeroPlaza);
                            Collections.sort(dataSet);
                        }
                        setProgress(getView());
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Error obteniendo datos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean areAllFieldsCompleted(View rootView) {
        ChipGroup chipGroup = rootView.findViewById(R.id.chipGroup);
        EditText etDate = rootView.findViewById(R.id.et_date);
        EditText etTimeEntry = rootView.findViewById(R.id.et_time_entry);
        EditText etTimeExit = rootView.findViewById(R.id.et_time_exit);

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerViewChips);
        PlazaAdapter adapter = (PlazaAdapter) recyclerView.getAdapter();
        boolean isAnyRecyclerViewChipSelected = false;

        for (int i = 0; i < adapter.getItemCount(); i++) {
            PlazaAdapter.PlazaViewHolder holder = (PlazaAdapter.PlazaViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null && adapter.isItemSelected() ) {
                selectedChip = adapter.getSelectedItem();
                isAnyRecyclerViewChipSelected = true;
                break;
            }
        }

        if (!isAnyRecyclerViewChipSelected) {
            return false;
        }
        if (chipGroup.getCheckedChipId() == View.NO_ID) {
            return false;
        }
        if (etDate.getText().toString().isEmpty()) {
            return false;
        }
        if (etTimeEntry.getText().toString().isEmpty()) {
            return false;
        }
        if (etTimeExit.getText().toString().isEmpty()) {
            return false;
        }

        return true;
    }
    public void addReservation(){
        View rootView = getView();
        EditText etDate = rootView.findViewById(R.id.et_date);
        EditText etTimeEntry = rootView.findViewById(R.id.et_time_entry);
        EditText etTimeExit = rootView.findViewById(R.id.et_time_exit);

        Reservation reservation = new Reservation(FirebaseAuth.getInstance().getCurrentUser().getUid(), selectedChip, etDate.getText().toString(), etTimeEntry.getText().toString(), etTimeExit.getText().toString(), selectedChipType);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservations")
                .add(reservation)
                .addOnSuccessListener(documentReference -> {
                    scheduleNotification(etDate.getText().toString(), etTimeEntry.getText().toString(), "Reservation Reminder", "Tu reserva comenzará en breves momentos");
                })
                .addOnFailureListener(e -> Log.w("TAG", "Error adding document", e));
    }

    private void scheduleNotification(String date, String time, String title, String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date reservationDate = dateFormat.parse(date + " " + time);
            if (reservationDate != null) {
                long notificationTime = reservationDate.getTime() - (60 * 60 * 1000); // Una hora antes

                Data data = new Data.Builder()
                        .putString("title", title)
                        .putString("message", message)
                        .build();

                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReservationWorker.class)
                        .setInitialDelay(notificationTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .build();

                WorkManager.getInstance(requireContext()).enqueue(Collections.singletonList(workRequest));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }



    public String getCHipType(String chipText){
        if (Objects.equals(chipText, "Automovil")){
            return "STD";
        } else if (Objects.equals(chipText, "Motocicleta")) {
            return "MOTO";
        } else if (Objects.equals(chipText, "Estación de carga")) {
            return "ELEC";
        } else if (Objects.equals(chipText, "Movilidad reducida")) {
            return "DISC";
        }else {
            return "STD";
        }
    }
    public void createDialog(View rootView) {
        EditText etDate = rootView.findViewById(R.id.et_date);
        EditText etTimeEntry = rootView.findViewById(R.id.et_time_entry);
        EditText etTimeExit = rootView.findViewById(R.id.et_time_exit);

        new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.bookmark_add)
                .setTitle("Confirmar reserva")
                .setMessage("¿Quieres confirmar tu reserva de tipo " + selectedChipType + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addReservation();
                        Bundle bundle = new Bundle();
                        bundle.putString("chipType", selectedChipType);
                        bundle.putString("chip", selectedChip);
                        bundle.putString("date", etDate.getText().toString());
                        bundle.putString("entry", etTimeEntry.getText().toString());
                        bundle.putString("exit", etTimeExit.getText().toString());

                        navController.navigate(R.id.confirmarReservaFragment, bundle);
                    }
                })
                .show();
    }

}

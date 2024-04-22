package com.lksnext.arivas.view.fragment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lksnext.arivas.R;
import com.lksnext.arivas.viewmodel.ReservasViewModel;

public class ReservasFragment extends Fragment {

    private NavController navController;
    private ReservasViewModel mViewModel;

    public static ReservasFragment newInstance() {
        return new ReservasFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_reservas, container, false);

        // Obtén una referencia al botón en tu diseño
        Button realizarReservaButton = rootView.findViewById(R.id.realizarReserva);

        // Configura un OnClickListener para el botón
        realizarReservaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navega al fragmento fragmentRealizarReserva
                NavController navController = NavHostFragment.findNavController(ReservasFragment.this);
                navController.navigate(R.id.realizarReservaFragment);
            }
        });

        return rootView;
    }
}
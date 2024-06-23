package com.lksnext.arivas.domain;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.lksnext.arivas.R;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private List<Integer> dataSet;


    public static class CardViewHolder extends RecyclerView.ViewHolder {
        public TextView numReserva, fechaReserva, horaReserva, estadoReserva;
        public MaterialButton verReservaButton;

        public CardViewHolder(View view) {
            super(view);
            numReserva = view.findViewById(R.id.NumReserva);
            fechaReserva = view.findViewById(R.id.fechaReserva);
            horaReserva = view.findViewById(R.id.horaReserva);
            estadoReserva = view.findViewById(R.id.estadoReserva);
            verReservaButton = view.findViewById(R.id.outlinedButton2);
        }
    }

    public CardAdapter(List<Integer> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reserva, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        holder.numReserva.setText(String.valueOf(dataSet.get(position)));
        // Configura los demás TextViews y el botón aquí si es necesario
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
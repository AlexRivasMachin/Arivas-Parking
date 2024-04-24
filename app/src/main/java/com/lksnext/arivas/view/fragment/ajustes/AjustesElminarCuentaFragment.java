package com.lksnext.arivas.view.fragment.ajustes;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lksnext.arivas.R;
import com.lksnext.arivas.viewmodel.ajustes.AjustesElminarCuentaViewModel;

import java.util.Objects;

public class AjustesElminarCuentaFragment extends Fragment {

    private NavController navController;

    private AjustesElminarCuentaViewModel mViewModel;

    public static AjustesElminarCuentaFragment newInstance() {
        return new AjustesElminarCuentaFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajustes_elminar_cuenta, container, false);

        ImageView volverAjustesEliminarCuentaImage = view.findViewById(R.id.volverAjustesEliminarCuentaImage);

        volverAjustesEliminarCuentaImage.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(AjustesElminarCuentaFragment.this);
            navController.navigate(R.id.action_ajustesFragment_to_ajustesEliminarCuentaFragment);
        });

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener el NavController
        navController = Navigation.findNavController(view);

        // Configurar OnClickListener para la imagen de volver
        view.findViewById(R.id.volverAjustesEliminarCuentaImage).setOnClickListener(v -> navController.popBackStack(R.id.ajustesFragment, false));
    }
}

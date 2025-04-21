package com.example.minesweeper;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ExitGameDialogFragment extends DialogFragment {
    private ExitGameListener listener;

    public interface ExitGameListener {
        void onGoHome();
        void onQuitGame();
    }

    public void setListener(ExitGameListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_exit_game, container, false);

        Button btnHome = view.findViewById(R.id.btnHome);
        Button btnQuit = view.findViewById(R.id.btnQuit);
        ImageView ivClose = view.findViewById(R.id.ivClose);

        btnHome.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoHome();
            }
            dismiss();
        });

        btnQuit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuitGame();
            }
            dismiss();
        });

        ivClose.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
} 
package com.example.minesweeper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class GameOverDialogFragment extends DialogFragment
{
    private final boolean isWon;
    private final int timeSeconds;
    private final int highScore;
    private GameOverListener listener;

    public interface GameOverListener
    {
        void onPlayAgain();

        void onReturnHome();
    }

    public GameOverDialogFragment(boolean isWon, int timeSeconds, int highScore)
    {
        this.isWon = isWon;
        this.timeSeconds = timeSeconds;
        this.highScore = highScore;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        listener = (GameOverListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_game_over, null);

        TextView emojiResult = view.findViewById(R.id.emojiResult);
        TextView tvResult = view.findViewById(R.id.tvGameResult);
        TextView tvTime = view.findViewById(R.id.tvGameTime);
        TextView tvHighScore = view.findViewById(R.id.tvHighScore);

        if (isWon)
        {
            emojiResult.setText("🏆");
            tvResult.setText(getString(R.string.you_win));
            tvResult.setTextColor(Color.parseColor("#00E676"));
        }
        else
        {
            emojiResult.setText("💣");
            tvResult.setText(getString(R.string.game_over));
            tvResult.setTextColor(Color.parseColor("#F44336"));
        }
        tvTime.setText(String.format(getString(R.string.your_time), timeSeconds / 60, timeSeconds % 60));
        tvHighScore.setText(highScore == Integer.MAX_VALUE ?
                getString(R.string.no_high_score) :
                String.format(getString(R.string.best), highScore / 60, highScore % 60));

        view.findViewById(R.id.btnPlayAgain).setOnClickListener(v -> {
            listener.onPlayAgain();
            dismiss();
        });

        view.findViewById(R.id.btnReturnHome).setOnClickListener(v -> {
            listener.onReturnHome();
            dismiss();
        });

        return builder.setView(view).create();
    }
}
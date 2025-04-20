package com.example.minesweeper;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

public class MainActivity extends AppCompatActivity implements GameOverDialogFragment.GameOverListener {

    private GameBoard gameBoard;
    private Button btnEasy, btnMedium, btnHard;
    private int gridSize = 5, numMines = 3;
    private GridLayout gameGrid;
    private TextView tvTimer, tvFlags;
    private Button[][] gridButtons;
    private final Animation[] gridAnimations = new Animation[4];
    private final Handler timerHandler = new Handler();
    private int secondsPassed = 0;
    private boolean timerRunning = false;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridAnimations[0] = AnimationUtils.loadAnimation(this, R.anim.grid_item_top_in);
        gridAnimations[1] = AnimationUtils.loadAnimation(this, R.anim.grid_item_right_in);
        gridAnimations[2] = AnimationUtils.loadAnimation(this, R.anim.grid_item_bottom_in);
        gridAnimations[3] = AnimationUtils.loadAnimation(this, R.anim.grid_item_left_in);
        sharedPreferences = getSharedPreferences("MinesweeperPrefs", MODE_PRIVATE);

        initializeHomeUI();
        setupDifficultyAndHintButtons();
    }

    private void setTextGradient(TextView textView, String startColor, String endColor)
    {
        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());
        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{Color.parseColor(startColor), Color.parseColor(endColor)},
                null, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(textShader);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeHomeUI()
    {
        TextView titleText = findViewById(R.id.titleText);
        Animation fadeScale = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in);
        titleText.startAnimation(fadeScale);
        //setTextGradient(titleText, "#9C27B0", "#2196F3");

        TextView startButton = findViewById(R.id.startButton);
        startButton.startAnimation(fadeScale);
        //setTextGradient(startButton, "#2196F3", "#F44336");

        startButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setScaleX(0.95f);
                    v.setScaleY(0.95f);
                    return true;
                case MotionEvent.ACTION_UP:
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                    showGameScreen();
                    setupGame(gridSize, numMines);
                    v.performClick();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                    return true;
            }
            return false;
        });

    }

    private void highlightDifficultyButton(Button selected)
    {
        btnEasy.setBackgroundTintList(ContextCompat.getColorStateList(this,
                selected == btnEasy ? R.color.difficulty_easy : R.color.difficulty_unselected));
        btnMedium.setBackgroundTintList(ContextCompat.getColorStateList(this,
                selected == btnMedium ? R.color.difficulty_medium : R.color.difficulty_unselected));
        btnHard.setBackgroundTintList(ContextCompat.getColorStateList(this,
                selected == btnHard ? R.color.difficulty_hard : R.color.difficulty_unselected));
    }


    private void setupDifficultyAndHintButtons()
    {
        btnEasy = findViewById(R.id.btnEasy);
        btnMedium = findViewById(R.id.btnMedium);
        btnHard = findViewById(R.id.btnHard);
        Button btnHint = findViewById(R.id.btnHint);

        btnEasy.setOnClickListener(v -> {
            setupGame(5, 3);
            highlightDifficultyButton(btnEasy);
        });
        btnMedium.setOnClickListener(v -> {
            setupGame(8, 10);
            highlightDifficultyButton(btnMedium);
        });
        btnHard.setOnClickListener(v -> {
            setupGame(10, 20);
            highlightDifficultyButton(btnHard);
        });
        if (btnHint != null)
            btnHint.setOnClickListener(v -> revealHint());
    }

    private void showGameScreen()
    {
        findViewById(R.id.homeLayout).setVisibility(View.GONE);
        findViewById(R.id.gameLayout).setVisibility(View.VISIBLE);
        initializeGameViews();
    }

    private void showHomeScreen()
    {
        findViewById(R.id.gameLayout).setVisibility(View.GONE);
        findViewById(R.id.homeLayout).setVisibility(View.VISIBLE);
    }

    private void initializeGameViews()
    {
        gameGrid = findViewById(R.id.gameGrid);
        tvTimer = findViewById(R.id.tvTimer);
        tvFlags = findViewById(R.id.tvFlags);
        Button btnRestart = findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(v -> setupGame(gridSize, numMines));
    }

    private final Runnable timerRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            secondsPassed++;
            if (tvTimer != null)
                tvTimer.setText(String.format(getString(R.string.time), secondsPassed / 60, secondsPassed % 60));
            timerHandler.postDelayed(this, 1000);
        }
    };

    private void setupGame(int size, int mines)
    {
        gridSize = size;
        numMines = mines;
        secondsPassed = 0;
        timerRunning = false;
        if (tvTimer != null)
            tvTimer.setText(String.format(getString(R.string.time), 0, 0));
        if (tvFlags != null)
            tvFlags.setText(String.format(getString(R.string.flags), 0));
        gameBoard = new GameBoard(gridSize, numMines);
        createGameGrid();
    }

    private void createGameGrid()
    {
        if (gameGrid == null)
            return;
        gameGrid.removeAllViews();
        gameGrid.setColumnCount(gridSize);
        gameGrid.setRowCount(gridSize);

        int buttonSize = calculateButtonSize();
        gridButtons = new Button[gridSize][gridSize];

        for (int row = 0; row < gridSize; row++)
            for (int col = 0; col < gridSize; col++)
                createGridButton(row, col, buttonSize);
    }

    private int calculateButtonSize()
    {
        View container = findViewById(R.id.gridContainer);
        return container.getWidth() / gridSize;
    }

    private void createGridButton(int row, int col, int size)
    {
        Button button = new Button(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(0, 0, 0, 0);
        button.setLayoutParams(params);
        button.setBackgroundResource(R.drawable.tile_background);
        button.setPadding(0, 0, 0, 0);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(button, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

        button.setOnClickListener(v -> handleTileClick(row, col));
        button.setOnLongClickListener(v ->
        {
            handleTileLongClick(row, col);
            return true;
        });

        animateButtonEntry(button, row, col);
        gridButtons[row][col] = button;
        gameGrid.addView(button);
    }

    private void animateButtonEntry(Button button, int row, int col)
    {
        button.setVisibility(View.INVISIBLE);
        button.postDelayed(() ->
        {
            button.setVisibility(View.VISIBLE);
            button.startAnimation(selectGridAnimation(row, col));
        }, calculateAnimationDelay(row, col));
    }

    private Animation selectGridAnimation(int row, int col)
    {
        if (row < gridSize / 2 && col < gridSize / 2)
            return row < col ? gridAnimations[0] : gridAnimations[3];
        else if (row < gridSize / 2)
            return row < (gridSize - col - 1) ? gridAnimations[0] : gridAnimations[1];
        else if (col < gridSize / 2)
            return (gridSize - row - 1) < col ? gridAnimations[2] : gridAnimations[3];
        else
            return row > col ? gridAnimations[2] : gridAnimations[1];
    }

    private int calculateAnimationDelay(int row, int col)
    {
        int distance = Math.min(
                Math.min(row, gridSize - 1 - row),
                Math.min(col, gridSize - 1 - col)
        );
        return distance * 100;
    }

    private void handleTileClick(int row, int col)
    {
        if (!timerRunning)
            startTimer();
        gameBoard.revealTile(row, col);
        updateGameUI();
        checkGameStatus();
    }

    private void handleTileLongClick(int row, int col) {
        Tile tile = gameBoard.getTile(row, col);
        if (tile.isRevealed())
            return;

        int flags = gameBoard.getFlagCount();

        if (tile.isFlagged())
        {
            gameBoard.toggleFlag(row, col);
            updateGameUI();
            return;
        }

        if (flags < numMines)
        {
            gameBoard.toggleFlag(row, col);
            updateGameUI();
        }
        else
            Toast.makeText(this, "No more flags allowed!", Toast.LENGTH_SHORT).show();
    }


    private void updateGameUI()
    {
        for (int row = 0; row < gridSize; row++)
            for (int col = 0; col < gridSize; col++)
                updateButtonAppearance(row, col);
        updateFlagDisplay();
    }

    private void updateButtonAppearance(int row, int col)
    {
        Tile tile = gameBoard.getTile(row, col);
        Button button = gridButtons[row][col];

        if (tile.isRevealed())
        {
            button.setBackgroundResource(R.drawable.tile_background);
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.tile_revealed));
            if (tile.isMine())
            {
                button.setText("💣");
                button.setTextColor(ContextCompat.getColor(this, R.color.mine));
            }
            else if (tile.getAdjacentMines() > 0)
            {
                button.setText(String.valueOf(tile.getAdjacentMines()));
                button.setTextColor(getNumberColor(tile.getAdjacentMines()));
            }
        }
        else if (tile.isFlagged())
        {
            button.setText("🚩");
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.tile_flagged));
        }
        else
        {
            button.setText("");
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.tile_default));
        }
    }

    private int getNumberColor(int number)
    {
        switch (number)
        {
            case 1: return ContextCompat.getColor(this, android.R.color.holo_blue_dark);
            case 2: return ContextCompat.getColor(this, android.R.color.holo_green_dark);
            case 3: return ContextCompat.getColor(this, android.R.color.holo_red_dark);
            case 4: return ContextCompat.getColor(this, android.R.color.holo_purple);
            case 5: return ContextCompat.getColor(this, android.R.color.holo_orange_dark);
            case 6: return ContextCompat.getColor(this, android.R.color.holo_blue_bright);
            case 7: return ContextCompat.getColor(this, android.R.color.white);
            case 8: return ContextCompat.getColor(this, android.R.color.darker_gray);
            default: return ContextCompat.getColor(this, android.R.color.black);
        }
    }

    private void updateFlagDisplay()
    {
        int flags = 0;
        for (int row = 0; row < gridSize; row++)
            for (int col = 0; col < gridSize; col++)
                if (gameBoard.getTile(row, col).isFlagged())
                    flags++;
        if (tvFlags != null)
            tvFlags.setText(String.format(getString(R.string.flags), flags));
    }

    private void checkGameStatus()
    {
        if (gameBoard.isGameOver())
            handleGameEnd(gameBoard.isWon());

    }

    private void handleGameEnd(boolean won)
    {
        timerHandler.removeCallbacks(timerRunnable);
        timerRunning = false;

        int highScore = sharedPreferences.getInt("HighScore_" + gridSize, Integer.MAX_VALUE);
        if (won && secondsPassed < highScore)
        {
            sharedPreferences.edit().putInt("HighScore_" + gridSize, secondsPassed).apply();
            highScore = secondsPassed;
        }

        new GameOverDialogFragment(won, secondsPassed, highScore)
                .show(getSupportFragmentManager(), "game_over");
    }

    private void revealHint()
    {
        for (int row = 0; row < gridSize; row++)
        {
            for (int col = 0; col < gridSize; col++)
            {
                Tile tile = gameBoard.getTile(row, col);
                if (!tile.isRevealed() && !tile.isMine() && !tile.isFlagged())
                {
                    gameBoard.revealTile(row, col);
                    updateGameUI();
                    return;
                }
            }
        }
    }

    private void startTimer()
    {
        timerRunning = true;
        timerHandler.post(timerRunnable);
    }

    @Override
    public void onPlayAgain()
    {
        setupGame(gridSize, numMines);
    }

    @Override
    public void onReturnHome()
    {
        showHomeScreen();
    }

}
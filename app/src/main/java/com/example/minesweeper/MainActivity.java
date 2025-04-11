package com.example.minesweeper;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private GameBoard gameBoard;
    private TextView statusText;
    private Button[][] buttons;
    private boolean isGameBoardVisible = false;
    private static final int GRID_SIZE = 8;
    private static final int NUM_MINES = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.startButton);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startButton.setOnClickListener(v -> startGame());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startGame() {
        gameBoard = new GameBoard(GRID_SIZE, NUM_MINES);
        setupGameBoard();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupGameBoard() {
        setContentView(R.layout.game_board);
        isGameBoardVisible = true;

        GridLayout gameGrid = findViewById(R.id.gameGrid);
        FrameLayout gridContainer = findViewById(R.id.gridContainer);
        statusText = findViewById(R.id.statusText);
        Button restartButton = findViewById(R.id.restartButton);

        buttons = new Button[GRID_SIZE][GRID_SIZE];

        // Set fixed column and row count
        gameGrid.setColumnCount(GRID_SIZE);
        gameGrid.setRowCount(GRID_SIZE);
        
        // Let the grid calculate its own dimensions first
        gridContainer.post(() -> {
            int containerWidth = gridContainer.getWidth();
            int containerHeight = gridContainer.getHeight();
            
            // Use the minimum dimension to ensure it fits
            int containerSize = Math.min(containerWidth, containerHeight);
            
            // Calculate button size to fit perfectly in the grid
            int buttonSize = (containerSize / GRID_SIZE);
            
            // Create and add buttons to the grid
            createButtons(gameGrid, buttonSize);
        });

        restartButton.setOnClickListener(v -> {
            setContentView(R.layout.activity_main);
            isGameBoardVisible = false;
            startGame();
        });
    }
    
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createButtons(GridLayout gameGrid, int buttonSize) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Button button = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(row, 1f), GridLayout.spec(col, 1f));
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(0, 0, 0, 0);
                button.setLayoutParams(params);
                button.setBackgroundResource(R.drawable.tile_background);
                button.setPadding(0, 0, 0, 0);
                
                // IMPORTANT: Increase font size to fit the button better
                // Increasing from previous value of 3.5f to 2.5f makes it larger
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                float textSizeInSp = buttonSize / displayMetrics.density / 2.5f;
                button.setTextSize(textSizeInSp);
                button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                button.setAllCaps(false); // Prevents text from being capitalized

                final int finalRow = row;
                final int finalCol = col;

                button.setOnClickListener(v -> onTileClick(finalRow, finalCol));
                button.setOnLongClickListener(v -> {
                    onTileLongClick(finalRow, finalCol);
                    return true;
                });

                buttons[row][col] = button;
                gameGrid.addView(button);
            }
        }
    }
    
    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void onTileClick(int row, int col) {
        if (gameBoard.isGameOver()) {
            return;
        }

        gameBoard.revealTile(row, col);
        updateUI();

        if (gameBoard.isGameOver()) {
            if (gameBoard.isWon()) {
                statusText.setText("Congratulations! You Won!");
                statusText.setTextColor(ContextCompat.getColor(this, R.color.success));
            } else {
                statusText.setText("Game Over! You Hit a Mine!");
                statusText.setTextColor(ContextCompat.getColor(this, R.color.failure));
                revealAllMines();
            }
        }
    }

    private void onTileLongClick(int row, int col) {
        if (!gameBoard.isGameOver()) {
            gameBoard.toggleFlag(row, col);
            updateUI();
        }
    }

    private void updateUI() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Tile tile = gameBoard.getTile(row, col);
                Button button = buttons[row][col];

                if (tile.isRevealed()) {
                    button.setBackgroundResource(R.drawable.tile_background);
                    button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.tile_revealed));
                    if (tile.isMine()) {
                        button.setText("💣");
                        button.setTextColor(ContextCompat.getColor(this, R.color.mine));
                    } else if (tile.getAdjacentMines() > 0) {
                        button.setText(String.valueOf(tile.getAdjacentMines()));
                        button.setTextColor(getNumberColor(tile.getAdjacentMines()));
                    } else {
                        button.setText("");
                    }
                } else if (tile.isFlagged()) {
                    button.setText("🚩");
                    button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.tile_flagged));
                } else {
                    button.setText("");
                    button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.tile_default));
                }
            }
        }
    }

    private int getNumberColor(int number) {
        switch (number) {
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

    private void revealAllMines() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Tile tile = gameBoard.getTile(row, col);
                if (tile.isMine()) {
                    buttons[row][col].setText("💣");
                    buttons[row][col].setTextColor(ContextCompat.getColor(this, R.color.mine));
                    buttons[row][col].setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.tile_revealed));
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isGameBoardVisible) {
            setContentView(R.layout.activity_main);
            isGameBoardVisible = false;
        } else {
            super.onBackPressed();
        }
    }
}
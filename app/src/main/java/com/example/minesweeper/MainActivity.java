package com.example.minesweeper;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private Animation[] animations = new Animation[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load animations
        animations[0] = AnimationUtils.loadAnimation(this, R.anim.grid_item_top_in);
        animations[1] = AnimationUtils.loadAnimation(this, R.anim.grid_item_right_in);
        animations[2] = AnimationUtils.loadAnimation(this, R.anim.grid_item_bottom_in);
        animations[3] = AnimationUtils.loadAnimation(this, R.anim.grid_item_left_in);

        // Apply gradient to title text
        TextView titleText = findViewById(R.id.titleText);
        setTextGradient(titleText, "#9C27B0", "#2196F3"); // Purple to Blue

        // Apply gradient to start button text
        TextView startButton = findViewById(R.id.startButton);
        setTextGradient(startButton, "#2196F3", "#F44336"); // Blue to Red

        // Setup start button without background changes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startButton.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // When pressed, scale down slightly
                        v.setScaleX(0.95f);
                        v.setScaleY(0.95f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        // When released, return to normal and start game
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                        startGame();
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        // If touch cancelled, return to normal
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                        return true;
                }
                return false;
            });
        }
    }

    // Helper method to set gradient on TextView
    private void setTextGradient(TextView textView, String startColor, String endColor) {
        TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());
        
        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{
                        Color.parseColor(startColor),
                        Color.parseColor(endColor)
                }, null, Shader.TileMode.CLAMP);
        
        textView.getPaint().setShader(textShader);
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
        TextView restartButton = findViewById(R.id.restartButton);

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
            
            // Create and add buttons to the grid with animation
            createButtons(gameGrid, buttonSize);
        });

        // Setup restart button without background changes
        restartButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // When pressed, scale down slightly
                    v.setScaleX(0.95f);
                    v.setScaleY(0.95f);
                    return true;
                case MotionEvent.ACTION_UP:
                    // When released, return to normal and restart
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                    setContentView(R.layout.activity_main);
                    isGameBoardVisible = false;
                    startGame();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    // If touch cancelled, return to normal
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                    return true;
            }
            return false;
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
                
                // Set font size
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                float textSizeInSp = buttonSize / displayMetrics.density / 2.0f;
                button.setTextSize(textSizeInSp);
                button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                button.setAllCaps(false);

                final int finalRow = row;
                final int finalCol = col;

                button.setOnClickListener(v -> onTileClick(finalRow, finalCol));
                button.setOnLongClickListener(v -> {
                    onTileLongClick(finalRow, finalCol);
                    return true;
                });

                buttons[row][col] = button;
                gameGrid.addView(button);
                
                // Initially hide the button
                button.setVisibility(Button.INVISIBLE);
                
                // Create dramatic popping animation from different directions
                int delay = calculateAnimationDelay(row, col);
                
                // Choose animation based on cell position
                Animation animation = chooseAnimation(row, col);
                
                // Apply animation with delay
                final int finalDelay = delay;
                button.postDelayed(() -> {
                    button.setVisibility(Button.VISIBLE);
                    button.startAnimation(animation);
                }, finalDelay);
            }
        }
    }
    
    // Choose animation based on cell position
    private Animation chooseAnimation(int row, int col) {
        // Top section
        if (row < GRID_SIZE / 2 && col < GRID_SIZE / 2) {
            if (row < col) return animations[0]; // Top
            else return animations[3]; // Left
        }
        // Top-right section
        else if (row < GRID_SIZE / 2 && col >= GRID_SIZE / 2) {
            if (row < (GRID_SIZE - col - 1)) return animations[0]; // Top
            else return animations[1]; // Right
        }
        // Bottom-left section
        else if (row >= GRID_SIZE / 2 && col < GRID_SIZE / 2) {
            if ((GRID_SIZE - row - 1) < col) return animations[2]; // Bottom
            else return animations[3]; // Left
        }
        // Bottom-right section
        else {
            if (row > col) return animations[2]; // Bottom
            else return animations[1]; // Right
        }
    }
    
    // Calculate animation delay based on position
    private int calculateAnimationDelay(int row, int col) {
        // Calculate distance from edges for outside-in animation
        int distanceFromEdge = Math.min(
            Math.min(row, GRID_SIZE - 1 - row),
            Math.min(col, GRID_SIZE - 1 - col)
        );
        
        // Base delay in milliseconds - increased for slower animation
        int baseDelay = 100;
        
        // Calculate total delay (outer cells appear first)
        return distanceFromEdge * baseDelay;
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
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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements 
    GameOverDialogFragment.GameOverListener,
    ExitGameDialogFragment.ExitGameListener {

    private GameBoard gameBoard;
    private Button btnEasy, btnMedium, btnHard;
    private int gridSize = 8, numMines = 10;
    private GridLayout gameGrid;
    private TextView tvTimer, tvFlags;
    private Button[][] gridButtons;
    private final Animation[] gridAnimations = new Animation[4];
    private final Handler timerHandler = new Handler();
    private int secondsPassed = 0;
    private boolean timerRunning = false;
    private SharedPreferences sharedPreferences;
    private UserDatabaseHelper dbHelper;
    private User currentUser;
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private TextView tvWelcome, tvHighScore;
    private RecyclerView rvUserProfiles;
    private UserProfileAdapter userProfileAdapter;
    private Button btnAddProfile;
    private TextView tvSelectProfile;
    private LinearLayout loginRegisterLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new UserDatabaseHelper(this);
        initializeViews();
        setupAnimations();
        setupListeners();
        setupUserProfiles();
    }

    private void initializeViews()
    {
        loginRegisterLayout = findViewById(R.id.loginRegisterLayout);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvHighScore = findViewById(R.id.tvHighScore);
        rvUserProfiles = findViewById(R.id.rvUserProfiles);
        btnAddProfile = findViewById(R.id.btnAddProfile);
        tvSelectProfile = findViewById(R.id.tvSelectProfile);
        
        // Initialize game views
        btnEasy = findViewById(R.id.btnEasy);
        btnMedium = findViewById(R.id.btnMedium);
        btnHard = findViewById(R.id.btnHard);
        gameGrid = findViewById(R.id.gameGrid);
        tvTimer = findViewById(R.id.tvTimer);
        tvFlags = findViewById(R.id.tvFlags);
        
        // Initialize animations
        gridAnimations[0] = AnimationUtils.loadAnimation(this, R.anim.grid_item_top_in);
        gridAnimations[1] = AnimationUtils.loadAnimation(this, R.anim.grid_item_right_in);
        gridAnimations[2] = AnimationUtils.loadAnimation(this, R.anim.grid_item_bottom_in);
        gridAnimations[3] = AnimationUtils.loadAnimation(this, R.anim.grid_item_left_in);
        sharedPreferences = getSharedPreferences("MinesweeperPrefs", MODE_PRIVATE);
    }

    private void setupAnimations()
    {
        TextView titleText = findViewById(R.id.titleText);
        Animation fadeScale = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in);
        titleText.startAnimation(fadeScale);

        etUsername.startAnimation(fadeScale);
        etPassword.startAnimation(fadeScale);
        btnLogin.startAnimation(fadeScale);
        btnRegister.startAnimation(fadeScale);
    }

    private void setupListeners()
    {
        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> handleRegister());
        btnAddProfile.setOnClickListener(v -> showLoginRegisterScreen());

        // Setup difficulty buttons
        btnEasy.setOnClickListener(v -> {
            setupGame(5, 3);
            highlightDifficultyButton(btnEasy);
            updateHighScoreDisplay("easy");
        });
        btnMedium.setOnClickListener(v -> {
            setupGame(8, 10);
            highlightDifficultyButton(btnMedium);
            updateHighScoreDisplay("medium");
        });
        btnHard.setOnClickListener(v -> {
            setupGame(10, 20);
            highlightDifficultyButton(btnHard);
            updateHighScoreDisplay("hard");
        });

        // Setup hint button
        Button btnHint = findViewById(R.id.btnHint);
        if (btnHint != null) {
            btnHint.setOnClickListener(v -> revealHint());
        }

        // Setup restart button
        Button btnRestart = findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(v -> setupGame(gridSize, numMines));
    }

    private void setupUserProfiles() {
        rvUserProfiles.setLayoutManager(new LinearLayoutManager(this));
        userProfileAdapter = new UserProfileAdapter(dbHelper.getAllUsers(), new UserProfileAdapter.OnUserProfileClickListener() {
            @Override
            public void onUserProfileClick(User user) {
                onUserProfileSelected(user);
            }

            @Override
            public void onProfileDeleted() {
                // If the current user was deleted, clear it
                if (currentUser != null && !dbHelper.checkUser(currentUser.getUsername(), currentUser.getPassword())) {
                    currentUser = null;
                }
            }
        }, dbHelper);
        rvUserProfiles.setAdapter(userProfileAdapter);
    }

    private void onUserProfileSelected(User user) {
        currentUser = user;
        showGameScreen();
        setupGame(gridSize, numMines);
    }

    private void showLoginRegisterScreen() {
        loginRegisterLayout.setVisibility(View.VISIBLE);
        btnAddProfile.setVisibility(View.GONE);
    }

    private void handleLogin()
    {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.checkUser(username, password)) {
            currentUser = dbHelper.getUser(username);
            loginRegisterLayout.setVisibility(View.GONE);
            btnAddProfile.setVisibility(View.VISIBLE);
            userProfileAdapter.updateUsers(dbHelper.getAllUsers());
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRegister()
    {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.getUser(username) != null) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(username, password);
        if (dbHelper.addUser(newUser)) {
            currentUser = newUser;
            loginRegisterLayout.setVisibility(View.GONE);
            btnAddProfile.setVisibility(View.VISIBLE);
            userProfileAdapter.updateUsers(dbHelper.getAllUsers());
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUserInfo()
    {
        etUsername.setVisibility(View.GONE);
        etPassword.setVisibility(View.GONE);
        btnLogin.setVisibility(View.GONE);
        btnRegister.setVisibility(View.GONE);

        tvWelcome.setVisibility(View.VISIBLE);
        tvHighScore.setVisibility(View.VISIBLE);

        tvWelcome.setText("Welcome, " + currentUser.getUsername() + "!");
        updateHighScoreDisplay(getCurrentDifficulty());
    }

    private String getCurrentDifficulty()
    {
        if (gridSize == 5 && numMines == 3) return "easy";
        if (gridSize == 8 && numMines == 10) return "medium";
        if (gridSize == 10 && numMines == 20) return "hard";
        return "medium";
    }

    @Override
    public void onGameOver(boolean won, int time)
    {
        if (won && currentUser != null) {
            String difficulty = getCurrentDifficulty();
            int currentHighScore = currentUser.getHighScore(difficulty);
            if (time < currentHighScore) {
                currentUser.setHighScore(difficulty, time);
                dbHelper.updateHighScore(currentUser.getUsername(), difficulty, time);
                tvHighScore.setText("New High Score: " + time + " seconds!");
            }
        }
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
    private void initializeHomeUI() {
        TextView titleText = findViewById(R.id.titleText);
        Animation fadeScale = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in);
        titleText.startAnimation(fadeScale);

        etUsername.startAnimation(fadeScale);
        etPassword.startAnimation(fadeScale);
        btnLogin.startAnimation(fadeScale);
        btnRegister.startAnimation(fadeScale);
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

    private void updateHighScoreDisplay(String difficulty) {
        if (currentUser != null && tvHighScore != null) {
            int highScore = currentUser.getHighScore(difficulty);
            String highScoreText;
            if (highScore == Integer.MAX_VALUE) {
                highScoreText = "No high score yet";
            } else {
                highScoreText = "High Score: " + highScore + " seconds";
            }
            tvHighScore.setText(highScoreText);
            tvHighScore.setVisibility(View.VISIBLE);
        }
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

        // Wait for layout to be measured before calculating button size
        gameGrid.post(() -> {
            int buttonSize = calculateButtonSize();
            gridButtons = new Button[gridSize][gridSize];

            for (int row = 0; row < gridSize; row++)
                for (int col = 0; col < gridSize; col++)
                    createGridButton(row, col, buttonSize);
        });
    }

    private int calculateButtonSize()
    {
        View container = findViewById(R.id.gridContainer);
        int containerWidth = container.getWidth();
        int containerHeight = container.getHeight();
        
        // Calculate size based on the smaller dimension to ensure square buttons
        int size = Math.min(containerWidth, containerHeight) / gridSize;
        
        // Add some padding to prevent edge bleeding
        int padding = 4; // 2dp padding on each side
        return Math.max(1, size - padding);  // Ensure size is at least 1
    }

    private void createGridButton(int row, int col, int size)
    {
        Button button = new Button(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(1, 1, 1, 1);  // Reduced margins to prevent bleeding
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

        if (won && currentUser != null) {
            String difficulty = getCurrentDifficulty();
            int currentHighScore = currentUser.getHighScore(difficulty);
            
            if (secondsPassed < currentHighScore) {
                currentUser.setHighScore(difficulty, secondsPassed);
                dbHelper.updateHighScore(currentUser.getUsername(), difficulty, secondsPassed);
                updateHighScoreDisplay(difficulty);
                Toast.makeText(this, "New High Score: " + secondsPassed + " seconds!", Toast.LENGTH_LONG).show();
            }
        }

        new GameOverDialogFragment(won, secondsPassed, currentUser != null ? 
            currentUser.getHighScore(getCurrentDifficulty()) : Integer.MAX_VALUE)
                .show(getSupportFragmentManager(), "game_over");
    }

    private void revealHint()
    {
        if (gameBoard == null) return;
        
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Tile tile = gameBoard.getTile(row, col);
                if (!tile.isRevealed() && !tile.isMine() && !tile.isFlagged()) {
                    gameBoard.revealTile(row, col);
                    updateGameUI();
                    return;
                }
            }
        }
        Toast.makeText(this, "No hints available!", Toast.LENGTH_SHORT).show();
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
        updateHighScoreDisplay(getCurrentDifficulty());
    }

    @Override
    public void onReturnHome()
    {
        showHomeScreen();
        userProfileAdapter.updateUsers(dbHelper.getAllUsers());
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.gameLayout).getVisibility() == View.VISIBLE) {
            showExitDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showExitDialog() {
        ExitGameDialogFragment dialog = new ExitGameDialogFragment();
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "exit_game");
    }

    @Override
    public void onGoHome() {
        showHomeScreen();
        userProfileAdapter.updateUsers(dbHelper.getAllUsers());
    }

    @Override
    public void onQuitGame() {
        finish();
    }

}
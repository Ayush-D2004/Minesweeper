package com.example.minesweeper;

import java.util.Random;

public class GameBoard {
    private Tile[][] grid;
    private int gridSize;
    private int numMines;
    private boolean isGameOver;
    private boolean isWon;

    public GameBoard(int gridSize, int numMines) {
        this.gridSize = gridSize;
        this.numMines = numMines;
        this.isGameOver = false;
        this.isWon = false;
        initializeGrid();
    }

    private void initializeGrid() {
        grid = new Tile[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = new Tile();
            }
        }
        placeMines();
        calculateAdjacentMines();
    }

    private void placeMines() {
        Random random = new Random();
        int minesPlaced = 0;
        while (minesPlaced < numMines) {
            int row = random.nextInt(gridSize);
            int col = random.nextInt(gridSize);
            if (!grid[row][col].isMine()) {
                grid[row][col].setMine(true);
                minesPlaced++;
            }
        }
    }

    private void calculateAdjacentMines() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (!grid[row][col].isMine()) {
                    int count = 0;
                    for (int r = Math.max(0, row - 1); r <= Math.min(gridSize - 1, row + 1); r++) {
                        for (int c = Math.max(0, col - 1); c <= Math.min(gridSize - 1, col + 1); c++) {
                            if (grid[r][c].isMine()) {
                                count++;
                            }
                        }
                    }
                    grid[row][col].setAdjacentMines(count);
                }
            }
        }
    }

    public void revealTile(int row, int col) {
        if (isGameOver || grid[row][col].isRevealed() || grid[row][col].isFlagged()) {
            return;
        }

        grid[row][col].setRevealed(true);

        if (grid[row][col].isMine()) {
            isGameOver = true;
            isWon = false;
            return;
        }

        if (grid[row][col].getAdjacentMines() == 0) {
            for (int r = Math.max(0, row - 1); r <= Math.min(gridSize - 1, row + 1); r++) {
                for (int c = Math.max(0, col - 1); c <= Math.min(gridSize - 1, col + 1); c++) {
                    if (!grid[r][c].isRevealed()) {
                        revealTile(r, c);
                    }
                }
            }
        }

        checkWin();
    }

    public void toggleFlag(int row, int col) {
        if (!isGameOver && !grid[row][col].isRevealed()) {
            grid[row][col].setFlagged(!grid[row][col].isFlagged());
        }
    }

    private void checkWin() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (!grid[row][col].isMine() && !grid[row][col].isRevealed()) {
                    return;
                }
            }
        }
        isGameOver = true;
        isWon = true;
    }

    public Tile getTile(int row, int col) {
        return grid[row][col];
    }

    public int getGridSize() {
        return gridSize;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public boolean isWon() {
        return isWon;
    }

    public void reset() {
        isGameOver = false;
        isWon = false;
        initializeGrid();
    }
} 
package com.example.minesweeper;

import java.util.Random;

public class GameBoard
{
    private Tile[][] grid;
    private final int gridSize;
    private final int numMines;
    private boolean gameOver;
    private boolean isWon;

    public GameBoard(int gridSize, int numMines)
    {
        this.gridSize = gridSize;
        this.numMines = numMines;
        initializeGrid();
    }

    private void initializeGrid()
    {
        grid = new Tile[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++)
                grid[i][j] = new Tile();
        placeMines();
        calculateAdjacentMines();
    }

    private void placeMines()
    {
        Random rand = new Random();
        int minesPlaced = 0;
        while (minesPlaced < numMines)
        {
            int row = rand.nextInt(gridSize);
            int col = rand.nextInt(gridSize);
            if (!grid[row][col].isMine())
            {
                grid[row][col].setMine(true);
                minesPlaced++;
            }
        }
    }

    private void calculateAdjacentMines()
    {
        for (int row = 0; row < gridSize; row++)
            for (int col = 0; col < gridSize; col++)
                if (!grid[row][col].isMine())
                    grid[row][col].setAdjacentMines(countAdjacentMines(row, col));
    }

    private int countAdjacentMines(int row, int col)
    {
        int count = 0;
        for (int i = row-1; i <= row+1; i++)
            for (int j = col-1; j <= col+1; j++)
                if (i >= 0 && i < gridSize && j >= 0 && j < gridSize && grid[i][j].isMine())
                    count++;
        return count;
    }

    public void revealTile(int row, int col)
    {
        if (grid[row][col].isRevealed() || grid[row][col].isFlagged())
            return;

        grid[row][col].setRevealed(true);

        if (grid[row][col].isMine())
        {
            gameOver = true;
            isWon = false;
            return;
        }

        if (grid[row][col].getAdjacentMines() == 0)
            revealAdjacentTiles(row, col);

        checkWin();
    }

    private void revealAdjacentTiles(int row, int col)
    {
        for (int i = row-1; i <= row+1; i++)
            for (int j = col-1; j <= col+1; j++)
                if (i >= 0 && i < gridSize && j >= 0 && j < gridSize && !grid[i][j].isRevealed())
                    revealTile(i, j);
    }

    public void toggleFlag(int row, int col)
    {
        grid[row][col].setFlagged(!grid[row][col].isFlagged());
    }

    private void checkWin()
    {
        int revealedCount = 0;
        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++)
                if (grid[i][j].isRevealed() && !grid[i][j].isMine())
                    revealedCount++;
        if (revealedCount == (gridSize * gridSize) - numMines)
        {
            gameOver = true;
            isWon = true;
        }
    }

    public Tile getTile(int row, int col)
    {
        return grid[row][col];
    }

    public boolean isGameOver()
    {
        return gameOver;
    }
    public boolean isWon()
    {
        return isWon;
    }
    public int getFlagCount()
    {
        int count = 0;
        for (Tile[] row : grid)
            for (Tile tile : row)
                if (tile.isFlagged())
                    count++;
        return count;
    }
}

package com.example.minesweeper;

public class Tile
{
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMines;

    public Tile()
    {
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;
    }

    public boolean isMine()
    {
        return isMine;
    }
    public boolean isRevealed()
    {
        return isRevealed;
    }
    public boolean isFlagged()
    {
        return isFlagged;
    }
    public int getAdjacentMines()
    {
        return adjacentMines;
    }

    public void setMine(boolean mine)
    {
        isMine = mine;
    }
    public void setRevealed(boolean revealed)
    {
        isRevealed = revealed;
    }
    public void setFlagged(boolean flagged)
    {
        isFlagged = flagged;
    }
    public void setAdjacentMines(int adjacentMines)
    {
        this.adjacentMines = adjacentMines;
    }
}

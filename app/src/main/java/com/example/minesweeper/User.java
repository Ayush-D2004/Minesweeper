package com.example.minesweeper;

public class User {
    private String username;
    private String password;
    private int easyHighScore;
    private int mediumHighScore;
    private int hardHighScore;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.easyHighScore = Integer.MAX_VALUE;
        this.mediumHighScore = Integer.MAX_VALUE;
        this.hardHighScore = Integer.MAX_VALUE;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    
    public int getHighScore(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy": return easyHighScore;
            case "medium": return mediumHighScore;
            case "hard": return hardHighScore;
            default: return Integer.MAX_VALUE;
        }
    }

    public void setHighScore(String difficulty, int score) {
        switch (difficulty.toLowerCase()) {
            case "easy": 
                if (score < easyHighScore) easyHighScore = score;
                break;
            case "medium": 
                if (score < mediumHighScore) mediumHighScore = score;
                break;
            case "hard": 
                if (score < hardHighScore) hardHighScore = score;
                break;
        }
    }
} 
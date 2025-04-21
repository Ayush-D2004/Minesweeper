package com.example.minesweeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class UserDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MinesweeperUsers.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_EASY_HIGHSCORE = "easy_highscore";
    private static final String COLUMN_MEDIUM_HIGHSCORE = "medium_highscore";
    private static final String COLUMN_HARD_HIGHSCORE = "hard_highscore";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USERNAME + " TEXT PRIMARY KEY,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_EASY_HIGHSCORE + " INTEGER DEFAULT " + Integer.MAX_VALUE + ","
                + COLUMN_MEDIUM_HIGHSCORE + " INTEGER DEFAULT " + Integer.MAX_VALUE + ","
                + COLUMN_HARD_HIGHSCORE + " INTEGER DEFAULT " + Integer.MAX_VALUE + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD, user.getPassword());

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public User getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_EASY_HIGHSCORE, COLUMN_MEDIUM_HIGHSCORE, COLUMN_HARD_HIGHSCORE},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(cursor.getString(0), cursor.getString(1));
            user.setHighScore("easy", cursor.getInt(2));
            user.setHighScore("medium", cursor.getInt(3));
            user.setHighScore("hard", cursor.getInt(4));
            cursor.close();
            db.close();
            return user;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }

    public boolean updateHighScore(String username, String difficulty, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        String column;
        switch (difficulty.toLowerCase()) {
            case "easy": column = COLUMN_EASY_HIGHSCORE; break;
            case "medium": column = COLUMN_MEDIUM_HIGHSCORE; break;
            case "hard": column = COLUMN_HARD_HIGHSCORE; break;
            default: return false;
        }
        
        values.put(column, score);
        int result = db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
        db.close();
        return result > 0;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
                int easyHighScore = cursor.getInt(cursor.getColumnIndex(COLUMN_EASY_HIGHSCORE));
                int mediumHighScore = cursor.getInt(cursor.getColumnIndex(COLUMN_MEDIUM_HIGHSCORE));
                int hardHighScore = cursor.getInt(cursor.getColumnIndex(COLUMN_HARD_HIGHSCORE));

                User user = new User(username, password);
                user.setHighScore("easy", easyHighScore);
                user.setHighScore("medium", mediumHighScore);
                user.setHighScore("hard", hardHighScore);
                users.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return users;
    }

    public boolean deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_USERS, COLUMN_USERNAME + "=?", new String[]{username});
        db.close();
        return result > 0;
    }
} 
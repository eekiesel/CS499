package com.example.weighttracking;

import android.content.Context;

public class UserManager {

    private final DatabaseHelper dbHelper;

    // initializes database helper
    public UserManager(Context context) {
    this.dbHelper = new DatabaseHelper(context);
    }

    // validates user login credentials
    public boolean validateLogin(String username, String password) {
        return dbHelper.checkUser(username, password);
    }

    // checks if a user already exists in the database
    public boolean checkIfUserExists(String username) {
        return dbHelper.checkUser(username);
    }

    // creates a new user by adding their username & password to the database
    public long createUser(String username, String password) {
        return dbHelper.addUser(username, password);
    }

    public int getUserId(String username) {
        return dbHelper.getUserId(username);
    }
}

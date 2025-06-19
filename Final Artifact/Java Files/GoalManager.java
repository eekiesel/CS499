package com.example.weighttracking;

import android.content.Context;
import android.database.Cursor;
public class GoalManager {

    private final DatabaseHelper dbHelper;

    public GoalManager(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // sets or updates user's goal weight
    public long setGoalWeight(double goalWeight, int userId) {
        return dbHelper.setGoalWeight(goalWeight, userId);
    }

    // gets user's goal weight
    public Double getGoalWeight(int userId) {
        Cursor cursor = dbHelper.getGoalWeight(userId);
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(DatabaseHelper.COLUMN_GOAL_WEIGHT);
            if (index != -1) {
                double goal = cursor.getDouble(index);
                cursor.close();
                return goal;
            }
            cursor.close();
        }
        return null; // no goal weight set
    }

    // compare current weight to user's goal weight
    public boolean isGoalAchieved(double currentWeight, int userId) {
        Double goal = getGoalWeight(userId);
        return goal != null && currentWeight <= goal;
    }

}

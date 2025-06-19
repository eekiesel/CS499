package com.example.weighttracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // database information
    private static final String DATABASE_NAME = "weightTracking.db";
    private static final int DATABASE_VERSION = 4;

    // user login table
    public static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // weight tracking table
    public static final String TABLE_WEIGHT_ENTRIES = "weight_entries";
    public static final String COLUMN_WEIGHT_ID = "id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_WEIGHT = "weight";

    // goal weight table
    public static final String TABLE_GOAL_WEIGHT = "goal_weight";
    public static final String COLUMN_GOAL_ID = "id";
    public static final String COLUMN_GOAL_WEIGHT = "goal_weight";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // create necessary tables in database
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT)");
            Log.d("DatabaseHelper", "Created table: " + TABLE_USERS);

            db.execSQL("CREATE TABLE " + TABLE_WEIGHT_ENTRIES + " (" +
                    COLUMN_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_WEIGHT + " REAL, "  +
                    "user_id INTEGER)");
            Log.d("DatabaseHelper", "Created table: " + TABLE_WEIGHT_ENTRIES);

            db.execSQL("CREATE TABLE " + TABLE_GOAL_WEIGHT + " (" +
                    COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_GOAL_WEIGHT + " REAL, " +
                    "user_id INTEGER)");
            Log.d("DatabaseHelper", "Created table: " + TABLE_GOAL_WEIGHT);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Ensure goal_weight table is created
            String createGoalWeightTable = "CREATE TABLE " + TABLE_GOAL_WEIGHT + " (" +
                    COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_GOAL_WEIGHT + " REAL, " + "user_id INTEGER)";
            db.execSQL(createGoalWeightTable);
        }
        // add timestamp for firebase sync
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_WEIGHT_ENTRIES + " ADD COLUMN needs_sync INTEGER DEFAULT 0");
        }

    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, COLUMN_USERNAME + " = ?", new String[]{username}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            if (idIndex == -1) {
                Log.e("DatabaseHelper", "COLUMN_ID not found in cursor.");
                cursor.close();
                return -1; // Return -1 if column not found
            }
            int userId = cursor.getInt(idIndex);
            cursor.close();
            return userId;
        }
        return -1; // Return -1 if user not found
    }


    /************************** users table methods **************************/
    // add user to users table
    public long addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    // check if user exists in users table
    public boolean checkUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // validate user login
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{username, password},
                null,
                null,
                null
        );

        boolean valid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return valid;
    }

    /************************** weight tracking table methods **************************/
    // add weight entry into weight tracking table
    public long addWeightEntry(String date, double weight, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_WEIGHT, weight);
        values.put("user_id", userId);

        long currentTime = System.currentTimeMillis() / 1000; // Unix time in seconds
        values.put("last_updated", currentTime);
        values.put("needs_sync", 1);

        long result = db.insert(TABLE_WEIGHT_ENTRIES, null, values);
        db.close();
        return result;
    }

    // retrieve all weight entries from weight tracking table
    public Cursor getAllWeightEntries(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_WEIGHT_ENTRIES, null, "user_id = ?", new String[]{String.valueOf(userId)}, null, null, COLUMN_DATE + " ASC");
    }

    // delete an entry from weight tracking table
    public int deleteWeightEntry(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_WEIGHT_ENTRIES, COLUMN_WEIGHT_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted;
    }

    // updates an existing weight entry in weight tracking table
    public int updateWeightEntry(int id, String date, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_WEIGHT, weight);

        long currentTime = System.currentTimeMillis() / 1000;
        values.put("last_updated", currentTime);
        values.put("needs_sync", 1);

        int rowsUpdated = db.update(TABLE_WEIGHT_ENTRIES, values, COLUMN_WEIGHT_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsUpdated;
    }

    /************************** goal weight table methods **************************/
    // set goal weight in goal weight table
    public long setGoalWeight(double goalWeight, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // clear the table before inserting a new goal weight
        db.delete(TABLE_GOAL_WEIGHT, "user_id = ?", new String[]{String.valueOf(userId)});

        ContentValues values = new ContentValues();
        values.put(COLUMN_GOAL_WEIGHT, goalWeight);
        values.put("user_id", userId);

        long result = db.insert(TABLE_GOAL_WEIGHT, null, values);
        db.close();
        return result;
    }

    // retrieves goal weight from goal weight table
    public Cursor getGoalWeight(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_GOAL_WEIGHT, new String[]{COLUMN_GOAL_WEIGHT}, "user_id = ?", new String[]{String.valueOf(userId)}, null, null, null);
    }

    public Cursor getLatestWeightEntry(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_WEIGHT_ENTRIES, null, "user_id = ?", new String[]{String.valueOf(userId)}, null, null, COLUMN_DATE + " DESC", "1");
    }

    // retrieves weight entry by id
    public WeightEntry getEntryById(int entryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHT_ENTRIES, null,
                COLUMN_WEIGHT_ID + " = ?", new String[]{String.valueOf(entryId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
            double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT));
            long lastUpdated = cursor.getLong(cursor.getColumnIndexOrThrow("last_updated"));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));

            cursor.close();
            return new WeightEntry(entryId, date, weight, userId, lastUpdated);
        }

        return null;
    }

    // inserts an entry with a timestamp
    public long addWeightEntryWithTimestamp(int entryId, String date, double weight, int userId, long lastUpdated) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT_ID, entryId);  // assumes entryId is globally unique and manually set
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_WEIGHT, weight);
        values.put("user_id", userId);
        values.put("last_updated", lastUpdated);

        long result = db.insertWithOnConflict(TABLE_WEIGHT_ENTRIES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return result;
    }

    // updates existing entry and its timestamp
    public int updateWeightEntryWithTimestamp(int entryId, String date, double weight, long lastUpdated) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_WEIGHT, weight);
        values.put("last_updated", lastUpdated);

        int rows = db.update(TABLE_WEIGHT_ENTRIES, values, COLUMN_WEIGHT_ID + " = ?", new String[]{String.valueOf(entryId)});
        db.close();
        return rows;
    }

    // fetch unsynced entries
    public Cursor getUnsyncedEntries(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_WEIGHT_ENTRIES, null,
                "user_id = ? AND needs_sync = 1", new String[]{String.valueOf(userId)},
                null, null, null);
    }

    public void markEntryAsSynced(int entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("needs_sync", 0);
        db.update(TABLE_WEIGHT_ENTRIES, values, "id = ?", new String[]{String.valueOf(entryId)});
    }

    // for testing
    public void markAllEntriesAsUnsynced(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("needs_sync", 1);
        db.update(TABLE_WEIGHT_ENTRIES, values, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    // for CSV export
    public Cursor getAllEntriesForUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, date, weight FROM weight_entries WHERE user_id = ?", new String[]{String.valueOf(userId)});
    }


}
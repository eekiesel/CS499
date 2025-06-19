package com.example.weighttracking;

import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {

    private static final String COLLECTION_NAME = "weight_entries";
    private static final String TAG = "FirebaseHelper";
    private final FirebaseFirestore firestore;

    public FirebaseHelper() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void uploadWeightEntry(int userId, int entryId, String date, double weight, long lastUpdated) {
        String docId = userId + "_" + entryId;

        Map<String, Object> entryData = new HashMap<>();
        entryData.put("userId", userId);
        entryData.put("entryId", entryId);
        entryData.put("date", date);
        entryData.put("weight", weight);
        entryData.put("lastUpdated", lastUpdated);

        firestore.collection(COLLECTION_NAME)
                .document(docId)
                .set(entryData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Uploaded entry: " + docId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to upload entry", e));
    }

    public void uploadWeightEntry(int userId, int entryId, String date, double weight) {
        long now = System.currentTimeMillis();
        uploadWeightEntry(userId, entryId, date, weight, now);
    }

    public void deleteWeightEntry(int userId, int entryId) {
        String docId = userId + "_" + entryId;

        firestore.collection(COLLECTION_NAME)
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Deleted entry: " + docId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete entry", e));
    }

    public void syncFromFirebase(int userId, DatabaseHelper dbHelper) {
        firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Firebase sync: Found " + queryDocumentSnapshots.size() + " entries for user " + userId);

                    for (var doc : queryDocumentSnapshots) {
                        Long entryIdLong = doc.getLong("entryId");
                        String date = doc.getString("date");
                        Double weight = doc.getDouble("weight");
                        Long lastUpdatedLong = doc.getLong("lastUpdated");

                        if (entryIdLong == null || date == null || weight == null || lastUpdatedLong == null) {
                            Log.w(TAG, "Skipping document with missing fields: " + doc.getId());
                            continue;
                        }

                        int entryId = entryIdLong.intValue();
                        long lastUpdated = lastUpdatedLong;

                        // Check local DB for this entry
                        WeightEntry localEntry = dbHelper.getEntryById(entryId);
                        if (localEntry == null) {
                            dbHelper.addWeightEntryWithTimestamp(entryId, date, weight, userId, lastUpdated);
                            Log.d(TAG, "Inserted new local entry: " + entryId);
                        } else if (localEntry.getLastUpdated() < lastUpdated) {
                            dbHelper.updateWeightEntryWithTimestamp(entryId, date, weight, lastUpdated);
                            Log.d(TAG, "Updated local entry: " + entryId);
                        } else {
                            Log.d(TAG, "Local entry up to date: " + entryId);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firebase sync failed", e));
    }

    // upload unsynced entries
    public void syncToFirebase(int userId, DatabaseHelper dbHelper) {
        Cursor cursor = dbHelper.getUnsyncedEntries(userId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int entryId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                double weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight"));
                long lastUpdated = cursor.getLong(cursor.getColumnIndexOrThrow("last_updated"));

                uploadWeightEntry(userId, entryId, date, weight, lastUpdated);
                dbHelper.markEntryAsSynced(entryId);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

}

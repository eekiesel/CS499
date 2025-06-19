package com.example.weighttracking;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;


public class HomeActivity extends AppCompatActivity {

    private RecyclerView weightHistoryRecyclerView;
    @SuppressWarnings("FieldCanBeLocal")
    private Button dailyEntryButton, goalEntryButton, logoutButton;
    private TextView goalWeightTextView;
    private DatabaseHelper dbHelper;
    private GoalManager goalManager;
    private NotificationHelper notificationHelper;
    private LineChart weightLineChart;
    private FirebaseHelper firebaseHelper;

    private int getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getInt("current_user_id", -1); // Default to -1 if not found
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        checkSmsPermission();

        // initialize UI Components
        weightHistoryRecyclerView = findViewById(R.id.weightHistoryRecyclerView);
        dailyEntryButton = findViewById(R.id.dailyEntryButton);
        goalEntryButton = findViewById(R.id.goalEntryButton);
        logoutButton = findViewById(R.id.logoutButton);
        goalWeightTextView = findViewById(R.id.goalWeightTextView);
        weightLineChart = findViewById(R.id.weightLineChart);

        // resets zoom and drag for chart
        Button resetZoomButton = findViewById(R.id.resetZoomButton);
        resetZoomButton.setOnClickListener(v -> weightLineChart.fitScreen());

        // initialize DatabaseHelper, GoalManager, NotificationHelper
        dbHelper = new DatabaseHelper(this);
        goalManager = new GoalManager(this); // handles goal weight logic
        notificationHelper = new NotificationHelper(this); // sends goal achieved SMS notifications

        // Firebase
        firebaseHelper = new FirebaseHelper();

        // retrieve and display goal weight
        loadGoalWeight();

        // set up RecyclerView
        weightHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        refreshWeightHistory();

        // button listeners
        dailyEntryButton.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Navigating to Daily Entry", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, DailyEntryActivity.class);
            startActivity(intent);
        });

        goalEntryButton.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Navigating to Set Goal", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, GoalEntryActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Logging out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        Button exportButton = findViewById(R.id.exportCsvButton);
        exportButton.setOnClickListener(v -> {
            int userId = getCurrentUserId();  // Make sure this method returns the right ID
            exportToCSV(userId);
        });




    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoalWeight();
        refreshWeightHistory();

        int userId = getCurrentUserId();

        // for testing
        // Cursor cursor = dbHelper.getUnsyncedEntries(userId);
        // Log.d("TestSync", "Unsynced local entries: " + cursor.getCount());
        // dbHelper.markAllEntriesAsUnsynced(userId);

        // Background Firebase sync
        Executors.newSingleThreadExecutor().execute(() -> {
            firebaseHelper.syncToFirebase(userId, dbHelper);
            firebaseHelper.syncFromFirebase(userId, dbHelper);

            // Once Firebase sync completes, refresh UI on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                refreshWeightHistory();
                setupWeightChart();
                checkAndNotifyGoalAchieved();
                displayWeightExtremes();
                displayStreakStats();
                displayPlateauStats();
            });
        });
    }

    private void checkAndNotifyGoalAchieved() {
        int currentUserId = getCurrentUserId();
        Cursor cursor = dbHelper.getLatestWeightEntry(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            int weightIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_WEIGHT);
            if (weightIndex != -1) {
                double latestWeight = cursor.getDouble(weightIndex);
                if (goalManager.isGoalAchieved(latestWeight, currentUserId)) {
                    Double goalWeight = goalManager.getGoalWeight(currentUserId);
                    if (goalWeight != null) {
                        notificationHelper.sendGoalAchievedSMS(latestWeight, goalWeight);
                    }
                }
            }
            cursor.close();
        } else {
            Log.e("HomeActivity", "No weight entries found for current user.");
        }
    }

    private void loadGoalWeight() {
        int currentUserId = getCurrentUserId(); // Get logged-in user ID
        Double goalWeight = goalManager.getGoalWeight(currentUserId);

        if (goalWeight != null) {
            goalWeightTextView.setText(getString(R.string.goal_weight_text, goalWeight));
        } else {
            goalWeightTextView.setText(R.string.set_goal_weight);
        }
    }

    private List<WeightEntry> getWeightEntriesFromDatabase() {
        List<WeightEntry> data = new ArrayList<>();
        int currentUserId = getCurrentUserId(); // Retrieve logged-in user's ID
        Cursor cursor = dbHelper.getAllWeightEntries(currentUserId);
        if (cursor != null) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_WEIGHT_ID);
            int dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE);
            int weightIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_WEIGHT);
            int userIdIndex = cursor.getColumnIndex("user_id");
            int lastUpdatedIndex = cursor.getColumnIndex("last_updated");

            if (idIndex == -1 || dateIndex == -1 || weightIndex == -1 || userIdIndex == -1 || lastUpdatedIndex == -1) {
                Log.e("HomeActivity", "Error: One or more column indices are invalid.");
                cursor.close();
                return data;
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(idIndex);
                String date = cursor.getString(dateIndex);
                double weight = cursor.getDouble(weightIndex);
                int userId = cursor.getInt(userIdIndex);
                long lastUpdated = cursor.getLong(lastUpdatedIndex);

                data.add(new WeightEntry(id, date, weight, userId, lastUpdated));
            }
            cursor.close();
        }
        return data;
    }

    private void refreshWeightHistory() {
        List<WeightEntry> weightEntries = getWeightEntriesFromDatabase();
        WeightHistoryAdapter adapter = new WeightHistoryAdapter(weightEntries,
                this::openEditDialog,
                this::confirmAndDeleteEntry
        );
        weightHistoryRecyclerView.setAdapter(adapter);
    }

    private void openEditDialog(int entryId, String oldDate, String oldWeight) {
        // create dialog for editing
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Entry");

        // inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_entry, null);
        EditText dateInput = dialogView.findViewById(R.id.editDateInput);
        EditText weightInput = dialogView.findViewById(R.id.editWeightInput);

        // set existing values
        String formattedDisplayDate = DateUtils.formatDateForDisplay(oldDate);
        dateInput.setText(formattedDisplayDate);
        weightInput.setText(oldWeight);

        builder.setView(dialogView);

        // save changes
        builder.setPositiveButton("Save", (dialog, which) -> {
            String displayDate = dateInput.getText().toString().trim();
            String newDate = DateUtils.formatDateForStorage(displayDate);
            String newWeightStr = weightInput.getText().toString().trim();

            if (!newDate.isEmpty() && !newWeightStr.isEmpty()) {
                try {
                    double newWeight = Double.parseDouble(newWeightStr);
                    int rowsUpdated = dbHelper.updateWeightEntry(entryId, newDate, newWeight);
                    if (rowsUpdated > 0) {
                        Toast.makeText(HomeActivity.this, "Entry updated successfully", Toast.LENGTH_SHORT).show();
                        refreshWeightHistory();
                        // firebase
                        int userId = getCurrentUserId();
                        firebaseHelper.uploadWeightEntry(userId, entryId, newDate, newWeight);
                    } else {
                        Toast.makeText(HomeActivity.this, "Failed to update entry", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(HomeActivity.this, "Invalid weight format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HomeActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // cancel
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void confirmAndDeleteEntry(int entryId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Entry");
        builder.setMessage("Are you sure you want to delete this entry?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            int rowsDeleted = dbHelper.deleteWeightEntry(entryId);
            if (rowsDeleted > 0) {
                Toast.makeText(HomeActivity.this, "Entry deleted successfully", Toast.LENGTH_SHORT).show();
                refreshWeightHistory();
                // firebase
                int userId = getCurrentUserId();
                firebaseHelper.deleteWeightEntry(userId, entryId);
            } else {
                Toast.makeText(HomeActivity.this, "Failed to delete entry", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied. Notifications will not be sent via SMS.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupWeightChart() {
        List<WeightEntry> weightEntries = getWeightEntriesFromDatabase();
        ArrayList<Entry> chartEntries = new ArrayList<>();
        ArrayList<String> dateLabels = new ArrayList<>();

        // Format dates for X-axis (MM/dd/yy)
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat chartFormat = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());

        for (int i = 0; i < weightEntries.size(); i++) {
            double weight = weightEntries.get(i).getWeight();
            chartEntries.add(new Entry(i, (float) weight));

            String rawDate = weightEntries.get(i).getDate();
            try {
                Date date = inputFormat.parse(rawDate);
                dateLabels.add(chartFormat.format(date));
            } catch (Exception e) {
                dateLabels.add(rawDate); // Fallback if parsing fails
            }
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(chartEntries, "Weight Over Time");
        dataSet.setColor(getResources().getColor(R.color.teal_700));
        dataSet.setValueTextColor(getResources().getColor(R.color.black));
        dataSet.setCircleColor(getResources().getColor(R.color.teal_700));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        // Apply dataset to chart
        LineData lineData = new LineData(dataSet);
        weightLineChart.setData(lineData);

        // Enable touch interaction, zooming, and panning
        weightLineChart.setTouchEnabled(true);
        weightLineChart.setDragEnabled(true);
        weightLineChart.setScaleEnabled(true);
        weightLineChart.setPinchZoom(true);
        weightLineChart.setDoubleTapToZoomEnabled(true);


        // Format X-axis with date labels
        XAxis xAxis = weightLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // one label per entry
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < dateLabels.size()) ? dateLabels.get(index) : "";
            }
        });

        // Add goal weight reference line
        Double goalWeight = goalManager.getGoalWeight(getCurrentUserId());
        if (goalWeight != null) {
            LimitLine goalLine = new LimitLine(goalWeight.floatValue(), "Goal");
            goalLine.setLineColor(Color.RED);
            goalLine.setLineWidth(2f);
            goalLine.setTextColor(Color.RED);
            goalLine.setTextSize(12f);

            YAxis leftAxis = weightLineChart.getAxisLeft();
            leftAxis.removeAllLimitLines(); // Remove old lines before adding new
            leftAxis.addLimitLine(goalLine);
        }

        // Final chart appearance settings
        weightLineChart.getDescription().setEnabled(false);
        weightLineChart.getLegend().setEnabled(false);
        weightLineChart.invalidate(); // Refresh chart
    }

    private void displayWeightExtremes() {
        WeightHeapUtil.MaxHeap maxHeap = new WeightHeapUtil.MaxHeap();
        WeightHeapUtil.MinHeap minHeap = new WeightHeapUtil.MinHeap();

        List<WeightEntry> entries = getWeightEntriesFromDatabase();
        for (WeightEntry entry : entries) {
            double weight = entry.getWeight();
            maxHeap.insert(weight);
            minHeap.insert(weight);
        }

        Double maxWeight = maxHeap.peek();
        Double minWeight = minHeap.peek();

        TextView maxWeightText = findViewById(R.id.maxWeightTextView);
        TextView minWeightText = findViewById(R.id.minWeightTextView);

        if (maxWeight != null && minWeight != null) {
            maxWeightText.setText("Max Weight: " + maxWeight + " lbs");
            minWeightText.setText("Min Weight: " + minWeight + " lbs");
        }
    }

    // Displays consecutive weight entry streaks and weight loss streaks to the user
    private void displayStreakStats() {
        // Retrieve all weight entries from the database
        List<WeightEntry> entries = getWeightEntriesFromDatabase();

        // Analyze and get longest streaks
        WeightTrendAnalyzer.StreakResult calendarStreak = WeightTrendAnalyzer.getLongestCalendarStreak(entries);
        WeightTrendAnalyzer.StreakResult downwardStreak = WeightTrendAnalyzer.getLongestDownwardStreak(entries);

        // TextView for weight entry streak
        TextView calendarStreakText = findViewById(R.id.calendarStreakTextView);
        TextView calendarStreakDatesText = findViewById(R.id.calendarStreakDatesTextView);

        // TextView for weight loss streak
        TextView downwardStreakText = findViewById(R.id.downwardStreakTextView);
        TextView downwardStreakDatesText = findViewById(R.id.downwardStreakDatesTextView);

        // Display entry streak if longer than one day
        if (calendarStreak.length > 1) {
            calendarStreakText.setText(getString(R.string.calendar_streak, calendarStreak.length));
            calendarStreakDatesText.setText("(" + DateUtils.formatDateForDisplay(calendarStreak.startDate)
                    + " to " + DateUtils.formatDateForDisplay(calendarStreak.endDate) + ")");
        } else {
            // Display default message if no streak is found
            calendarStreakText.setText(getString(R.string.calendar_streak, 0));
            calendarStreakDatesText.setText(getString(R.string.no_streak_yet));
        }

        // Display weight loss streak if longer than one day
        if (downwardStreak.length > 1) {
            downwardStreakText.setText(getString(R.string.downward_streak, downwardStreak.length));
            downwardStreakDatesText.setText("(" + DateUtils.formatDateForDisplay(downwardStreak.startDate)
                    + " to " + DateUtils.formatDateForDisplay(downwardStreak.endDate) + ")");
        } else {
            // Display default message if no streak is found
            downwardStreakText.setText(getString(R.string.downward_streak, 0));
            downwardStreakDatesText.setText(getString(R.string.no_streak_yet));
        }
    }

    // Displays longest period of time user experienced a plateau in their weight
    private void displayPlateauStats() {

        // Retrieve all weight entries from the database
        List<WeightEntry> entries = getWeightEntriesFromDatabase();

        // Analyze and get the longest plateau within 0.2 lbs variation
        WeightTrendAnalyzer.PlateauResult plateau = WeightTrendAnalyzer.getLongestPlateau(entries);

        // TextView for for plateau
        TextView plateauText = findViewById(R.id.plateauTextView);
        TextView plateauDatesText = findViewById(R.id.plateauDatesTextView);

        // Display plateau duration and date range if it exists
        if (plateau.length > 1) {
            String formattedStart = DateUtils.formatDateForDisplay(plateau.startDate);
            String formattedEnd = DateUtils.formatDateForDisplay(plateau.endDate);
            plateauText.setText(getString(R.string.longest_plateau, plateau.length));
            plateauDatesText.setText("(" + formattedStart + " to " + formattedEnd + ")");
        } else {
            // Display default message if no plateau is found
            plateauText.setText(getString(R.string.longest_plateau, 0));
            plateauDatesText.setText(getString(R.string.no_plateau_yet));
        }
    }

    private void exportToCSV(int userId) {
        Cursor cursor = dbHelper.getAllEntriesForUser(userId);

        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No data to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
        if (!exportDir.exists()) exportDir.mkdirs();

        String fileName = "weight_entries_user_" + userId + ".csv";
        File file = new File(exportDir, fileName);

        try {
            FileWriter writer = new FileWriter(file);
            writer.append("Entry ID,Date,Weight\n");

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                double weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight"));

                writer.append(id + "," + date + "," + weight + "\n");
            }

            writer.flush();
            writer.close();
            cursor.close();

            Toast.makeText(this, "Exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("CSVExport", "Export failed", e);
        }
    }

}
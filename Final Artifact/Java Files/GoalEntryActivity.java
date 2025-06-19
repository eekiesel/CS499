package com.example.weighttracking;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class GoalEntryActivity extends AppCompatActivity {

    private TextInputEditText goalWeightInput;
    @SuppressWarnings("FieldCanBeLocal")
    private MaterialButton saveButton, cancelButton, requestSmsPermissionButton;
    private static final int SMS_PERMISSION_CODE = 100;
    private GoalManager goalManager;
    private NotificationHelper notificationHelper;


    private int getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getInt("current_user_id", -1); // Default to -1 if not found
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_entry);

        // initialize helpers
        goalManager = new GoalManager(this);
        notificationHelper = new NotificationHelper(this);

        // initialize UI components
        goalWeightInput = findViewById(R.id.goalWeightInput);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        requestSmsPermissionButton = findViewById(R.id.requestSmsPermissionButton);

        // save button listener
        saveButton.setOnClickListener(v -> {
            String input = goalWeightInput.getText().toString().trim();
            if (TextUtils.isEmpty(input)) {
                Toast.makeText(this, "Please enter your goal weight", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    double goalWeight = Double.parseDouble(input);
                    int userId = getCurrentUserId();
                    if (userId != -1) {
                        long result = goalManager.setGoalWeight(goalWeight, userId);
                        if (result != -1) {
                            Toast.makeText(this, "Goal weight saved successfully!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to save goal weight. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Enter a valid weight", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // cancel button listener
        cancelButton.setOnClickListener(v -> finish());

        // SMS permission button listener
        requestSmsPermissionButton.setOnClickListener(v -> checkAndRequestSmsPermission());


    }

    // check and request SMS permission
    private void checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            notificationHelper.sendTestSms();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                notificationHelper.sendTestSms();
            } else {
                Toast.makeText(this, "SMS Permission Denied. Notifications will not be sent via SMS.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
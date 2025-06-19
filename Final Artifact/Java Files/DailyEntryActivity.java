package com.example.weighttracking;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class DailyEntryActivity extends AppCompatActivity {


    private EditText weightInput;
    @SuppressWarnings("FieldCanBeLocal")
    private Button saveButton, cancelButton;
    private DatabaseHelper dbHelper;
    private FirebaseHelper firebaseHelper;

    private int getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return sharedPreferences.getInt("current_user_id", -1);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_entry);

        // initialize UI components
        weightInput = findViewById(R.id.weightInput);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        // initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // initialize FirebaseHelper
        firebaseHelper = new FirebaseHelper();

        // save button listener
        saveButton.setOnClickListener(v -> {
            String weight = weightInput.getText().toString().trim();
            // get current date
            String date = java.time.LocalDate.now().toString();
            // get user id
            int currentUserId = getCurrentUserId();

            if (TextUtils.isEmpty(weight)) {
                Toast.makeText(DailyEntryActivity.this, "Enter your weight", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    double weightValue = Double.parseDouble(weight);
                    long result = dbHelper.addWeightEntry(date, weightValue, currentUserId);
                    if (result != -1) {
                        Toast.makeText(DailyEntryActivity.this, "Weight saved successfully!", Toast.LENGTH_SHORT).show();
                        // firebase
                        firebaseHelper.uploadWeightEntry(currentUserId, (int) result, date, weightValue);
                        finish();
                    } else {
                        Toast.makeText(DailyEntryActivity.this, "Failed to save weight. Try again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(DailyEntryActivity.this, "Enter a valid weight", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // cancel button listener
        cancelButton.setOnClickListener(v -> finish());

    }
}
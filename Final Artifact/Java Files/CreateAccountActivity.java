package com.example.weighttracking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    @SuppressWarnings("FieldCanBeLocal")
    private Button createAccountButton, cancelButton;

    // database helper instance
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // initialize UI components
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        createAccountButton = findViewById(R.id.createAccountButton);
        cancelButton = findViewById(R.id.cancelButton);

        // initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // create account button listener
        createAccountButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // validate input fields
            if (TextUtils.isEmpty(username) || username.length() < 4) {
                Toast.makeText(CreateAccountActivity.this, "Username must be at least 4 characters", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(password) || password.length() < 6) {
                Toast.makeText(CreateAccountActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else if (dbHelper.checkUser(username)) {
                Toast.makeText(CreateAccountActivity.this, "Username already exists. Choose a different username.", Toast.LENGTH_SHORT).show();
            } else {
                // add new user to the database
                long result = dbHelper.addUser(username, password);
                if (result != -1) {
                    Toast.makeText(CreateAccountActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                    // redirect to MainActivity (login screen)
                    Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateAccountActivity.this, "Failed to create account. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Cancel button listener
        cancelButton.setOnClickListener(v -> {
            // Redirect to MainActivity (login screen)
            Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
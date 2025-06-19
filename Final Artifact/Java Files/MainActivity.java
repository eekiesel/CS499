package com.example.weighttracking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    @SuppressWarnings("FieldCanBeLocal")
    private Button loginButton, createAccountButton;
    private UserManager userManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // UI components
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);

        // initialize UserManager
        userManager = new UserManager(this);

        // login button listener
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // username and password cannot be blank
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Username and Password are required", Toast.LENGTH_SHORT).show();
            } else {
                // check credentials in database
                if (userManager.validateLogin(username, password)) {
                    int userId = userManager.getUserId(username);
                    SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("current_user_id", userId);
                    editor.apply();

                    Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    // navigate to home screen
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid Login information. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // create account button click listener
        createAccountButton.setOnClickListener(v -> {
            // Navigate to CreateAccountActivity
            Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

    }
}
package com.example.weighttracking;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class NotificationHelper {

    private final Context context;
    private static final String TAG = "NotificationHelper";

    public NotificationHelper(Context context) {
        this.context = context;
    }

    // sends goal achieved SMS notification to user
    public void sendGoalAchievedSMS(double currentWeight, double goalWeight) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {

            try {
                SmsManager smsManager = SmsManager.getDefault();
                String phoneNumber = "+15551234567"; // emulator's phone number
                String message = "Congratulations! You've achieved your goal weight of " + goalWeight +
                        " lbs. Current weight: " + currentWeight + " lbs.";

                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(context, "Goal weight notification sent via SMS", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.e(TAG, "Error sending SMS notification", e);
                Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(context, "Goal achieved! SMS permission is not granted.", Toast.LENGTH_SHORT).show();
        }
    }

    // send test SMS
    public void sendTestSms() {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String phoneNumber = "+15551234567";
            String message = "You've enabled SMS notifications.";
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("NotificationHelper", "Failed to send SMS", e);
            Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show();
        }
    }

}

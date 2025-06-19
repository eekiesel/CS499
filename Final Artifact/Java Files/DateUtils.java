package com.example.weighttracking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    // Converts "yyyy-MM-dd" to "MM/dd/yyyy" for display
    public static String formatDateForDisplay(String rawDate) {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date date = dbFormat.parse(rawDate);
            return displayFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return rawDate; // fallback to raw string if parsing fails
        }
    }

    // Converts "MM/dd/yyyy" back to "yyyy-MM-dd"
    public static String formatDateForStorage(String displayDate) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = displayFormat.parse(displayDate);
            return dbFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return displayDate; // fallback if parse fails
        }
    }


}

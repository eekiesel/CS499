package com.example.weighttracking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightTrendAnalyzer {

    // Finds the longest streak of consecutive entries where the weight decreases each day
    public static StreakResult getLongestDownwardStreak(List<WeightEntry> entries) {
        if (entries.size() < 2) return new StreakResult(0, "", "");

        // Sort entries by date to ensure chronological order
        entries.sort((e1, e2) -> e1.getDate().compareTo(e2.getDate()));

        int maxStreak = 1;
        int currentStreak = 1;
        String streakStart = entries.get(0).getDate();
        String currentStart = streakStart;
        String streakEnd = entries.get(0).getDate();

        // Loop through entries and compare each weight to the previous one
        for (int i = 1; i < entries.size(); i++) {
            if (entries.get(i).getWeight() < entries.get(i - 1).getWeight()) {
                // Continue the streak
                currentStreak++;
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak;
                    streakStart = currentStart;
                    streakEnd = entries.get(i).getDate();
                }
            } else {
                // Reset the streak if the weight doesn't decrease
                currentStreak = 1;
                currentStart = entries.get(i).getDate();
            }
        }

        if (maxStreak > 1) {
            return new StreakResult(maxStreak, streakStart, streakEnd);
        } else {
            return new StreakResult(0, "", "");
        }
    }

    // Finds the longest plateau: a streak of entries where weight stays within 0.2 lbs
    public static PlateauResult getLongestPlateau(List<WeightEntry> entries) {
        if (entries.size() < 2) return new PlateauResult(0, "", "");

        // Sort entries by date to ensure chronological order
        entries.sort((e1, e2) -> e1.getDate().compareTo(e2.getDate()));

        int longest = 1;
        int current = 1;
        int startIndex = 0;
        int currentStartIndex = 0;

        // Loop through and compare weights within ±0.2 lbs
        for (int i = 1; i < entries.size(); i++) {
            double prev = entries.get(i - 1).getWeight();
            double currWeight = entries.get(i).getWeight();
            if (Math.abs(currWeight - prev) <= 0.2) {
                // Continue plateau
                current++;
                if (current > longest) {
                    longest = current;
                    startIndex = currentStartIndex;
                }
            } else {
                // Reset if difference > 0.2
                current = 1;
                currentStartIndex = i;
            }
        }

        // Extract dates for longest plateau if valid
        if (longest > 1 && startIndex + longest <= entries.size()) {
            String startDate = entries.get(startIndex).getDate();
            String endDate = entries.get(startIndex + longest - 1).getDate();
            return new PlateauResult(longest, startDate, endDate);
        }

        return new PlateauResult(0, "", "");
    }

    // Finds the longest streak of consecutive calendar days with a logged weight entry
    public static StreakResult getLongestCalendarStreak(List<WeightEntry> entries) {
        if (entries.size() < 2) return new StreakResult(0, "", "");

        // Sort entries by date to ensure chronological order
        entries.sort((e1, e2) -> e1.getDate().compareTo(e2.getDate()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        int maxStreak = 1;
        int currentStreak = 1;
        String streakStart = entries.get(0).getDate();
        String currentStart = streakStart;
        String streakEnd = entries.get(0).getDate();

        for (int i = 1; i < entries.size(); i++) {
            try {
                Date prev = sdf.parse(entries.get(i - 1).getDate());
                Date curr = sdf.parse(entries.get(i).getDate());

                // Calculate difference in days between entries
                long diff = (curr.getTime() - prev.getTime()) / (1000 * 60 * 60 * 24);

                if (diff == 1) {
                    // If difference is 1 day, extend the streak
                    currentStreak++;
                    if (currentStreak > maxStreak) {
                        maxStreak = currentStreak;
                        streakStart = currentStart;
                        streakEnd = entries.get(i).getDate();
                    }
                } else {
                    // Reset streak if the day gap is larger
                    currentStreak = 1;
                    currentStart = entries.get(i).getDate();
                }
            } catch (Exception e) {
                // Handle invalid date formats
                e.printStackTrace();
            }
        }

        return new StreakResult(maxStreak, streakStart, streakEnd);
    }

    // Helper class to store the result of a streak calculation
    public static class StreakResult {
        public final int length;
        public final String startDate;
        public final String endDate;

        public StreakResult(int length, String startDate, String endDate) {
            this.length = length;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    // Helper class to store plateau calculation results
    public static class PlateauResult {
        public final int length;
        public final String startDate;
        public final String endDate;

        public PlateauResult(int length, String startDate, String endDate) {
            this.length = length;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

}

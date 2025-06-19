package com.example.weighttracking;

public class WeightEntry {
    private final int id;
    private final String date;
    private final double weight;
    private final int userId;
    private final long lastUpdated;

    public WeightEntry(int id, String date, double weight, int userId, long lastUpdated) {
        this.id = id;
        this.date = date;
        this.weight = weight;
        this.userId = userId;
        this.lastUpdated = lastUpdated;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public double getWeight() {
        return weight;
    }

    public int getUserId() {
        return userId;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
package com.hamsa.fittrack.activity;

/**
 * Supported activity types with their MET (Metabolic Equivalent of Task) value.
 * MET values are approximate averages from the Compendium of Physical Activities.
 */
public enum ActivityType {
    RUNNING(9.8),
    WALKING(3.5),
    CYCLING(7.5),
    SWIMMING(8.0),
    YOGA(2.5),
    WEIGHT_TRAINING(5.0),
    HIIT(8.0);

    private final double met;

    ActivityType(double met) {
        this.met = met;
    }

    public double getMet() {
        return met;
    }
}

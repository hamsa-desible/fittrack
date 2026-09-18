package com.hamsa.fittrack.activity;

import org.springframework.stereotype.Component;

/**
 * Estimates calories burned with the standard MET formula:
 * calories = MET x body weight (kg) x duration (hours).
 */
@Component
public class CalorieCalculator {

    public int estimate(ActivityType type, double weightKg, int durationMinutes) {
        if (weightKg <= 0 || durationMinutes <= 0) {
            throw new IllegalArgumentException("Weight and duration must be positive");
        }
        return (int) Math.round(type.getMet() * weightKg * (durationMinutes / 60.0));
    }
}

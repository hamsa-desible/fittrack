package com.hamsa.fittrack.stats;

import com.hamsa.fittrack.activity.ActivityType;

import java.time.LocalDate;
import java.util.List;

public record StatsResponse(
        Long userId,
        LocalDate from,
        LocalDate to,
        int totalActivities,
        int totalMinutes,
        int totalCalories,
        double totalDistanceKm,
        int currentStreakDays,
        int longestStreakDays,
        List<TypeBreakdown> byType) {

    public record TypeBreakdown(ActivityType type, int count, int minutes, int calories) {
    }
}

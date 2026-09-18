package com.hamsa.fittrack.stats;

import java.time.LocalDate;
import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Calculates workout streaks (consecutive days with at least one activity).
 *
 * Dates are put into a TreeSet, which removes duplicates (several workouts on the
 * same day count once) and keeps them sorted, so both streaks can be found in a
 * single pass. Time complexity: O(n log n) for building the set, O(n) for the scan.
 */
public final class StreakCalculator {

    private StreakCalculator() {
    }

    /** Longest run of consecutive active days ever. */
    public static int longestStreak(Collection<LocalDate> activityDates) {
        NavigableSet<LocalDate> days = new TreeSet<>(activityDates);
        int longest = 0;
        int current = 0;
        LocalDate previous = null;
        for (LocalDate day : days) {
            current = (previous != null && previous.plusDays(1).equals(day)) ? current + 1 : 1;
            longest = Math.max(longest, current);
            previous = day;
        }
        return longest;
    }

    /**
     * Current streak ending today. If there is no activity today yet, a streak that
     * ended yesterday still counts, because the user can still continue it today.
     */
    public static int currentStreak(Collection<LocalDate> activityDates, LocalDate today) {
        NavigableSet<LocalDate> days = new TreeSet<>(activityDates);
        LocalDate day = days.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (days.contains(day)) {
            streak++;
            day = day.minusDays(1);
        }
        return streak;
    }
}

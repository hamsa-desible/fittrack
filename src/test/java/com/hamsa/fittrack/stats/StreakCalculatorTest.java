package com.hamsa.fittrack.stats;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StreakCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 18);

    private static LocalDate daysAgo(int days) {
        return TODAY.minusDays(days);
    }

    @Test
    void emptyHistoryHasNoStreaks() {
        assertThat(StreakCalculator.longestStreak(List.of())).isZero();
        assertThat(StreakCalculator.currentStreak(List.of(), TODAY)).isZero();
    }

    @Test
    void multipleWorkoutsOnSameDayCountOnce() {
        List<LocalDate> dates = List.of(TODAY, TODAY, daysAgo(1));
        assertThat(StreakCalculator.currentStreak(dates, TODAY)).isEqualTo(2);
        assertThat(StreakCalculator.longestStreak(dates)).isEqualTo(2);
    }

    @Test
    void findsLongestRunEvenWhenInThePast() {
        // 4-day run long ago, gap, then a 2-day run ending today
        List<LocalDate> dates = List.of(daysAgo(10), daysAgo(9), daysAgo(8), daysAgo(7),
                daysAgo(1), TODAY);
        assertThat(StreakCalculator.longestStreak(dates)).isEqualTo(4);
        assertThat(StreakCalculator.currentStreak(dates, TODAY)).isEqualTo(2);
    }

    @Test
    void streakEndingYesterdayIsStillCurrent() {
        List<LocalDate> dates = List.of(daysAgo(3), daysAgo(2), daysAgo(1));
        assertThat(StreakCalculator.currentStreak(dates, TODAY)).isEqualTo(3);
    }

    @Test
    void streakIsBrokenAfterAMissedDay() {
        List<LocalDate> dates = List.of(daysAgo(4), daysAgo(3), daysAgo(2));
        assertThat(StreakCalculator.currentStreak(dates, TODAY)).isZero();
    }

    @Test
    void worksWithUnsortedInput() {
        List<LocalDate> dates = List.of(daysAgo(1), daysAgo(3), daysAgo(2), TODAY);
        assertThat(StreakCalculator.longestStreak(dates)).isEqualTo(4);
    }
}

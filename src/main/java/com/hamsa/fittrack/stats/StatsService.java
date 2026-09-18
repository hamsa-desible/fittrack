package com.hamsa.fittrack.stats;

import com.hamsa.fittrack.activity.Activity;
import com.hamsa.fittrack.activity.ActivityRepository;
import com.hamsa.fittrack.activity.ActivityType;
import com.hamsa.fittrack.stats.StatsResponse.TypeBreakdown;
import com.hamsa.fittrack.user.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final ActivityRepository activityRepository;
    private final UserService userService;
    private final Clock clock;

    public StatsService(ActivityRepository activityRepository, UserService userService, Clock clock) {
        this.activityRepository = activityRepository;
        this.userService = userService;
        this.clock = clock;
    }

    /**
     * Aggregating activities is the most expensive read in the app, so results are cached
     * (Caffeine, 10 minute TTL). Any activity write evicts the cache.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userStats", key = "#userId + ':' + #from + ':' + #to")
    public StatsResponse getStats(Long userId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must not be after 'to' date");
        }
        userService.getUser(userId);

        List<Activity> activities = activityRepository.findByUserIdAndPerformedAtBetween(
                userId, from.atStartOfDay(), to.plusDays(1).atStartOfDay().minusNanos(1));

        int totalMinutes = 0;
        int totalCalories = 0;
        double totalDistance = 0;
        Map<ActivityType, int[]> perType = new EnumMap<>(ActivityType.class); // [count, minutes, calories]
        for (Activity activity : activities) {
            totalMinutes += activity.getDurationMinutes();
            totalCalories += activity.getCaloriesBurned();
            if (activity.getDistanceKm() != null) {
                totalDistance += activity.getDistanceKm();
            }
            int[] sums = perType.computeIfAbsent(activity.getType(), t -> new int[3]);
            sums[0]++;
            sums[1] += activity.getDurationMinutes();
            sums[2] += activity.getCaloriesBurned();
        }

        List<TypeBreakdown> byType = perType.entrySet().stream()
                .map(e -> new TypeBreakdown(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]))
                .sorted(Comparator.comparingInt(TypeBreakdown::minutes).reversed())
                .toList();

        // Streaks are calculated over the user's whole history, not just the selected range
        List<LocalDate> allDates = activityRepository.findActivityTimesByUserId(userId).stream()
                .map(LocalDateTime::toLocalDate)
                .toList();

        return new StatsResponse(userId, from, to, activities.size(), totalMinutes, totalCalories,
                Math.round(totalDistance * 100) / 100.0,
                StreakCalculator.currentStreak(allDates, LocalDate.now(clock)),
                StreakCalculator.longestStreak(allDates),
                byType);
    }
}

package com.hamsa.fittrack.activity;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ActivityDtos {

    private ActivityDtos() {
    }

    /**
     * caloriesBurned is optional - when it is missing we estimate it from the
     * activity type, duration and the user's weight.
     * performedAt is optional - it defaults to the current time.
     */
    public record ActivityRequest(
            @NotNull ActivityType type,
            @NotNull @Min(1) @Max(1440) Integer durationMinutes,
            @DecimalMin("0.0") @DecimalMax("1000.0") Double distanceKm,
            @Min(0) @Max(20000) Integer caloriesBurned,
            @PastOrPresent LocalDateTime performedAt,
            @Size(max = 500) String notes) {
    }

    public record ActivityResponse(Long id, Long userId, ActivityType type, int durationMinutes,
                                   Double distanceKm, int caloriesBurned, LocalDateTime performedAt,
                                   String notes) {

        public static ActivityResponse from(Activity activity) {
            return new ActivityResponse(activity.getId(), activity.getUser().getId(), activity.getType(),
                    activity.getDurationMinutes(), activity.getDistanceKm(), activity.getCaloriesBurned(),
                    activity.getPerformedAt(), activity.getNotes());
        }
    }
}

package com.hamsa.fittrack.activity;

import com.hamsa.fittrack.activity.ActivityDtos.ActivityRequest;
import com.hamsa.fittrack.activity.ActivityDtos.ActivityResponse;
import com.hamsa.fittrack.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/users/{userId}/activities")
    @Operation(summary = "Log a workout. Calories are estimated automatically if not provided")
    public ResponseEntity<ActivityResponse> logActivity(@PathVariable Long userId,
                                                        @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.logActivity(userId, request));
    }

    @GetMapping("/users/{userId}/activities")
    @Operation(summary = "List a user's activities, newest first, optionally filtered by type")
    public PageResponse<ActivityResponse> getActivities(@PathVariable Long userId,
                                                        @RequestParam(required = false) ActivityType type,
                                                        @RequestParam(defaultValue = "0") @Min(0) int page,
                                                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return activityService.getActivities(userId, type, page, size);
    }

    @GetMapping("/activities/{id}")
    @Operation(summary = "Get a single activity")
    public ActivityResponse getActivity(@PathVariable Long id) {
        return activityService.getActivity(id);
    }

    @PutMapping("/activities/{id}")
    @Operation(summary = "Update an activity")
    public ActivityResponse updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        return activityService.updateActivity(id, request);
    }

    @DeleteMapping("/activities/{id}")
    @Operation(summary = "Delete an activity")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }
}

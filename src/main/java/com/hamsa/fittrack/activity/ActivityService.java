package com.hamsa.fittrack.activity;

import com.hamsa.fittrack.activity.ActivityDtos.ActivityRequest;
import com.hamsa.fittrack.activity.ActivityDtos.ActivityResponse;
import com.hamsa.fittrack.common.PageResponse;
import com.hamsa.fittrack.common.ResourceNotFoundException;
import com.hamsa.fittrack.user.User;
import com.hamsa.fittrack.user.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserService userService;
    private final CalorieCalculator calorieCalculator;
    private final Clock clock;

    public ActivityService(ActivityRepository activityRepository, UserService userService,
                           CalorieCalculator calorieCalculator, Clock clock) {
        this.activityRepository = activityRepository;
        this.userService = userService;
        this.calorieCalculator = calorieCalculator;
        this.clock = clock;
    }

    /** Stats are cached, so every write must evict them to keep the numbers correct. */
    @Transactional
    @CacheEvict(value = "userStats", allEntries = true)
    public ActivityResponse logActivity(Long userId, ActivityRequest request) {
        User user = userService.getUser(userId);
        Activity activity = new Activity(user, request.type(), request.durationMinutes(), request.distanceKm(),
                resolveCalories(request, user), resolvePerformedAt(request), request.notes());
        return ActivityResponse.from(activityRepository.save(activity));
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityResponse> getActivities(Long userId, ActivityType type, int page, int size) {
        userService.getUser(userId); // 404 if the user does not exist
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
        Page<Activity> result = type == null
                ? activityRepository.findByUserId(userId, pageable)
                : activityRepository.findByUserIdAndType(userId, type, pageable);
        return PageResponse.from(result, ActivityResponse::from);
    }

    @Transactional(readOnly = true)
    public ActivityResponse getActivity(Long id) {
        return ActivityResponse.from(findActivity(id));
    }

    @Transactional
    @CacheEvict(value = "userStats", allEntries = true)
    public ActivityResponse updateActivity(Long id, ActivityRequest request) {
        Activity activity = findActivity(id);
        activity.update(request.type(), request.durationMinutes(), request.distanceKm(),
                resolveCalories(request, activity.getUser()), resolvePerformedAt(request), request.notes());
        return ActivityResponse.from(activity);
    }

    @Transactional
    @CacheEvict(value = "userStats", allEntries = true)
    public void deleteActivity(Long id) {
        activityRepository.delete(findActivity(id));
    }

    private Activity findActivity(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", id));
    }

    private int resolveCalories(ActivityRequest request, User user) {
        if (request.caloriesBurned() != null) {
            return request.caloriesBurned();
        }
        return calorieCalculator.estimate(request.type(), user.getWeightKg(), request.durationMinutes());
    }

    private LocalDateTime resolvePerformedAt(ActivityRequest request) {
        return request.performedAt() != null ? request.performedAt() : LocalDateTime.now(clock);
    }
}

package com.hamsa.fittrack.activity;

import com.hamsa.fittrack.activity.ActivityDtos.ActivityRequest;
import com.hamsa.fittrack.activity.ActivityDtos.ActivityResponse;
import com.hamsa.fittrack.common.ResourceNotFoundException;
import com.hamsa.fittrack.user.User;
import com.hamsa.fittrack.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-09-18T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserService userService;

    private ActivityService activityService;
    private User user;

    @BeforeEach
    void setUp() {
        activityService = new ActivityService(activityRepository, userService, new CalorieCalculator(), FIXED_CLOCK);
        user = new User("Asha", "asha@example.com", 60);
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    void estimatesCaloriesAndDefaultsTimeWhenNotProvided() {
        when(userService.getUser(1L)).thenReturn(user);
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> inv.getArgument(0));

        ActivityResponse response = activityService.logActivity(1L,
                new ActivityRequest(ActivityType.CYCLING, 60, 20.0, null, null, "Evening ride"));

        // 7.5 MET x 60 kg x 1 h
        assertThat(response.caloriesBurned()).isEqualTo(450);
        assertThat(response.performedAt()).isEqualTo(LocalDateTime.of(2026, 9, 18, 10, 0));
        assertThat(response.userId()).isEqualTo(1L);
    }

    @Test
    void keepsCaloriesWhenProvidedByClient() {
        when(userService.getUser(1L)).thenReturn(user);
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> inv.getArgument(0));

        ActivityResponse response = activityService.logActivity(1L,
                new ActivityRequest(ActivityType.RUNNING, 30, 5.0, 400, null, null));

        assertThat(response.caloriesBurned()).isEqualTo(400);
    }

    @Test
    void failsWhenUserDoesNotExist() {
        when(userService.getUser(99L)).thenThrow(new ResourceNotFoundException("User", 99L));

        assertThatThrownBy(() -> activityService.logActivity(99L,
                new ActivityRequest(ActivityType.YOGA, 30, null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(activityRepository, never()).save(any());
    }

    @Test
    void deleteFailsForUnknownActivity() {
        when(activityRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityService.deleteActivity(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

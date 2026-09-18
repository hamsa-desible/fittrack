package com.hamsa.fittrack.config;

import com.hamsa.fittrack.activity.Activity;
import com.hamsa.fittrack.activity.ActivityRepository;
import com.hamsa.fittrack.activity.ActivityType;
import com.hamsa.fittrack.activity.CalorieCalculator;
import com.hamsa.fittrack.user.User;
import com.hamsa.fittrack.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Seeds a demo user with three weeks of workouts so the live demo is not empty. */
@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private static final ActivityType[] PLAN = {
            ActivityType.RUNNING, ActivityType.WEIGHT_TRAINING, ActivityType.CYCLING,
            ActivityType.YOGA, ActivityType.RUNNING, ActivityType.SWIMMING, ActivityType.WALKING
    };

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final CalorieCalculator calorieCalculator;
    private final Clock clock;

    public DemoDataLoader(UserRepository userRepository, ActivityRepository activityRepository,
                          CalorieCalculator calorieCalculator, Clock clock) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.calorieCalculator = calorieCalculator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        User demo = userRepository.save(new User("Demo User", "demo@fittrack.dev", 70));
        LocalDate today = LocalDate.now(clock);
        List<Activity> activities = new ArrayList<>();
        for (int daysAgo = 20; daysAgo >= 1; daysAgo--) {
            if (daysAgo == 12 || daysAgo == 8) {
                continue; // rest days, so the streak numbers are interesting
            }
            ActivityType type = PLAN[daysAgo % PLAN.length];
            int minutes = 30 + (daysAgo % 4) * 10;
            Double distance = switch (type) {
                case RUNNING -> minutes / 6.0;
                case CYCLING -> minutes / 2.5;
                case WALKING -> minutes / 12.0;
                case SWIMMING -> minutes / 30.0;
                default -> null;
            };
            activities.add(new Activity(demo, type, minutes,
                    distance == null ? null : Math.round(distance * 10) / 10.0,
                    calorieCalculator.estimate(type, demo.getWeightKg(), minutes),
                    today.minusDays(daysAgo).atTime(7, 0), null));
        }
        activityRepository.saveAll(activities);
        log.info("Seeded demo user with {} activities", activities.size());
    }
}

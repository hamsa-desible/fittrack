package com.hamsa.fittrack.activity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalorieCalculatorTest {

    private final CalorieCalculator calculator = new CalorieCalculator();

    @Test
    void estimatesCaloriesUsingMetFormula() {
        // 9.8 MET x 70 kg x 0.5 h = 343
        assertThat(calculator.estimate(ActivityType.RUNNING, 70, 30)).isEqualTo(343);
    }

    @Test
    void roundsToNearestCalorie() {
        // 2.5 MET x 65 kg x (45 / 60) h = 121.875
        assertThat(calculator.estimate(ActivityType.YOGA, 65, 45)).isEqualTo(122);
    }

    @Test
    void rejectsNonPositiveInput() {
        assertThatThrownBy(() -> calculator.estimate(ActivityType.WALKING, 0, 30))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculator.estimate(ActivityType.WALKING, 70, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

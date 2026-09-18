package com.hamsa.fittrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /** Injecting a Clock (instead of calling LocalDate.now() directly) makes date logic testable. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("FitTrack API")
                .version("1.0")
                .description("REST API for logging workouts, estimating calories burned and tracking "
                        + "progress with stats and streaks."));
    }
}

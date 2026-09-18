package com.hamsa.fittrack.stats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

@RestController
@Tag(name = "Stats")
public class StatsController {

    private static final int DEFAULT_RANGE_DAYS = 30;

    private final StatsService statsService;
    private final Clock clock;

    public StatsController(StatsService statsService, Clock clock) {
        this.statsService = statsService;
        this.clock = clock;
    }

    @GetMapping("/api/users/{userId}/stats")
    @Operation(summary = "Workout summary for a date range (defaults to the last 30 days), plus streaks")
    public StatsResponse getStats(@PathVariable Long userId,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        // Defaults are resolved here so the cache key always contains real dates
        LocalDate end = to != null ? to : LocalDate.now(clock);
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_RANGE_DAYS - 1);
        return statsService.getStats(userId, start, end);
    }
}

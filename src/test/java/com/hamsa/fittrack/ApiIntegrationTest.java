package com.hamsa.fittrack;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end tests through the HTTP layer, using the in-memory H2 database. */
@SpringBootTest(properties = "app.demo-data.enabled=false")
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private long createUser(String email) throws Exception {
        String body = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Test User", "email": "%s", "weightKg": 80}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static String uniqueEmail() {
        return UUID.randomUUID() + "@example.com";
    }

    @Test
    void logActivitiesAndGetStats() throws Exception {
        long userId = createUser(uniqueEmail());
        String yesterday = LocalDate.now().minusDays(1) + "T07:30:00";

        mockMvc.perform(post("/api/users/{id}/activities", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "RUNNING", "durationMinutes": 30, "distanceKm": 5.2, "performedAt": "%s"}
                                """.formatted(yesterday)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.caloriesBurned").value(392)); // 9.8 x 80 x 0.5

        mockMvc.perform(post("/api/users/{id}/activities", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "YOGA", "durationMinutes": 60, "caloriesBurned": 150}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/{id}/activities", userId).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].type").value("YOGA")) // newest first
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/users/{id}/activities", userId).param("type", "RUNNING"))
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/users/{id}/stats", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActivities").value(2))
                .andExpect(jsonPath("$.totalMinutes").value(90))
                .andExpect(jsonPath("$.totalCalories").value(542))
                .andExpect(jsonPath("$.totalDistanceKm").value(5.2))
                .andExpect(jsonPath("$.currentStreakDays").value(2))
                .andExpect(jsonPath("$.byType[0].type").value("YOGA"));
    }

    @Test
    void statsCacheIsEvictedWhenActivityIsAdded() throws Exception {
        long userId = createUser(uniqueEmail());

        mockMvc.perform(get("/api/users/{id}/stats", userId))
                .andExpect(jsonPath("$.totalActivities").value(0));

        mockMvc.perform(post("/api/users/{id}/activities", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "WALKING", "durationMinutes": 20}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/{id}/stats", userId))
                .andExpect(jsonPath("$.totalActivities").value(1));
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        String email = uniqueEmail();
        createUser(email);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Someone Else", "email": "%s", "weightKg": 70}
                                """.formatted(email.toUpperCase())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate resource"));
    }

    @Test
    void returnsFieldErrorsForInvalidInput() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "email": "not-an-email", "weightKg": 5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.weightKg").exists());
    }

    @Test
    void rejectsUnknownActivityType() throws Exception {
        long userId = createUser(uniqueEmail());
        mockMvc.perform(post("/api/users/{id}/activities", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "BOXING", "durationMinutes": 10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed request body"));
    }

    @Test
    void returns404ForUnknownUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}/stats", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User with id 999999 was not found"));
    }

    @Test
    void rejectsInvalidDateRange() throws Exception {
        long userId = createUser(uniqueEmail());
        mockMvc.perform(get("/api/users/{id}/stats", userId)
                        .param("from", "2026-09-10").param("to", "2026-09-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletingUserRemovesTheirActivities() throws Exception {
        long userId = createUser(uniqueEmail());
        String body = mockMvc.perform(post("/api/users/{id}/activities", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "HIIT", "durationMinutes": 25}
                                """))
                .andReturn().getResponse().getContentAsString();
        long activityId = ((Number) JsonPath.read(body, "$.id")).longValue();

        mockMvc.perform(delete("/api/users/{id}", userId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/activities/{id}", activityId)).andExpect(status().isNotFound());
    }
}

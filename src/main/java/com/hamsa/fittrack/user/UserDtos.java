package com.hamsa.fittrack.user;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class UserDtos {

    private UserDtos() {
    }

    public record CreateUserRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @DecimalMin("20.0") @DecimalMax("300.0") double weightKg) {
    }

    public record UserResponse(Long id, String name, String email, double weightKg, Instant createdAt) {

        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                    user.getWeightKg(), user.getCreatedAt());
        }
    }
}

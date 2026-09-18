package com.hamsa.fittrack.activity;

import com.hamsa.fittrack.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities",
        indexes = @Index(name = "idx_activity_user_time", columnList = "user_id, performed_at"))
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType type;

    @Column(nullable = false)
    private int durationMinutes;

    private Double distanceKm;

    @Column(nullable = false)
    private int caloriesBurned;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Activity() {
        // required by JPA
    }

    public Activity(User user, ActivityType type, int durationMinutes, Double distanceKm,
                    int caloriesBurned, LocalDateTime performedAt, String notes) {
        this.user = user;
        update(type, durationMinutes, distanceKm, caloriesBurned, performedAt, notes);
    }

    public final void update(ActivityType type, int durationMinutes, Double distanceKm,
                             int caloriesBurned, LocalDateTime performedAt, String notes) {
        this.type = type;
        this.durationMinutes = durationMinutes;
        this.distanceKm = distanceKm;
        this.caloriesBurned = caloriesBurned;
        this.performedAt = performedAt;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public ActivityType getType() { return type; }
    public int getDurationMinutes() { return durationMinutes; }
    public Double getDistanceKm() { return distanceKm; }
    public int getCaloriesBurned() { return caloriesBurned; }
    public LocalDateTime getPerformedAt() { return performedAt; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
}

package com.hamsa.fittrack.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Body weight is needed to estimate calories burned (MET formula). */
    @Column(nullable = false)
    private double weightKg;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected User() {
        // required by JPA
    }

    public User(String name, String email, double weightKg) {
        this.name = name;
        this.email = email;
        this.weightKg = weightKg;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public double getWeightKg() { return weightKg; }
    public Instant getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }
}

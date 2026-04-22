package com.xebec.BusTracking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA Entity for Buses table.
 * Represents the bus fleet
 */
@Entity
@Table(name = "buses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_number", unique = true, nullable = false, length = 20)
    private String registrationNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Column(length = 50)
    private String make;

    @Column(length = 50)
    private String model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    @Enumerated(EnumType.STRING)
    private BusStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Assign driver to bus
     */
    public void assignDriver(User driver) {
        if (driver != null && driver.isDriver()) {
            this.driver = driver;
        } else {
            throw new IllegalArgumentException("User must have DRIVER role");
        }
    }

    /**
     * Check if the bus is active
     */
    public boolean isActive() {
        return BusStatus.ACTIVE.equals(this.status);
    }

    /**
     * Check if bus is in maintenance
     */
    public boolean isInMaintenance() {
        return BusStatus.MAINTENANCE.equals(this.status);
    }

    /**
     * Check if the bus has a driver
     */
    public boolean hasDriver() {
        return this.driver != null;
    }

    /**
     * Activate the bus
     */
    public void activate() {
        this.status = BusStatus.ACTIVE;
    }

    /**
     * Deactivate the bus
     */
    public void deactivate() {
        this.status = BusStatus.INACTIVE;
    }

    /**
     * Set bus to maintenance mode
     */
    public void setMaintenance() {
        this.status = BusStatus.MAINTENANCE;
    }

    /**
     * Remove driver assignment
     */
    public void removeDriver() {
        this.driver = null;
    }
}

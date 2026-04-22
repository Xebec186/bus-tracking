package com.xebec.BusTracking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Entity for Schedules table.
 * Represents bus-route assignments with timing
 */
@Entity
@Table(name = "schedules",
    indexes = {
        @Index(name = "idx_active_schedules",
               columnList = "status, effective_date, expiry_date")
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<ScheduleDay> scheduleDays;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ScheduleStatus status = ScheduleStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    /**
     * Check if the schedule is active
     */
    public boolean isActive() {
        return ScheduleStatus.ACTIVE.equals(this.status);
    }

    /**
     * Check if the schedule is currently valid based on effective and expiry dates
     */
    public boolean isCurrentlyValid() {
        LocalDate today = LocalDate.now();
        return isActive() &&
               !today.isBefore(effectiveDate) &&
               (expiryDate == null || !today.isAfter(expiryDate));
    }

    /**
     * Check if the schedule is expired
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expiryDate);
    }


    /**
     * Get bus registration number
     */
    public String getBusRegistrationNumber() {
        return bus != null ? bus.getRegistrationNumber() : null;
    }

    /**
     * Get route number
     */
    public String getRouteNumber() {
        return route != null ? route.getNumber() : null;
    }

    /**
     * Activate the schedule
     */
    public void activate() {
        this.status = ScheduleStatus.ACTIVE;
    }

    /**
     * Deactivate the schedule
     */
    public void deactivate() {
        this.status = ScheduleStatus.INACTIVE;
    }

    /**
     * Mark schedule as expired
     */
    public void expire() {
        this.status = ScheduleStatus.EXPIRED;
    }
}

package com.xebec.BusTracking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * JPA Entity for Tickets table.
 * Represents mobile ticketing transactions
 */
@Entity
@Table(name = "tickets"
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_stop_id", nullable = false)
    private Stop originStop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_stop_id", nullable = false)
    private Stop destinationStop;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime boardingTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TicketStatus status = TicketStatus.PENDING;

    @Column(nullable = false)
    private LocalDate validityDate;

    @Column(length = 30)
    private String paymentMethod;

    @Column(length = 100)
    private String paymentReference;

    private LocalDateTime validatedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Check if ticket is paid
     */
    public boolean isPaid() {
        return TicketStatus.PAID.equals(this.status);
    }

    /**
     * Check if ticket is used
     */
    public boolean isUsed() {
        return TicketStatus.USED.equals(this.status);
    }

    /**
     * Check if the ticket is expired
     */
    public boolean isExpired() {
        return TicketStatus.EXPIRED.equals(this.status) ||
               (validityDate != null && LocalDate.now().isAfter(validityDate));
    }

    /**
     * Check if ticket is canceled
     */
    public boolean isCancelled() {
        return TicketStatus.CANCELLED.equals(this.status);
    }

    /**
     * Check if ticket can be validated (used)
     */
    public boolean canBeValidated() {
        return isPaid() &&
               !isExpired() &&
               validityDate != null &&
               !LocalDate.now().isAfter(validityDate);
    }

    /**
     * Check if ticket can be canceled
     */
    public boolean canBeCancelled() {
        return (TicketStatus.PENDING.equals(this.status) ||
                TicketStatus.PAID.equals(this.status)) &&
               !isUsed();
    }

    /**
     * Mark ticket as paid
     */
    public void markAsPaid(String paymentMethod, String paymentReference) {
        if (!TicketStatus.PENDING.equals(this.status)) {
            throw new IllegalStateException("Only PENDING tickets can be marked as PAID");
        }
        this.status = TicketStatus.PAID;
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
    }

    /**
     * Validate/use the ticket
     */
    public void validate() {
        if (!canBeValidated()) {
            throw new IllegalStateException("Ticket cannot be validated");
        }
        this.status = TicketStatus.USED;
        this.validatedAt = LocalDateTime.now();
    }

    /**
     * Cancel the ticket
     */
    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Ticket cannot be cancelled");
        }
        this.status = TicketStatus.CANCELLED;
    }

    /**
     * Mark the ticket as expired
     */
    public void markAsExpired() {
        if (TicketStatus.PAID.equals(this.status) && isExpired()) {
            this.status = TicketStatus.EXPIRED;
        }
    }

    /**
     * Get passenger name (convenience method)
     */
    public String getPassengerName() {
        return passenger != null ? passenger.getFirstName() + " " + passenger.getLastName() : null;
    }

    /**
     * Get route number (convenience method)
     */
    public String getRouteNumber() {
        return schedule != null ? schedule.getRoute().getNumber() : null;
    }

    /**
     * Get origin stop name (convenience method)
     */
    public String getOriginStopName() {
        return originStop != null ? originStop.getName() : null;
    }

    /**
     * Get destination stop name (convenience method)
     */
    public String getDestinationStopName() {
        return destinationStop != null ? destinationStop.getName() : null;
    }
}

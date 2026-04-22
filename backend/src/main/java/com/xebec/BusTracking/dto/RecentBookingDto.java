package com.xebec.BusTracking.dto;

import com.xebec.BusTracking.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecentBookingDto {
    private String passengerName;
    private String route;
    private String status;
    private String timeAgo;

    /**
     * Used by JPQL constructor expressions in {@code TicketRepository}.
     */
    public RecentBookingDto(String passengerName, String route, TicketStatus status, LocalDateTime createdAt) {
        this.passengerName = passengerName;
        this.route = route;
        this.status = status != null ? toUiStatus(status) : null;
        this.timeAgo = createdAt != null ? formatTimeAgo(createdAt) : null;
    }

    private static String toUiStatus(TicketStatus status) {
        return switch (status) {
            case PAID, USED -> "Confirmed";
            case PENDING -> "Pending";
            case CANCELLED -> "Cancelled";
            default -> capitalize(status.name());
        };
    }

    private static String capitalize(String raw) {
        if (raw == null || raw.isBlank()) return raw;
        String lower = raw.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static String formatTimeAgo(LocalDateTime createdAt) {
        Duration d = Duration.between(createdAt, LocalDateTime.now());
        long minutes = Math.max(0, d.toMinutes());
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " mins ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + " hrs ago";
        long days = hours / 24;
        return days + " days ago";
    }
}

package com.xebec.BusTracking.dto;

import com.xebec.BusTracking.model.ScheduleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDto {
    private Long id;
    private Long busId;
    private Long routeId;

    @NotNull(message = "Departure time is required")
    private LocalTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalTime arrivalTime;

    private Set<DayOfWeek> daysOfWeek;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;
    private LocalDate expiryDate;

    private ScheduleStatus status;
}

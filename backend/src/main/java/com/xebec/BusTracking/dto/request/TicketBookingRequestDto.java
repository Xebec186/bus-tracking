package com.xebec.BusTracking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TicketBookingRequestDto {

    @NotNull(message = "Schedule id is required")
    private Long scheduleId;

    @NotNull(message = "Origin stop id is required")
    private Long originStopId;

    @NotNull(message = "Destination stop id is required")
    private Long destinationStopId;

    @NotNull(message = "Travel date is required")
    private LocalDate date;
}


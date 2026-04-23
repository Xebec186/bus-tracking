package com.xebec.BusTracking.dto;

import com.xebec.BusTracking.model.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripDto {
    private Long id;
    private Long scheduleId;
    private Long busId;
    private Integer ticketsSold;
    private Double revenue;
    private TripStatus status;
    private LocalDateTime actualDepartureTime;
    private LocalDateTime actualArrivalTime;

    // --- Enhanced fields for Mobile UI ---
    private String routeName;
    private String origin;
    private String destination;
    private String scheduledDeparture;
    private String scheduledArrival;
    private Integer passengerCount;
}

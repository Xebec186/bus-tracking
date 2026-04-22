package com.xebec.BusTracking.dto;

import com.xebec.BusTracking.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketDto {
    private Long id;
    private Long passengerId;

    private Long scheduleId;

    private String code;

    private Long originStopId;
    private Long destinationStopId;

    private BigDecimal price;

    private LocalDate date;

    private LocalTime boardingTime;

    private LocalDate validityDate;

    private LocalDateTime validatedAt;

    private TicketStatus status = TicketStatus.PENDING;

    private String paymentMethod;

    private String paymentReference;
}

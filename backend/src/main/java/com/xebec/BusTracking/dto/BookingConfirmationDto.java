package com.xebec.BusTracking.dto;

import com.xebec.BusTracking.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmationDto {
    private Long ticketId;
    private String ticketCode;
    private TicketStatus status;
    private BigDecimal price;
    private LocalDate travelDate;
}

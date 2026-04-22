package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.TicketDto;
import com.xebec.BusTracking.model.Schedule;
import com.xebec.BusTracking.model.Stop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TicketService {
    TicketDto addTicket(TicketDto ticketDto);

    String generateTicketCode(Long scheduleId, LocalDate ticketDate);

    BigDecimal calculatePrice(Schedule schedule, Stop originStop, Stop destinationStop);

    TicketDto getTicket(Long ticketId);

    List<TicketDto> getAllTickets();

    void deleteTicket(Long ticketId);

    TicketDto payTicket(TicketDto ticketDto);

    List<TicketDto> getTicketsByPassenger(Long passengerId);

    List<TicketDto> getTicketsBySchedule(Long scheduleId);

    List<TicketDto> getActiveTicketsByPassenger(Long passengerId);

    TicketDto validateTicket(String code);

    TicketDto cancelTicket(Long ticketId);

}

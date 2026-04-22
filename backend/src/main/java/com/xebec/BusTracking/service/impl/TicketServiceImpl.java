package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.TicketDto;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.*;
import com.xebec.BusTracking.repository.*;
import com.xebec.BusTracking.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final StopRepository stopRepository;
    private final RouteStopRepository routeStopRepository;
    private final ModelMapper modelMapper;

    @Override
    public TicketDto addTicket(TicketDto ticketDto) {
        Ticket ticket = modelMapper.map(ticketDto, Ticket.class);

        Long passengerId = ticketDto.getPassengerId();
        User user = userRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given id: " + passengerId));
        if(!user.isPassenger()) {
            throw new IllegalStateException("User must have PASSENGER role");
        }

        Long scheduleId = ticketDto.getScheduleId();
        Schedule schedule = scheduleRepository.findById(scheduleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with given id: " + scheduleId));

        Long originStopId = ticketDto.getOriginStopId();
        Long destinationStopId = ticketDto.getDestinationStopId();
        LocalDate ticketDate = ticketDto.getDate();
        DayOfWeek day = ticketDate.getDayOfWeek();

        Stop originStop = stopRepository.findById(originStopId)
                        .orElseThrow(() -> new ResourceNotFoundException("Stop not found with given id: " + originStopId));
        Stop destinationStop = stopRepository.findById(destinationStopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with given id: " + destinationStopId));
        ScheduleDay scheduleDay = scheduleDayRepository.findByScheduleIdAndDay(scheduleId, day)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not available on: " + day));

        ticket.setPassenger(user);
        ticket.setSchedule(schedule);
        ticket.setOriginStop(originStop);
        ticket.setDestinationStop(destinationStop);
        ticket.setPrice(calculatePrice(schedule, originStop, destinationStop));
        ticket.setBoardingTime(scheduleDay.getDepartureTime().minusMinutes(30));
        ticket.setValidityDate(ticketDate.plusDays(1));

        String ticketCode = generateTicketCode(scheduleId, ticketDate);
        ticket.setCode(ticketCode);

        Ticket addedTicket = ticketRepository.save(ticket);

        return modelMapper.map(addedTicket, TicketDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDto getTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with given id: " + ticketId));
        return modelMapper.map(ticket, TicketDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(ticket -> modelMapper.map(ticket, TicketDto.class))
                .toList();
    }

    @Override
    public void deleteTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with given id: " + ticketId));
        ticketRepository.delete(ticket);
    }

    @Override
    public TicketDto payTicket(TicketDto ticketDto) {
        Long ticketId = ticketDto.getId();
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with given id: " + ticketId));

        Long passengerId = ticketDto.getPassengerId();
        if (passengerId == null || ticket.getPassenger() == null || !passengerId.equals(ticket.getPassenger().getId())) {
            throw new AccessDeniedException("You are not allowed to pay this ticket");
        }

        ticket.markAsPaid(ticketDto.getPaymentMethod(), ticketDto.getPaymentReference());

        Ticket paidTicket = ticketRepository.save(ticket);
        return modelMapper.map(paidTicket, TicketDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByPassenger(Long passengerId) {
        return ticketRepository.findByPassengerId(passengerId).stream()
                .map(ticket -> modelMapper.map(ticket, TicketDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsBySchedule(Long scheduleId) {
        return ticketRepository.findByScheduleId(scheduleId).stream()
                .map(ticket -> modelMapper.map(ticket, TicketDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getActiveTicketsByPassenger(Long passengerId) {
        return ticketRepository.findByPassengerIdAndStatusIn(passengerId, List.of(TicketStatus.PAID, TicketStatus.PENDING)).stream()
                .map(ticket -> modelMapper.map(ticket, TicketDto.class))
                .toList();
    }

    @Override
    public TicketDto validateTicket(String code) {
        Ticket ticket = ticketRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with given code: " + code));

        ticket.validate();

        Ticket validatedTicket = ticketRepository.save(ticket);
        return modelMapper.map(validatedTicket, TicketDto.class);
    }

    @Override
    public TicketDto cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with given id: " + ticketId));
        ticket.cancel();
        Ticket canclledTicket = ticketRepository.save(ticket);
        return modelMapper.map(canclledTicket, TicketDto.class);
    }

    @Override
    public String generateTicketCode(Long scheduleId, LocalDate ticketDate) {

        String prefix = "BUS";

        String datePart = ticketDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 5)
                .toUpperCase();

        return prefix + "-" + scheduleId + "-" + datePart + "-" + randomPart;
    }

    @Override
    public BigDecimal calculatePrice(Schedule schedule, Stop origin, Stop destination) {

        List<RouteStop> routeStops = routeStopRepository
                .findByRouteIdOrderByStopSequence(schedule.getRoute().getId());

        RouteStop originRouteStop = routeStops.stream()
                .filter(rs -> rs.getStop().getId().equals(origin.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Origin stop not part of route"));

        RouteStop destinationRouteStop = routeStops.stream()
                .filter(rs -> rs.getStop().getId().equals(destination.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Destination stop not part of route"));

        int originOrder = originRouteStop.getStopSequence();
        int destinationOrder = destinationRouteStop.getStopSequence();

        if (destinationOrder <= originOrder) {
            throw new IllegalArgumentException("Destination must come after origin stop");
        }

        int segments = destinationOrder - originOrder;

        BigDecimal pricePerSegment = BigDecimal.valueOf(2.5);
        BigDecimal segmentsCount = BigDecimal.valueOf(segments);

        return pricePerSegment.multiply(segmentsCount)
                .setScale(2, RoundingMode.HALF_UP);
    }
}

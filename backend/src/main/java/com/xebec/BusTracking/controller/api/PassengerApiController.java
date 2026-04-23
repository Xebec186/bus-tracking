package com.xebec.BusTracking.controller.api;

import com.xebec.BusTracking.dto.*;
import com.xebec.BusTracking.dto.request.FareEstimateRequest;
import com.xebec.BusTracking.dto.request.SearchRouteRequest;
import com.xebec.BusTracking.dto.request.TicketBookingRequestDto;
import com.xebec.BusTracking.dto.request.TicketPaymentRequestDto;
import com.xebec.BusTracking.dto.response.RouteStopResponse;
import com.xebec.BusTracking.dto.response.ScheduleResponse;
import com.xebec.BusTracking.model.Schedule;
import com.xebec.BusTracking.model.Stop;
import com.xebec.BusTracking.model.User;
import com.xebec.BusTracking.repository.ScheduleRepository;
import com.xebec.BusTracking.repository.StopRepository;
import com.xebec.BusTracking.repository.UserRepository;
import com.xebec.BusTracking.security.MyUserDetails;
import com.xebec.BusTracking.service.RouteService;
import com.xebec.BusTracking.service.RouteStopService;
import com.xebec.BusTracking.service.ScheduleService;
import com.xebec.BusTracking.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/passenger")
public class PassengerApiController {
    private final RouteService routeService;
    private final ScheduleService scheduleService;
    private final TicketService ticketService;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final StopRepository stopRepository;
    private final RouteStopService routeStopService;

    @GetMapping("/routes")
    public List<RouteDto> routes() {
        return routeService.getAllRoutes();
    }

    @GetMapping("/routes/active")
    public List<RouteDto> activeRoutes() {
        return routeService.getActiveRoutes();
    }

    @GetMapping("/routes/{routeId}")
    public RouteDto route(@PathVariable Long routeId) {
        return routeService.getRouteById(routeId);
    }

    @GetMapping("/routes/{routeId}/stops")
    public List<RouteStopResponse> getRouteStops(@PathVariable Long routeId) {
        return routeStopService.getStopsByRouteIdOrdered(routeId);
    }

    @GetMapping("/schedules")
    public List<ScheduleDto> schedules() {
        return scheduleService.getActiveSchedules();
    }

    @GetMapping("/schedules/{scheduleId}")
    public ScheduleDto schedule(@PathVariable Long scheduleId) {
        return scheduleService.getScheduleById(scheduleId);
    }

    @GetMapping("/routes/{routeId}/schedules")
    public List<ScheduleResponse> getSchedulesByRouteId(@PathVariable Long routeId) {
        return scheduleService.getSchedulesByRouteId(routeId);
    }

    @GetMapping("/tickets")
    public List<TicketDto> myTickets(@AuthenticationPrincipal MyUserDetails principal) {
        return ticketService.getTicketsByPassenger(currentUserId(principal));
    }

    @PostMapping("/tickets/book")
    public BookingConfirmationDto bookTicket(@AuthenticationPrincipal MyUserDetails principal,
                                             @Valid @RequestBody TicketBookingRequestDto request) {
        TicketDto ticketDto = new TicketDto();
        ticketDto.setPassengerId(currentUserId(principal));
        ticketDto.setScheduleId(request.getScheduleId());
        ticketDto.setOriginStopId(request.getOriginStopId());
        ticketDto.setDestinationStopId(request.getDestinationStopId());
        ticketDto.setDate(request.getDate());

        TicketDto booked = ticketService.addTicket(ticketDto);
        return new BookingConfirmationDto(
                booked.getId(),
                booked.getCode(),
                booked.getStatus(),
                booked.getPrice(),
                booked.getDate()
        );
    }

    @GetMapping("/tickets/{ticketId}")
    public TicketDto ticket(@PathVariable Long ticketId,
                            @AuthenticationPrincipal MyUserDetails principal) {
        TicketDto ticket = ticketService.getTicket(ticketId);
        Long userId = currentUserId(principal);
        if (ticket.getPassengerId() == null || !ticket.getPassengerId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not allowed to access this ticket");
        }
        return ticket;
    }

    @PostMapping("/tickets/{ticketId}/pay")
    public TicketDto payTicket(@PathVariable Long ticketId,
                                @Valid @RequestBody TicketPaymentRequestDto request,
                                @AuthenticationPrincipal MyUserDetails principal) {
        TicketDto ticketDto = new TicketDto();
        ticketDto.setId(ticketId);
        ticketDto.setPassengerId(currentUserId(principal));
        ticketDto.setPaymentMethod(request.getPaymentMethod());
        ticketDto.setPaymentReference(request.getPaymentReference());

        return ticketService.payTicket(ticketDto);
    }

    @PostMapping("/tickets/{code}/validate")
    public TicketDto validateTicket(@PathVariable String code) {
        return ticketService.validateTicket(code);
    }

    @PostMapping("/search-route")
    public List<RouteDto> searchRoute(@Valid @RequestBody SearchRouteRequest request) {
        return routeService.findByOriginAndDestination(request.getOrigin(), request.getDestination());
    }

    @GetMapping("/fare-estimate")
    public BigDecimal fareEstimate(@RequestParam Long scheduleId,
                                   @RequestParam Long originStopId,
                                   @RequestParam Long destinationStopId) {
        FareEstimateRequest request = new FareEstimateRequest();
        request.setScheduleId(scheduleId);
        request.setOriginStopId(originStopId);
        request.setDestinationStopId(destinationStopId);
        return fareEstimateInternal(request);
    }

    @PostMapping("/fare-estimate")
    public BigDecimal fareEstimatePost(@Valid @RequestBody FareEstimateRequest request) {
        return fareEstimateInternal(request);
    }

    private BigDecimal fareEstimateInternal(FareEstimateRequest request) {
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        Stop origin = stopRepository.findById(request.getOriginStopId())
                .orElseThrow(() -> new IllegalArgumentException("Origin stop not found"));
        Stop destination = stopRepository.findById(request.getDestinationStopId())
                .orElseThrow(() -> new IllegalArgumentException("Destination stop not found"));
        return ticketService.calculatePrice(schedule, origin, destination);
    }

    private Long currentUserId(MyUserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        return user.getId();
    }
}

package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.TripDto;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.*;
import com.xebec.BusTracking.repository.ScheduleRepository;
import com.xebec.BusTracking.repository.TripRepository;
import com.xebec.BusTracking.security.MyUserDetails;
import com.xebec.BusTracking.service.TripService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {
    private final TripRepository tripRepository;
    private final ScheduleRepository scheduleRepository;
    private final ModelMapper modelMapper;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public TripDto createTrip(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with given id: " + scheduleId));

        Trip trip = new Trip();
        trip.setSchedule(schedule);
        trip.setBus(schedule.getBus());
        trip.setTicketsSold(0);
        trip.setRevenue(0.0);
        trip.setStatus(TripStatus.ACTIVE);

        return mapToDto(tripRepository.save(trip));
    }

    @Override
    @Transactional(readOnly = true)
    public TripDto getTripById(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with given id: " + tripId));
        return mapToDto(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDto> getAllTrips() {
        return tripRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDto> getTripsBySchedule(Long scheduleId) {
        return tripRepository.findByScheduleId(scheduleId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDto> getTripsByBus(Long busId) {
        return tripRepository.findByBusId(busId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDto> getTripsByDriver(Long driverId) {
        return tripRepository.findByBusDriverId(driverId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public TripDto updateTripStatus(Long tripId, TripStatus status) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with given id: " + tripId));

        assertCurrentDriverCanModifyTrip(trip);

        trip.setStatus(status);
        return mapToDto(tripRepository.save(trip));
    }

    @Override
    public TripDto recordDeparture(Long tripId, LocalDateTime actualDeparture) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with given id: " + tripId));

        assertCurrentDriverCanModifyTrip(trip);

        trip.setActualDepartureTime(actualDeparture);
        trip.setStatus(TripStatus.ACTIVE);
        return mapToDto(tripRepository.save(trip));
    }

    @Override
    public TripDto recordArrival(Long tripId, LocalDateTime actualArrival) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with given id: " + tripId));

        assertCurrentDriverCanModifyTrip(trip);

        trip.setActualArrivalTime(actualArrival);
        trip.setStatus(TripStatus.COMPLETED);
        return mapToDto(tripRepository.save(trip));
    }

    @Override
    public TripDto systemRecordArrival(Long tripId, LocalDateTime actualArrival) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with given id: " + tripId));

        trip.setActualArrivalTime(actualArrival);
        trip.setStatus(TripStatus.COMPLETED);
        return mapToDto(tripRepository.save(trip));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDto> getTripsByDateRange(LocalDateTime start, LocalDateTime end) {
        return tripRepository.findByActualDepartureTimeBetween(start, end).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long completedTripCount() {
        return tripRepository.countByStatus(TripStatus.COMPLETED);
    }

    private TripDto mapToDto(Trip trip) {
        TripDto dto = modelMapper.map(trip, TripDto.class);
        
        if (trip.getSchedule() != null) {
            Route route = trip.getSchedule().getRoute();
            if (route != null) {
                dto.setRouteName(route.getName() + " (" + route.getNumber() + ")");
                
                // Set origin and destination from route stops if available
                if (route.getRouteStops() != null && !route.getRouteStops().isEmpty()) {
                    List<RouteStop> stops = route.getRouteStops();
                    dto.setOrigin(stops.get(0).getStop().getName());
                    dto.setDestination(stops.get(stops.size() - 1).getStop().getName());
                }
            }
            
            // Set scheduled times from schedule days if today
            if (trip.getSchedule().getScheduleDays() != null && !trip.getSchedule().getScheduleDays().isEmpty()) {
                ScheduleDay day = trip.getSchedule().getScheduleDays().get(0); // Take first as representative
                dto.setScheduledDeparture(day.getDepartureTime().format(timeFormatter));
                dto.setScheduledArrival(day.getArrivalTime().format(timeFormatter));
            }
        }
        
        dto.setPassengerCount(trip.getTicketsSold() != null ? trip.getTicketsSold() : 0);
        
        return dto;
    }

    private void assertCurrentDriverCanModifyTrip(Trip trip) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof MyUserDetails principal)) {
            throw new AccessDeniedException("Authentication required");
        }

        Long currentDriverId = principal.getUser().getId();
        if (trip.getBus() == null || trip.getBus().getDriver() == null || trip.getBus().getDriver().getId() == null
                || !trip.getBus().getDriver().getId().equals(currentDriverId)) {
            throw new AccessDeniedException("You are not allowed to modify this trip");
        }
    }
}

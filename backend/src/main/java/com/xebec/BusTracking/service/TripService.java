package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.TripDto;
import com.xebec.BusTracking.model.TripStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface TripService {
    TripDto createTrip(Long scheduleId);

    TripDto getTripById(Long tripId);

    List<TripDto> getAllTrips();

    List<TripDto> getTripsBySchedule(Long scheduleId);

    List<TripDto> getTripsByBus(Long busId);

    List<TripDto> getTripsByDriver(Long driverId);

    TripDto updateTripStatus(Long tripId, TripStatus status);

    TripDto recordDeparture(Long tripId, LocalDateTime actualDeparture);

    TripDto recordArrival(Long tripId, LocalDateTime actualArrival);

    List<TripDto> getTripsByDateRange(LocalDateTime start, LocalDateTime end);

    long completedTripCount();
}

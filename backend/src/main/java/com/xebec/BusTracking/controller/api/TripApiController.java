package com.xebec.BusTracking.controller.api;

import com.xebec.BusTracking.dto.TripDto;
import com.xebec.BusTracking.dto.request.TripStatusUpdateRequest;
import com.xebec.BusTracking.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
public class TripApiController {
    private final TripService tripService;

    @GetMapping
    public List<TripDto> trips() {
        return tripService.getAllTrips();
    }

    @GetMapping("/{tripId}")
    public TripDto trip(@PathVariable Long tripId) {
        return tripService.getTripById(tripId);
    }

    @GetMapping("/schedule/{scheduleId}")
    public List<TripDto> tripsBySchedule(@PathVariable Long scheduleId) {
        return tripService.getTripsBySchedule(scheduleId);
    }

    @GetMapping("/bus/{busId}")
    public List<TripDto> tripsByBus(@PathVariable Long busId) {
        return tripService.getTripsByBus(busId);
    }

    @GetMapping("/driver/{driverId}")
    public List<TripDto> tripsByDriver(@PathVariable Long driverId) {
        return tripService.getTripsByDriver(driverId);
    }

    @PutMapping("/{tripId}/status")
    public TripDto updateStatus(@PathVariable Long tripId, @Valid @RequestBody TripStatusUpdateRequest request) {
        return tripService.updateTripStatus(tripId, request.getStatus());
    }

    @PostMapping("/{tripId}/depart")
    public TripDto depart(@PathVariable Long tripId) {
        return tripService.recordDeparture(tripId, LocalDateTime.now());
    }

    @PostMapping("/{tripId}/arrive")
    public TripDto arrive(@PathVariable Long tripId) {
        return tripService.recordArrival(tripId, LocalDateTime.now());
    }
}

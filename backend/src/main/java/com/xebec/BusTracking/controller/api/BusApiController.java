package com.xebec.BusTracking.controller.api;

import com.xebec.BusTracking.dto.BusDto;
import com.xebec.BusTracking.dto.BusLocationDto;
import com.xebec.BusTracking.dto.TripDto;
import com.xebec.BusTracking.service.BusLocationService;
import com.xebec.BusTracking.service.BusService;
import com.xebec.BusTracking.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/buses")
public class BusApiController {
    private final BusService busService;
    private final BusLocationService busLocationService;
    private final TripService tripService;

    @GetMapping
    public List<BusDto> getAllBuses() {
        return busService.getAllBuses();
    }

    @PostMapping
    public BusDto addBus(@Valid @RequestBody BusDto busDto) {
        return busService.addBus(busDto);
    }

    @GetMapping("/{busId}")
    public BusDto getBus(@PathVariable Long busId) {
        return busService.getBusById(busId);
    }

    @PutMapping("/{busId}")
    public BusDto updateBus(@PathVariable Long busId, @Valid @RequestBody BusDto busDto) {
        return busService.updateBus(busId, busDto);
    }

    @DeleteMapping("/{busId}")
    public void deleteBus(@PathVariable Long busId) {
        busService.deleteBus(busId);
    }

    @PostMapping("/{busId}/location")
    public BusLocationDto updateBusLocation(@PathVariable Long busId, @Valid @RequestBody BusLocationDto locationDto) {
        locationDto.setBusId(busId);
        return busLocationService.addBusLocation(locationDto);
    }

    @GetMapping("/{busId}/location/latest")
    public BusLocationDto latestLocation(@PathVariable Long busId) {
        return busLocationService.getLatestLocationByBusId(busId);
    }

    @GetMapping("/{busId}/trips")
    public List<TripDto> busTrips(@PathVariable Long busId) {
        return tripService.getTripsByBus(busId);
    }
}

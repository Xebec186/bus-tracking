package com.xebec.BusTracking.controller.api;

import com.xebec.BusTracking.dto.BusLocationDto;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.repository.RouteRepository;
import com.xebec.BusTracking.repository.ScheduleRepository;
import com.xebec.BusTracking.service.BusLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tracking")
public class TrackingApiController {
    private final BusLocationService busLocationService;
    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;

    @GetMapping("/buses")
    public List<BusLocationDto> allBusTracking() {
        return busLocationService.getAllBusLocations();
    }

    @GetMapping("/buses/{busId}")
    public BusLocationDto oneBusTracking(@PathVariable Long busId) {
        return busLocationService.getLatestLocationByBusId(busId);
    }

    @PostMapping("/location")
    public BusLocationDto updateLocation(@Valid @RequestBody BusLocationDto dto) {
        return busLocationService.addBusLocation(dto);
    }

    @GetMapping("/routes/{routeId}")
    public List<BusLocationDto> routeTracking(@PathVariable Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new ResourceNotFoundException("Route not found with given id: " + routeId);
        }
        List<Long> busIds = scheduleRepository.findByRouteId(routeId).stream()
                .map(schedule -> schedule.getBus() != null ? schedule.getBus().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return busIds.stream()
                .map(busLocationService::getLatestLocationByBusId)
                .toList();
    }
}

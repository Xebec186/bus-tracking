package com.xebec.BusTracking.controller.api;

import com.xebec.BusTracking.dto.BusLocationDto;
import com.xebec.BusTracking.dto.response.RouteStopResponse;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.repository.RouteRepository;
import com.xebec.BusTracking.repository.ScheduleRepository;
import com.xebec.BusTracking.service.BusLocationService;
import com.xebec.BusTracking.service.RouteStopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tracking")
public class TrackingApiController {
    private final BusLocationService busLocationService;
    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;
    private final RouteStopService routeStopService;

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

    /**
     * Returns latest locations for all buses on a specific route
     */
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
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Returns the geometric path (stops) of a route for polyline rendering
     */
    @GetMapping("/routes/{routeId}/path")
    public Map<String, Object> getRoutePath(@PathVariable Long routeId) {
        List<RouteStopResponse> stops = routeStopService.getStopsByRouteIdOrdered(routeId);
        Map<String, Object> response = new HashMap<>();
        response.put("routeId", routeId);
        response.put("coordinates", stops.stream()
                .map(s -> {
                    Map<String, Object> coord = new HashMap<>();
                    coord.put("latitude", s.getLatitude());
                    coord.put("longitude", s.getLongitude());
                    return coord;
                })
                .toList());
        return response;
    }
}

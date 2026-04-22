package com.xebec.BusTracking.controller.api;

import com.xebec.BusTracking.dto.RouteDto;
import com.xebec.BusTracking.dto.RouteStopDto;
import com.xebec.BusTracking.dto.response.RouteStopResponse;
import com.xebec.BusTracking.service.RouteService;
import com.xebec.BusTracking.service.RouteStopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class RouteApiController {
    private final RouteService routeService;
    private final RouteStopService routeStopService;

    @GetMapping
    public List<RouteDto> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    @PostMapping
    public RouteDto createRoute(@Valid @RequestBody RouteDto routeDto) {
        return routeService.addRoute(routeDto);
    }

    @GetMapping("/{routeId}")
    public RouteDto getRoute(@PathVariable Long routeId) {
        return routeService.getRouteById(routeId);
    }

    @PutMapping("/{routeId}")
    public RouteDto updateRoute(@PathVariable Long routeId, @Valid @RequestBody RouteDto routeDto) {
        return routeService.updateRoute(routeId, routeDto);
    }

    @DeleteMapping("/{routeId}")
    public void deleteRoute(@PathVariable Long routeId) {
        routeService.deleteRoute(routeId);
    }

    @GetMapping("/{routeId}/stops")
    public List<RouteStopResponse> getRouteStops(@PathVariable Long routeId) {
        return routeStopService.getStopsByRouteIdOrdered(routeId);
    }

    @PostMapping("/{routeId}/stops")
    public RouteStopDto addRouteStop(@PathVariable Long routeId, @Valid @RequestBody RouteStopDto routeStopDto) {
        routeStopDto.setRouteId(routeId);
        return routeStopService.addRouteStop(routeStopDto);
    }
}

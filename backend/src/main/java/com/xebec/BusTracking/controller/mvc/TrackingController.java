package com.xebec.BusTracking.controller.mvc;

import com.xebec.BusTracking.dto.TrackingBusDto;
import com.xebec.BusTracking.dto.TrackingStatsDto;
import com.xebec.BusTracking.model.*;
import com.xebec.BusTracking.repository.BusLocationRepository;
import com.xebec.BusTracking.repository.BusRepository;
import com.xebec.BusTracking.repository.RouteRepository;
import com.xebec.BusTracking.repository.TripRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TrackingController {

    private final BusRepository busRepository;
    private final BusLocationRepository busLocationRepository;
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/tracking")
    public String showTrackingPage(Model model) {
        model.addAttribute("activePage", "tracking");
        List<Bus> buses = busRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        List<TrackingBusDto> active = new ArrayList<>();
        List<Object> mapPoints = new ArrayList<>();

        long inMaintenance = buses.stream().filter(b -> b.getStatus() == BusStatus.MAINTENANCE).count();

        for (Bus bus : buses) {
            if (bus.getStatus() == BusStatus.MAINTENANCE) continue; 
            BusLocation latest = busLocationRepository.findTopByBusIdOrderByTimestampDesc(bus.getId()).orElse(null);
            if (latest == null || latest.getTimestamp() == null) continue;

            boolean isRecent = Duration.between(latest.getTimestamp(), now).abs().toMinutes() <= 10;
            if (!isRecent) continue;

            TrackingBusDto.TrackingStatus status = (latest.getSpeed() != null && latest.getSpeed().compareTo(BigDecimal.valueOf(0.5)) > 0)
                    ? TrackingBusDto.TrackingStatus.MOVING
                    : TrackingBusDto.TrackingStatus.STOPPED;

            String currentRouteName = null;
            List<Trip> activeTrips = tripRepository.findByBusIdAndStatusWithScheduleAndRoute(bus.getId(), TripStatus.ACTIVE);
            if (!activeTrips.isEmpty()) {
                Trip currentTrip = activeTrips.get(0);
                currentRouteName = currentTrip.getSchedule().getRoute().getName();
            }

            TrackingBusDto dto = new TrackingBusDto(
                    bus.getRegistrationNumber(),
                    latest.getLatitude(),
                    latest.getLongitude(),
                    status,
                    currentRouteName,
                    latest.getSpeed(),
                    formatLastUpdated(latest.getTimestamp())
            );
            active.add(dto);

            mapPoints.add(new java.util.LinkedHashMap<String, Object>() {{
                put("reg", dto.getRegistrationNumber());
                put("lat", dto.getLatitude());
                put("lng", dto.getLongitude());
                put("status", dto.getTrackingStatus().name());
                put("route", dto.getCurrentRouteName());
                put("speed", dto.getSpeedKmh() != null ? dto.getSpeedKmh() : 0);
            }});
        }

        TrackingStatsDto stats = new TrackingStatsDto(
                buses.size(),
                active.size(),
                inMaintenance,
                Math.max(0, buses.size() - active.size() - inMaintenance)
        );

        model.addAttribute("activeBuses", active);
        model.addAttribute("trackingStats", stats);

        try {
            model.addAttribute("busLocationsJson", objectMapper.writeValueAsString(mapPoints));
        } catch (JsonProcessingException e) {
            log.error("Error serializing bus locations", e);
            model.addAttribute("busLocationsJson", "[]");
        }

        List<Route> routes = routeRepository.findAllWithStops();
        List<Object> routeData = new ArrayList<>();
        for (Route route : routes) {
            List<Object> stops = new ArrayList<>();
            for (RouteStop routeStop : route.getRouteStops()) {
                Stop stop = routeStop.getStop();
                stops.add(new java.util.LinkedHashMap<String, Object>() {{
                    put("name", stop.getName());
                    put("lat", stop.getLatitude());
                    put("lng", stop.getLongitude());
                    put("sequence", routeStop.getStopSequence());
                }});
            }
            routeData.add(new java.util.LinkedHashMap<String, Object>() {{
                put("id", route.getId());
                put("name", route.getName());
                put("number", route.getNumber());
                put("stops", stops);
            }});
        }

        try {
            model.addAttribute("routesJson", objectMapper.writeValueAsString(routeData));
        } catch (JsonProcessingException e) {
            log.error("Error serializing route data", e);
            model.addAttribute("routesJson", "[]");
        }

        return "admin/tracking";
    }

    private String formatLastUpdated(LocalDateTime timestamp) {
        if (timestamp == null) return "never";
        Duration duration = Duration.between(timestamp, LocalDateTime.now()).abs();
        long minutes = duration.toMinutes();
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " min ago";
        long hours = duration.toHours();
        if (hours < 24) return hours + "h ago";
        return duration.toDays() + "d ago";
    }
}

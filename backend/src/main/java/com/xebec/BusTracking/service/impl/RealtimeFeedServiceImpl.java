package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.DashboardDto;
import com.xebec.BusTracking.dto.TrackingBusDto;
import com.xebec.BusTracking.dto.TrackingRealtimePayload;
import com.xebec.BusTracking.dto.TrackingStatsDto;
import com.xebec.BusTracking.model.*;
import com.xebec.BusTracking.repository.BusLocationRepository;
import com.xebec.BusTracking.repository.BusRepository;
import com.xebec.BusTracking.repository.TripRepository;
import com.xebec.BusTracking.service.DashboardService;
import com.xebec.BusTracking.service.RealtimeFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RealtimeFeedServiceImpl implements RealtimeFeedService {

    private final BusRepository busRepository;
    private final BusLocationRepository busLocationRepository;
    private final TripRepository tripRepository;
    private final DashboardService dashboardService;

    @Override
    public TrackingRealtimePayload getTrackingPayload() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusMinutes(10);

        // --- Fix #2: single bulk query for latest locations (replaces N+1 loop) ---
        Map<Long, BusLocation> latestByBusId = busLocationRepository
                .findLatestLocationsNewerThan(cutoff)
                .stream()
                .collect(Collectors.toMap(
                        bl -> bl.getBus().getId(),
                        bl -> bl,
                        (a, b) -> a   // keep first if duplicates exist
                ));

        // --- Fix (N+1 for trips): load all active trips in one query, key by bus id ---
        Map<Long, String> routeNameByBusId = tripRepository
                .findByStatusWithScheduleAndRoute(TripStatus.ACTIVE)
                .stream()
                .filter(t -> t.getBus() != null)
                .collect(Collectors.toMap(
                        t -> t.getBus().getId(),
                        t -> t.getSchedule().getRoute().getName(),
                        (a, b) -> a   // keep first active trip per bus
                ));

        List<Bus> buses = busRepository.findAll();
        List<TrackingBusDto> active = new ArrayList<>();

        // Fix #4: count maintenance separately, skip them from the active loop
        long inMaintenance = 0;

        for (Bus bus : buses) {
            if (bus.getStatus() == BusStatus.MAINTENANCE) {
                inMaintenance++;
                continue;
            }

            BusLocation latest = latestByBusId.get(bus.getId());
            if (latest == null || latest.getTimestamp() == null) {
                continue;
            }

            // Fix #3: use .abs() so slightly-future timestamps don't produce negative durations
            boolean isRecent = Duration.between(latest.getTimestamp(), now).abs().toMinutes() <= 10;
            if (!isRecent) {
                continue;
            }

            TrackingBusDto.TrackingStatus status =
                    (latest.getSpeed() != null && latest.getSpeed().compareTo(BigDecimal.valueOf(0.5)) > 0)
                            ? TrackingBusDto.TrackingStatus.MOVING
                            : TrackingBusDto.TrackingStatus.STOPPED;

            String currentRouteName = routeNameByBusId.get(bus.getId());

            active.add(new TrackingBusDto(
                    bus.getRegistrationNumber(),
                    latest.getLatitude(),
                    latest.getLongitude(),
                    status,
                    currentRouteName,
                    latest.getSpeed(),
                    formatLastUpdated(latest.getTimestamp())
            ));
        }

        TrackingStatsDto stats = new TrackingStatsDto(
                buses.size(),
                active.size(),
                inMaintenance,
                Math.max(0, buses.size() - active.size() - inMaintenance)
        );

        return new TrackingRealtimePayload(active, stats);
    }

    // Fix #3: .abs() prevents negative durations from slightly-future timestamps
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

    @Override
    public DashboardDto getDashboardPayload() {
        return dashboardService.getDashboard();
    }
}
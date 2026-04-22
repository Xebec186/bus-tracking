package com.xebec.BusTracking.scheduler;

import com.xebec.BusTracking.model.*;
import com.xebec.BusTracking.repository.BusLocationRepository;
import com.xebec.BusTracking.repository.RouteStopRepository;
import com.xebec.BusTracking.repository.ScheduleRepository;
import com.xebec.BusTracking.service.RealtimePublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusTrackingSimulationScheduler {
    private final ScheduleRepository scheduleRepository;
    private final RouteStopRepository routeStopRepository;
    private final BusLocationRepository busLocationRepository;
    private final RealtimePublisherService realtimePublisherService;

    private final Map<Long, SimulationState> states = new ConcurrentHashMap<>();
    private final Random random = new Random(3141592L);

    @Scheduled(fixedDelayString = "${seed.simulation.interval-ms:15000}")
    @Transactional
    public void simulateBusMovement() {
        if (states.isEmpty()) {
            initializeStates();
        }

        if (states.isEmpty()) {
            return;
        }

        List<BusLocation> updates = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (SimulationState state : states.values()) {
            if (state.routeStops().size() < 2) {
                continue;
            }

            double progress = state.progress() + state.stepSize();
            int segmentIdx = state.segmentIndex();

            while (progress >= 1.0) {
                progress -= 1.0;
                segmentIdx = (segmentIdx + 1) % (state.routeStops().size() - 1);
            }

            RouteStop a = state.routeStops().get(segmentIdx);
            RouteStop b = state.routeStops().get(segmentIdx + 1);

            double lat = lerp(a.getStop().getLatitude().doubleValue(), b.getStop().getLatitude().doubleValue(), progress);
            double lng = lerp(a.getStop().getLongitude().doubleValue(), b.getStop().getLongitude().doubleValue(), progress);
            double heading = headingDegrees(a.getStop(), b.getStop());

            BusLocation location = new BusLocation();
            location.setBus(state.bus());
            location.setLatitude(decimal(lat, 8));
            location.setLongitude(decimal(lng, 8));
            location.setHeading(decimal(heading, 2));
            location.setSpeed(decimal(18 + random.nextInt(38), 2));
            location.setTimestamp(now);
            updates.add(location);

            state.segmentIndex(segmentIdx);
            state.progress(progress);
        }

        if (!updates.isEmpty()) {
            busLocationRepository.saveAll(updates);
            realtimePublisherService.publishTrackingUpdate();
        }
    }

    private void initializeStates() {
        List<Schedule> activeSchedules = scheduleRepository.findByStatus(ScheduleStatus.ACTIVE);
        if (activeSchedules.isEmpty()) {
            return;
        }

        for (Schedule schedule : activeSchedules) {
            Bus bus = schedule.getBus();
            if (bus == null || bus.getStatus() != BusStatus.ACTIVE || states.containsKey(bus.getId())) {
                continue;
            }

            List<RouteStop> orderedStops = routeStopRepository.findByRouteIdOrderByStopSequenceWithStops(schedule.getRoute().getId());
            if (orderedStops.size() < 2) {
                continue;
            }

            states.put(bus.getId(), new SimulationState(
                    bus,
                    orderedStops,
                    random.nextInt(Math.max(1, orderedStops.size() - 1)),
                    random.nextDouble(),
                    0.08 + (random.nextDouble() * 0.07)
            ));
        }
        log.info("Initialized bus simulation states: {}", states.size());
    }

    private double lerp(double a, double b, double t) {
        return a + ((b - a) * t);
    }

    private BigDecimal decimal(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private double headingDegrees(Stop from, Stop to) {
        double fromLat = Math.toRadians(from.getLatitude().doubleValue());
        double fromLng = Math.toRadians(from.getLongitude().doubleValue());
        double toLat = Math.toRadians(to.getLatitude().doubleValue());
        double toLng = Math.toRadians(to.getLongitude().doubleValue());
        double dLng = toLng - fromLng;

        double y = Math.sin(dLng) * Math.cos(toLat);
        double x = Math.cos(fromLat) * Math.sin(toLat) - Math.sin(fromLat) * Math.cos(toLat) * Math.cos(dLng);
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360.0) % 360.0;
    }

    private static final class SimulationState {
        private final Bus bus;
        private final List<RouteStop> routeStops;
        private int segmentIndex;
        private double progress;
        private final double stepSize;

        private SimulationState(Bus bus, List<RouteStop> routeStops, int segmentIndex, double progress, double stepSize) {
            this.bus = bus;
            this.routeStops = routeStops;
            this.segmentIndex = segmentIndex;
            this.progress = progress;
            this.stepSize = stepSize;
        }

        private Bus bus() {
            return bus;
        }

        private List<RouteStop> routeStops() {
            return routeStops;
        }

        private int segmentIndex() {
            return segmentIndex;
        }

        private void segmentIndex(int value) {
            this.segmentIndex = value;
        }

        private double progress() {
            return progress;
        }

        private void progress(double value) {
            this.progress = value;
        }

        private double stepSize() {
            return stepSize;
        }
    }
}

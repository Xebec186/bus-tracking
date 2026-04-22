package com.xebec.BusTracking.runner.bootstrap;

import com.xebec.BusTracking.model.*;
import com.xebec.BusTracking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
@Order(0)
public class DevMinimalSimulationBootstrap implements CommandLineRunner {
    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final BusRepository busRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final BusLocationRepository busLocationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (scheduleRepository.count() > 0) {
            log.info("Dev bootstrap skipped: schedules already exist (count={}).", scheduleRepository.count());
            return;
        }

        log.info("Dev bootstrap: creating minimal dataset for bus simulation...");

        Random random = new Random(42L);

        // Corridor: Achimota -> Circle -> Osu
        List<BootstrapPoint> corridor = List.of(
                new BootstrapPoint("Achimota", 5.6258, -0.2297),
                new BootstrapPoint("Circle", 5.5716, -0.2152),
                new BootstrapPoint("Osu", 5.5637, -0.1772)
        );

        List<Stop> stops = new ArrayList<>();
        // 12 clustered stops along the corridor with light jitter.
        for (int i = 0; i < 12; i++) {
            BootstrapPoint base = corridor.get(i % (corridor.size() - 1));
            BootstrapPoint next = corridor.get((i % (corridor.size() - 1)) + 1);

            double t = (i % (12 / (corridor.size() - 1)) + 1) / (double) (12 / (corridor.size() - 1) + 1);
            double lat = lerp(base.lat, next.lat, t) + random.nextGaussian() * 0.006;
            double lng = lerp(base.lng, next.lng, t) + random.nextGaussian() * 0.006;

            lat = clamp(lat, 5.50, 5.72);
            lng = clamp(lng, -0.31, -0.09);

            Stop stop = new Stop();
            stop.setName(base.name + " " + ordinal(i + 1) + " Stop");
            stop.setLatitude(BigDecimal.valueOf(lat).setScale(8, RoundingMode.HALF_UP));
            stop.setLongitude(BigDecimal.valueOf(lng).setScale(8, RoundingMode.HALF_UP));
            stop.setDescription("Synthetic dev bootstrap stop");
            stops.add(stop);
        }

        stops = stopRepository.saveAll(stops);

        // Precompute route distance/duration from the generated stop chain.
        double totalKm = 0.0;
        int[] cumulativeMinutesAtIndex = new int[stops.size()];
        int cumulativeMinutes = 0;
        for (int i = 1; i < stops.size(); i++) {
            Stop prev = stops.get(i - 1);
            Stop current = stops.get(i);
            double legKm = prev.calculateDistanceTo(current);
            totalKm += legKm;
            cumulativeMinutes += (int) Math.max(1, Math.round((legKm / 22.0) * 60.0));
            cumulativeMinutesAtIndex[i] = cumulativeMinutes;
        }

        int estimatedDurationMinutes = Math.max(20, cumulativeMinutesAtIndex[cumulativeMinutesAtIndex.length - 1]);

        Route route = new Route();
        route.setNumber("R-BOOT-01");
        route.setName("Achimota - Circle - Osu (Sim)");
        route.setDistanceKm(BigDecimal.valueOf(totalKm).setScale(2, RoundingMode.HALF_UP));
        route.setEstimatedDurationMinutes(estimatedDurationMinutes);
        route = routeRepository.save(route);

        // Create ordered RouteStops (stopSequence starts at 1).
        int sequence = 1;
        List<RouteStop> routeStops = new ArrayList<>();

        for (int i = 0; i < stops.size(); i++) {
            Stop current = stops.get(i);
            RouteStop rs = new RouteStop();
            rs.setRoute(route);
            rs.setStop(current);
            rs.setStopSequence(sequence++);
            rs.setEstimatedArrivalMinutes(cumulativeMinutesAtIndex[i]);
            routeStops.add(rs);
        }

        routeStops = routeStopRepository.saveAll(routeStops);
        routeStops.sort(Comparator.comparing(RouteStop::getStopSequence));

        Bus bus = new Bus();
        bus.setRegistrationNumber("GT-BOOT-01");
        bus.setCapacity(40);
        bus.setMake("Toyota");
        bus.setModel("Coaster");
        bus.setStatus(BusStatus.ACTIVE);
        bus = busRepository.save(bus);

        Schedule schedule = new Schedule();
        LocalDate today = LocalDate.now();
        schedule.setBus(bus);
        schedule.setRoute(route);
        schedule.setEffectiveDate(today.minusDays(1));
        schedule.setExpiryDate(today.plusDays(30));
        schedule.setStatus(ScheduleStatus.ACTIVE);
        schedule = scheduleRepository.save(schedule);

        // Exactly one ScheduleDay per day-of-week (UI expects day pills).
        LocalTime weekdayDeparture = LocalTime.of(7, 15);
        LocalTime weekendDeparture = LocalTime.of(8, 0);
        int travelMinutes = Math.min(100, estimatedDurationMinutes / 2);

        List<ScheduleDay> scheduleDays = new ArrayList<>();
        for (DayOfWeek dow : DayOfWeek.values()) {
            ScheduleDay sd = new ScheduleDay();
            sd.setSchedule(schedule);
            sd.setDay(dow);
            LocalTime dep = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) ? weekendDeparture : weekdayDeparture;
            sd.setDepartureTime(dep);
            sd.setArrivalTime(dep.plusMinutes(travelMinutes));
            scheduleDays.add(sd);
        }
        scheduleDayRepository.saveAll(scheduleDays);

        // Insert an initial location so "active bus" appears immediately.
        Stop first = stops.get(0);
        Stop second = stops.size() > 1 ? stops.get(1) : stops.get(0);
        double heading = headingDegrees(first, second);

        BusLocation loc = new BusLocation();
        loc.setBus(bus);
        loc.setLatitude(first.getLatitude());
        loc.setLongitude(first.getLongitude());
        loc.setSpeed(BigDecimal.valueOf(25).setScale(2, RoundingMode.HALF_UP));
        loc.setHeading(BigDecimal.valueOf(heading).setScale(2, RoundingMode.HALF_UP));
        loc.setTimestamp(LocalDateTime.now().minusSeconds(30));
        busLocationRepository.save(loc);

        log.info("Dev bootstrap complete: stops={}, route={}, bus={}, schedule={}",
                stops.size(), route.getNumber(), bus.getRegistrationNumber(), schedule.getId());
    }

    private static double lerp(double a, double b, double t) {
        return a + ((b - a) * t);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String ordinal(int n) {
        return switch (n) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> n + "th";
        };
    }

    private static double headingDegrees(Stop from, Stop to) {
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

    private record BootstrapPoint(String name, double lat, double lng) {
    }
}


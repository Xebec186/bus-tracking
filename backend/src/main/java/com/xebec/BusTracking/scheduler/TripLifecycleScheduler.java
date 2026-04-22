package com.xebec.BusTracking.scheduler;

import com.xebec.BusTracking.model.Schedule;
import com.xebec.BusTracking.model.ScheduleDay;
import com.xebec.BusTracking.model.TripStatus;
import com.xebec.BusTracking.repository.ScheduleDayRepository;
import com.xebec.BusTracking.repository.ScheduleRepository;
import com.xebec.BusTracking.repository.TripRepository;
import com.xebec.BusTracking.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripLifecycleScheduler {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final TripRepository tripRepository;
    private final TripService tripService;

    @Scheduled(fixedDelay = 300000)
    public void manageTripLifecycle() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();

        List<Schedule> activeSchedules = scheduleRepository.findAll().stream()
                .filter(Schedule::isCurrentlyValid)
                .toList();

        for (Schedule schedule : activeSchedules) {
            scheduleDayRepository.findByScheduleIdAndDay(schedule.getId(), today).ifPresent(scheduleDay -> {
                createTripIfDue(schedule, scheduleDay, now);
                completeOldTrips(schedule, scheduleDay, now);
            });
        }
    }

    private void createTripIfDue(Schedule schedule, ScheduleDay scheduleDay, LocalDateTime now) {
        LocalTime departure = scheduleDay.getDepartureTime();
        LocalTime windowEnd = departure.plusMinutes(5);
        boolean due = !now.toLocalTime().isBefore(departure) && now.toLocalTime().isBefore(windowEnd);
        if (!due) {
            return;
        }

        boolean hasActiveTrip = !tripRepository.findByScheduleIdAndStatus(schedule.getId(), TripStatus.ACTIVE).isEmpty();
        if (!hasActiveTrip) {
            tripService.createTrip(schedule.getId());
            log.info("Created active trip for schedule {}", schedule.getId());
        }
    }

    private void completeOldTrips(Schedule schedule, ScheduleDay scheduleDay, LocalDateTime now) {
        if (now.toLocalTime().isBefore(scheduleDay.getArrivalTime())) {
            return;
        }
        tripRepository.findByScheduleIdAndStatus(schedule.getId(), TripStatus.ACTIVE)
                .forEach(trip -> tripService.recordArrival(trip.getId(), now));
    }
}

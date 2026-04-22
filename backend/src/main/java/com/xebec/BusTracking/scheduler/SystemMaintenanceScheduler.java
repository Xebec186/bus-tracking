package com.xebec.BusTracking.scheduler;

import com.xebec.BusTracking.model.Schedule;
import com.xebec.BusTracking.model.ScheduleStatus;
import com.xebec.BusTracking.model.Ticket;
import com.xebec.BusTracking.model.TicketStatus;
import com.xebec.BusTracking.repository.BusLocationRepository;
import com.xebec.BusTracking.repository.ScheduleRepository;
import com.xebec.BusTracking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMaintenanceScheduler {
    private final TicketRepository ticketRepository;
    private final ScheduleRepository scheduleRepository;
    private final BusLocationRepository busLocationRepository;

    @Scheduled(fixedDelay = 3600000)
    public void expireOldTickets() {
        List<Ticket> tickets = ticketRepository.findByStatusAndValidityDateBefore(TicketStatus.PAID, LocalDate.now());
        if (tickets.isEmpty()) {
            return;
        }
        tickets.forEach(Ticket::markAsExpired);
        ticketRepository.saveAll(tickets);
        log.info("Expired {} tickets", tickets.size());
    }

    @Scheduled(cron = "0 * * * * *")
    public void updateScheduleStatuses() {
        List<Schedule> expired = scheduleRepository.findByExpiryDateBefore(LocalDate.now());
        if (expired.isEmpty()) {
            return;
        }
        expired.stream()
                .filter(schedule -> schedule.getStatus() == ScheduleStatus.ACTIVE)
                .forEach(schedule -> schedule.setStatus(ScheduleStatus.INACTIVE));
        scheduleRepository.saveAll(expired);
        log.info("Updated {} expired schedules to INACTIVE", expired.size());
    }

    @Scheduled(fixedDelay = 86400000)
    public void cleanupOldBusLocations() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        busLocationRepository.deleteByTimestampBefore(threshold);
        log.info("Cleaned up bus locations older than {}", threshold);
    }
}

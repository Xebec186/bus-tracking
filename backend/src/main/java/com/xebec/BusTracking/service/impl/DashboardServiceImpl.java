package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.DashboardDto;
import com.xebec.BusTracking.dto.DashboardStats;
import com.xebec.BusTracking.model.TripStatus;
import com.xebec.BusTracking.repository.*;
import com.xebec.BusTracking.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;

    @Override
    public DashboardDto getDashboard() {

        LocalDateTime startOfWeek = LocalDate.now()
                .with(DayOfWeek.SUNDAY)
                .atStartOfDay();

        DashboardDto dashboard = new DashboardDto();

        dashboard.setStats(getStats());
        dashboard.setWeeklyTicketSales(ticketRepository.getWeeklyTicketCounts(startOfWeek));
        dashboard.setWeeklyRevenue(ticketRepository.getWeeklyRevenue(startOfWeek));
        dashboard.setRecentBookings(ticketRepository.getRecentBookings());
        dashboard.setActivityFeed(List.of());

        return dashboard;
    }

    private DashboardStats getStats() {

        DashboardStats stats = new DashboardStats();

        stats.setTotalUsers(userRepository.count());
        stats.setTotalBuses(busRepository.count());
        stats.setActiveRoutes(routeRepository.count());
        stats.setActiveTrips(tripRepository.countByStatus(TripStatus.ACTIVE));
        stats.setTicketsToday(ticketRepository.countTicketsToday());

        return stats;
    }
}

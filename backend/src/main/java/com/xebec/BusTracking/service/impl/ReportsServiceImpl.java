package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.ReportRowDto;
import com.xebec.BusTracking.dto.ReportSummaryDto;
import com.xebec.BusTracking.model.Bus;
import com.xebec.BusTracking.model.Route;
import com.xebec.BusTracking.model.Ticket;
import com.xebec.BusTracking.repository.BusRepository;
import com.xebec.BusTracking.repository.RouteRepository;
import com.xebec.BusTracking.repository.TicketRepository;
import com.xebec.BusTracking.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportsServiceImpl implements ReportsService {

    private final TicketRepository ticketRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;


    @Override
    public ReportSummaryDto getReportSummary(LocalDate startDate, LocalDate endDate, Long routeId, Long busId) {

        List<Ticket> tickets = findTicketsBetweenDates(startDate, endDate, routeId, busId);

        long totalTickets = tickets.size();
        double totalRevenue = tickets.stream()
                .map(Ticket::getPrice)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        long activeRoutes = tickets.stream()
                .map(t -> t.getSchedule().getRoute().getId())
                .distinct()
                .count();

        double avgOccupancy = computeAverageOccupancy(tickets);

        long days = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);
        LocalDate previousEnd = startDate.minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(days - 1);

        List<Ticket> previousTickets = findTicketsBetweenDates(previousStart, previousEnd, routeId, busId);
        long previousTotalTickets = previousTickets.size();
        double previousTotalRevenue = previousTickets.stream()
                .map(Ticket::getPrice)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
        double previousOccupancy = computeAverageOccupancy(previousTickets);

        String ticketGrowth = calculateGrowth(previousTotalTickets, totalTickets);
        String revenueGrowth = calculateGrowth(previousTotalRevenue, totalRevenue);
        String occupancyGrowth = calculateGrowth(previousOccupancy, avgOccupancy);

        return new ReportSummaryDto(
                totalTickets,
                ticketGrowth,
                totalRevenue,
                revenueGrowth,
                activeRoutes,
                avgOccupancy,
                occupancyGrowth
        );
    }

    @Override
    public List<ReportRowDto> getReportData(LocalDate startDate, LocalDate endDate, Long routeId, Long busId) {
        List<Ticket> tickets = findTicketsBetweenDates(startDate, endDate, routeId, busId);
        Map<String, List<Ticket>> grouped = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getDate() + "-" +
                        t.getSchedule().getRoute().getId() + "-" +
                        t.getSchedule().getBus().getId()));
        List<ReportRowDto> rows = new ArrayList<>();
        for (List<Ticket> group : grouped.values()) {
            Ticket sample = group.get(0);
            rows.add(new ReportRowDto(
                    sample.getDate(),
                    sample.getSchedule().getRoute().getName(),
                    sample.getSchedule().getBus().getRegistrationNumber(),
                    (long) group.size(),
                    group.stream()
                            .map(Ticket::getPrice)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
            ));
        }
        rows.sort(Comparator.comparing(ReportRowDto::getDate));
        return rows;
    }
    @Override
    public Map<String, Object> getChartData(LocalDate startDate, LocalDate endDate, Long routeId, Long busId) {
        Map<String, Object> data = new HashMap<>();
        // Last 10 days trend
        List<String> last10Days = new ArrayList<>();
        List<Integer> salesTrendData = new ArrayList<>();
        List<Double> revenueTrendData = new ArrayList<>();
        LocalDate today = endDate;
        for (int i = 9; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            last10Days.add(d.getDayOfMonth() + "/" + d.getMonthValue());
            List<Ticket> dailyTickets = findTicketsBetweenDates(d, d, routeId, busId);
            salesTrendData.add(dailyTickets.size());
            revenueTrendData.add(
                    dailyTickets.stream()
                            .map(Ticket::getPrice)
                            .mapToDouble(BigDecimal::doubleValue)
                            .sum()
            );
        }
        // Route performance
        List<Route> routes = routeRepository.findAll();
        List<String> routeLabels = new ArrayList<>();
        List<Long> routePerfData = new ArrayList<>();
        for (Route route : routes) {
            routeLabels.add(route.getName());
            long routeTickets = findTicketsBetweenDates(startDate, endDate, route.getId(), busId).size();
            routePerfData.add(routeTickets);
        }
        // Bus occupancy (approx using tickets vs capacity over period)
        List<Bus> buses = busRepository.findAll();
        List<String> busLabels = new ArrayList<>();
        List<Long> busTrips = new ArrayList<>();
        List<Double> busOcc = new ArrayList<>();
        for (Bus bus : buses) {
            busLabels.add(bus.getRegistrationNumber());
            List<Ticket> busTickets = findTicketsBetweenDates(startDate, endDate, routeId, bus.getId());
            busTrips.add((long) busTickets.size()); // or distinct trip-like grouping
            busOcc.add(computeAverageOccupancy(busTickets));
        }
        data.put("last10Days", last10Days);
        data.put("salesTrendData", salesTrendData);
        data.put("revenueTrendData", revenueTrendData);
        data.put("routePerfLabels", routeLabels);
        data.put("routePerfData", routePerfData);
        data.put("busOccLabels", busLabels);
        data.put("busTripsData", busTrips);
        data.put("busOccData", busOcc);
        return data;
    }

    private List<Ticket> findTicketsBetweenDates(LocalDate start, LocalDate end, Long routeId, Long busId) {
        return ticketRepository.findTicketsForReport(start, end, routeId, busId);
    }

    private String calculateGrowth(double previousValue, double currentValue) {
        if (previousValue == 0) {
            if (currentValue == 0) return "0%";
            return "+100%";
        }
        double change = ((currentValue - previousValue) / Math.abs(previousValue)) * 100.0;
        return String.format("%+.2f%%", change);
    }

    private double computeAverageOccupancy(List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) return 0d;

        // Approximate a "trip" as (scheduleId + travel date).
        record TripKey(Long scheduleId, LocalDate date) {}
        Map<TripKey, List<Ticket>> byTrip = tickets.stream()
                .filter(t -> t.getSchedule() != null && t.getSchedule().getId() != null && t.getDate() != null)
                .collect(Collectors.groupingBy(t -> new TripKey(t.getSchedule().getId(), t.getDate())));

        if (byTrip.isEmpty()) return 0d;

        double sumPct = 0d;
        int count = 0;
        for (List<Ticket> tripTickets : byTrip.values()) {
            Ticket sample = tripTickets.get(0);
            Integer capacity = (sample.getSchedule() != null && sample.getSchedule().getBus() != null)
                    ? sample.getSchedule().getBus().getCapacity()
                    : null;
            if (capacity == null || capacity <= 0) continue;
            double pct = (tripTickets.size() * 100.0) / capacity;
            sumPct += pct;
            count++;
        }

        return count == 0 ? 0d : (sumPct / count);
    }
}
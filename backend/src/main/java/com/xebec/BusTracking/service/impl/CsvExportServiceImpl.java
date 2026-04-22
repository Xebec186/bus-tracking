package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.ReportRowDto;
import com.xebec.BusTracking.dto.ReportSummaryDto;
import com.xebec.BusTracking.model.Route;
import com.xebec.BusTracking.model.Ticket;
import com.xebec.BusTracking.model.Trip;
import com.xebec.BusTracking.repository.BusRepository;
import com.xebec.BusTracking.repository.RouteRepository;
import com.xebec.BusTracking.repository.TicketRepository;
import com.xebec.BusTracking.repository.TripRepository;
import com.xebec.BusTracking.service.CsvExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvExportServiceImpl implements CsvExportService {
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;

    @Override
    public byte[] exportTicketsToCSV(LocalDate startDate, LocalDate endDate) {
        StringBuilder sb = new StringBuilder("id,code,date,status,price,passengerEmail,route,bus\n");
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getDate() != null && !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .toList();
        for (Ticket t : tickets) {
            sb.append(t.getId()).append(',')
                    .append(safe(t.getCode())).append(',')
                    .append(t.getDate()).append(',')
                    .append(t.getStatus()).append(',')
                    .append(t.getPrice()).append(',')
                    .append(safe(t.getPassenger() != null ? t.getPassenger().getEmail() : null)).append(',')
                    .append(safe(t.getSchedule() != null && t.getSchedule().getRoute() != null ? t.getSchedule().getRoute().getName() : null)).append(',')
                    .append(safe(t.getSchedule() != null && t.getSchedule().getBus() != null ? t.getSchedule().getBus().getRegistrationNumber() : null))
                    .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportTripsToCSV(LocalDate startDate, LocalDate endDate) {
        StringBuilder sb = new StringBuilder("id,scheduleId,bus,status,ticketsSold,revenue,departure,arrival\n");
        List<Trip> trips = tripRepository.findAll().stream()
                .filter(t -> t.getActualDepartureTime() != null)
                .filter(t -> {
                    LocalDate d = t.getActualDepartureTime().toLocalDate();
                    return !d.isBefore(startDate) && !d.isAfter(endDate);
                })
                .toList();
        for (Trip t : trips) {
            sb.append(t.getId()).append(',')
                    .append(t.getSchedule() != null ? t.getSchedule().getId() : "").append(',')
                    .append(safe(t.getBus() != null ? t.getBus().getRegistrationNumber() : null)).append(',')
                    .append(t.getStatus()).append(',')
                    .append(t.getTicketsSold()).append(',')
                    .append(t.getRevenue()).append(',')
                    .append(t.getActualDepartureTime()).append(',')
                    .append(t.getActualArrivalTime())
                    .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportReportToCSV(ReportSummaryDto summary, List<ReportRowDto> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("totalTickets,totalRevenue,activeRoutes,avgOccupancy\n")
                .append(summary.getTotalTickets()).append(',')
                .append(summary.getTotalRevenue()).append(',')
                .append(summary.getActiveRoutes()).append(',')
                .append(summary.getAvgOccupancy()).append("\n\n");
        sb.append("date,route,bus,tickets,revenue\n");
        for (ReportRowDto row : data) {
            sb.append(row.getDate()).append(',')
                    .append(safe(row.getRouteName())).append(',')
                    .append(safe(row.getBusReg())).append(',')
                    .append(row.getTicketsSold()).append(',')
                    .append(row.getRevenue())
                    .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportBusesListToCSV() {
        StringBuilder sb = new StringBuilder("id,registrationNumber,capacity,make,model,status\n");
        busRepository.findAll().forEach(b -> sb.append(b.getId()).append(',')
                .append(safe(b.getRegistrationNumber())).append(',')
                .append(b.getCapacity()).append(',')
                .append(safe(b.getMake())).append(',')
                .append(safe(b.getModel())).append(',')
                .append(b.getStatus()).append('\n'));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportRoutesListToCSV() {
        StringBuilder sb = new StringBuilder("id,number,name,distanceKm,estimatedDurationMinutes\n");
        List<Route> routes = routeRepository.findAll();
        for (Route route : routes) {
            sb.append(route.getId()).append(',')
                    .append(safe(route.getNumber())).append(',')
                    .append(safe(route.getName())).append(',')
                    .append(route.getDistanceKm()).append(',')
                    .append(route.getEstimatedDurationMinutes())
                    .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String safe(String input) {
        if (input == null) {
            return "";
        }
        String escaped = input.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}

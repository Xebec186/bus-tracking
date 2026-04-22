package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.ReportRowDto;
import com.xebec.BusTracking.dto.ReportSummaryDto;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.Ticket;
import com.xebec.BusTracking.model.Trip;
import com.xebec.BusTracking.repository.TicketRepository;
import com.xebec.BusTracking.repository.TripRepository;
import com.xebec.BusTracking.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements PdfExportService {
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;

    @Override
    public byte[] exportReportToPDF(ReportSummaryDto summary, List<ReportRowDto> data, LocalDate start, LocalDate end) {
        return withDocument(document -> {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                writeLine(content, 700, "Bus Tracking Report: " + start + " to " + end);
                writeLine(content, 680, "Tickets: " + summary.getTotalTickets());
                writeLine(content, 665, "Revenue: " + summary.getTotalRevenue());
                writeLine(content, 650, "Active Routes: " + summary.getActiveRoutes());
                writeLine(content, 635, "Avg Occupancy: " + String.format("%.2f", summary.getAvgOccupancy()) + "%");
                int y = 610;
                for (ReportRowDto row : data.stream().limit(20).toList()) {
                    writeLine(content, y, row.getDate() + " | " + row.getRouteName() + " | " + row.getBusReg()
                            + " | " + row.getTicketsSold() + " | " + row.getRevenue());
                    y -= 14;
                }
            }
        });
    }

    @Override
    public byte[] exportTicketToPDF(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with given id: " + ticketId));
        return withDocument(document -> {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                writeLine(content, 700, "Ticket #" + ticket.getId());
                writeLine(content, 680, "Code: " + ticket.getCode());
                writeLine(content, 665, "Passenger: " + ticket.getPassengerName());
                writeLine(content, 650, "Date: " + ticket.getDate());
                writeLine(content, 635, "Route: " + ticket.getRouteNumber());
                writeLine(content, 620, "From: " + ticket.getOriginStopName());
                writeLine(content, 605, "To: " + ticket.getDestinationStopName());
                writeLine(content, 590, "Status: " + ticket.getStatus());
                writeLine(content, 575, "Price: " + ticket.getPrice());
            }
        });
    }

    @Override
    public byte[] exportTripSummaryToPDF(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with given id: " + tripId));
        return withDocument(document -> {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                writeLine(content, 700, "Trip Summary #" + trip.getId());
                writeLine(content, 680, "Schedule: " + (trip.getSchedule() != null ? trip.getSchedule().getId() : ""));
                writeLine(content, 665, "Bus: " + (trip.getBus() != null ? trip.getBus().getRegistrationNumber() : ""));
                writeLine(content, 650, "Status: " + trip.getStatus());
                writeLine(content, 635, "Tickets Sold: " + trip.getTicketsSold());
                writeLine(content, 620, "Revenue: " + trip.getRevenue());
                writeLine(content, 605, "Departure: " + trip.getActualDepartureTime());
                writeLine(content, 590, "Arrival: " + trip.getActualArrivalTime());
            }
        });
    }

    private interface PdfWriter {
        void write(PDDocument document) throws IOException;
    }

    private byte[] withDocument(PdfWriter writer) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writer.write(document);
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to export PDF", ex);
        }
    }

    private void writeLine(PDPageContentStream content, int y, String text) throws IOException {
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        content.newLineAtOffset(50, y);
        content.showText(text == null ? "" : text);
        content.endText();
    }
}

package com.xebec.BusTracking.controller.mvc;

import com.xebec.BusTracking.dto.BusDto;
import com.xebec.BusTracking.dto.ReportRowDto;
import com.xebec.BusTracking.dto.ReportSummaryDto;
import com.xebec.BusTracking.dto.RouteDto;
import com.xebec.BusTracking.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;
    private final RouteService routeService;
    private final BusService busService;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;

    @GetMapping("/reports")
    public String showReportsPage(
            @RequestParam(value = "reportType", required = false, defaultValue = "SALES_SUMMARY") String reportType,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "routeId", required = false) Long routeId,
            @RequestParam(value = "busId", required = false) Long busId,
            Model model) {

        model.addAttribute("activePage", "reports");

        // Provide defaults if dates not supplied
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null)   endDate = LocalDate.now();

        // Fetch dropdown data
        List<RouteDto> routes = routeService.getAllRoutes();
        List<BusDto> buses = busService.getAllBuses();

        // Fetch report summary and table/chart data
        ReportSummaryDto summary = reportsService.getReportSummary(startDate, endDate, routeId, busId);
        List<ReportRowDto> reportData = reportsService.getReportData(startDate, endDate, routeId, busId);

        // Add chart-specific data (last 10 days, route perf, bus occupancy)
        model.addAllAttributes(reportsService.getChartData(startDate, endDate, routeId, busId));

        // Add everything to model for Thymeleaf
        model.addAttribute("reportType", reportType);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedRouteId", routeId);
        model.addAttribute("selectedBusId", busId);
        model.addAttribute("routes", routes);
        model.addAttribute("buses", buses);
        model.addAttribute("reportSummary", summary);
        model.addAttribute("reportData", reportData);

        return "admin/reports";
    }

    @GetMapping("/reports/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "routeId", required = false) Long routeId,
            @RequestParam(value = "busId", required = false) Long busId
    ) {
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now();
        ReportSummaryDto summary = reportsService.getReportSummary(startDate, endDate, routeId, busId);
        List<ReportRowDto> reportData = reportsService.getReportData(startDate, endDate, routeId, busId);
        byte[] bytes = csvExportService.exportReportToCSV(summary, reportData);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv")
                .body(bytes);
    }

    @GetMapping("/reports/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "routeId", required = false) Long routeId,
            @RequestParam(value = "busId", required = false) Long busId
    ) {
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now();
        ReportSummaryDto summary = reportsService.getReportSummary(startDate, endDate, routeId, busId);
        List<ReportRowDto> reportData = reportsService.getReportData(startDate, endDate, routeId, busId);
        byte[] bytes = pdfExportService.exportReportToPDF(summary, reportData, startDate, endDate);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                .body(bytes);
    }
}
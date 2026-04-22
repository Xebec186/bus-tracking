package com.xebec.BusTracking.controller.api;

import com.xebec.BusTracking.dto.ReportRowDto;
import com.xebec.BusTracking.dto.ReportSummaryDto;
import com.xebec.BusTracking.service.CsvExportService;
import com.xebec.BusTracking.service.PdfExportService;
import com.xebec.BusTracking.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportsApiController {
    private final ReportsService reportsService;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;

    @GetMapping("/summary")
    public ReportSummaryDto summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Long busId
    ) {
        return reportsService.getReportSummary(startDate, endDate, routeId, busId);
    }

    @GetMapping("/data")
    public List<ReportRowDto> data(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Long busId
    ) {
        return reportsService.getReportData(startDate, endDate, routeId, busId);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Long busId
    ) {
        ReportSummaryDto summary = reportsService.getReportSummary(startDate, endDate, routeId, busId);
        List<ReportRowDto> data = reportsService.getReportData(startDate, endDate, routeId, busId);
        byte[] bytes = csvExportService.exportReportToCSV(summary, data);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv")
                .body(bytes);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Long busId
    ) {
        ReportSummaryDto summary = reportsService.getReportSummary(startDate, endDate, routeId, busId);
        List<ReportRowDto> data = reportsService.getReportData(startDate, endDate, routeId, busId);
        byte[] bytes = pdfExportService.exportReportToPDF(summary, data, startDate, endDate);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                .body(bytes);
    }
}

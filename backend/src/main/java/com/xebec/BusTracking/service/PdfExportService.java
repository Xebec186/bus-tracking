package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.ReportRowDto;
import com.xebec.BusTracking.dto.ReportSummaryDto;

import java.time.LocalDate;
import java.util.List;

public interface PdfExportService {
    byte[] exportReportToPDF(ReportSummaryDto summary, List<ReportRowDto> data, LocalDate start, LocalDate end);

    byte[] exportTicketToPDF(Long ticketId);

    byte[] exportTripSummaryToPDF(Long tripId);
}

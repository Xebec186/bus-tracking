package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.ReportRowDto;
import com.xebec.BusTracking.dto.ReportSummaryDto;

import java.time.LocalDate;
import java.util.List;

public interface CsvExportService {
    byte[] exportTicketsToCSV(LocalDate startDate, LocalDate endDate);

    byte[] exportTripsToCSV(LocalDate startDate, LocalDate endDate);

    byte[] exportReportToCSV(ReportSummaryDto summary, List<ReportRowDto> data);

    byte[] exportBusesListToCSV();

    byte[] exportRoutesListToCSV();
}

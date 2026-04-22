package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.ReportRowDto;
import com.xebec.BusTracking.dto.ReportSummaryDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportsService {

    ReportSummaryDto getReportSummary(LocalDate startDate, LocalDate endDate, Long routeId, Long busId);

    List<ReportRowDto> getReportData(LocalDate startDate, LocalDate endDate, Long routeId, Long busId);

    Map<String, Object> getChartData(LocalDate startDate, LocalDate endDate, Long routeId, Long busId);
}

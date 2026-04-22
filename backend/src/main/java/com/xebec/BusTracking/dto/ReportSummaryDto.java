package com.xebec.BusTracking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportSummaryDto {
    private long totalTickets;
    private String ticketGrowth;
    private double totalRevenue;
    private String revenueGrowth;
    private long activeRoutes;
    private double avgOccupancy;
    private String occupancyGrowth;
}

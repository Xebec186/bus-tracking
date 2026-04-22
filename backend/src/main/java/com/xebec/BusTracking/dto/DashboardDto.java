package com.xebec.BusTracking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DashboardDto {
    private DashboardStats stats;

    private List<Integer> weeklyTicketSales;

    private List<Double> weeklyRevenue;

    private List<RecentBookingDto> recentBookings;

    private List<ActivityDto> activityFeed;
}

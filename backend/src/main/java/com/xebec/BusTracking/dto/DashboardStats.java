package com.xebec.BusTracking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DashboardStats {
    private long totalUsers;
    private String userGrowth;

    private long totalBuses;
    private String busGrowth;

    private long activeRoutes;
    private String routeGrowth;

    private long activeTrips;
    private String tripGrowth;

    private int ticketsToday;
    private String ticketGrowth;
}

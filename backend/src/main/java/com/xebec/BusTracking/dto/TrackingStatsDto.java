package com.xebec.BusTracking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackingStatsDto {
    private long totalBuses;
    private long currentlyActive;
    private long inMaintenance;
    private long offline;
}


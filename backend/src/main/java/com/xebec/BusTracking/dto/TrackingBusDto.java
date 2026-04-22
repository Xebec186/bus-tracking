package com.xebec.BusTracking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackingBusDto {
    private String registrationNumber;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private TrackingStatus trackingStatus;
    private String currentRouteName;
    private BigDecimal speedKmh;
    private String lastUpdated;

    public enum TrackingStatus {
        MOVING,
        STOPPED
    }
}


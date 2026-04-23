package com.xebec.BusTracking.dto.response;

import com.xebec.BusTracking.model.RouteStop;
import com.xebec.BusTracking.model.Stop;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@ToString
@Getter
@Setter
public class RouteStopResponse {
    private Long id;
    private Long routeId;
    private Long stopId;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private Integer stopSequence;
    private Integer estimatedArrivalMinutes;

    public static RouteStopResponse from(RouteStop routeStop, Stop stop) {
        if(routeStop == null || stop == null) return null;
        return RouteStopResponse.builder()
                .id(routeStop.getId())
                .stopId(stop.getId())
                .routeId(routeStop.getRoute() != null ? routeStop.getRoute().getId() : null)
                .name(stop.getName())
                .latitude(stop.getLatitude())
                .longitude(stop.getLongitude())
                .description(stop.getDescription())
                .stopSequence(routeStop.getStopSequence())
                .estimatedArrivalMinutes(routeStop.getEstimatedArrivalMinutes())
                .build();
    }
}

package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.RouteStopDto;
import com.xebec.BusTracking.dto.response.RouteStopResponse;

import java.util.List;

public interface RouteStopService {
    RouteStopDto addRouteStop(RouteStopDto routeStopDto);

    RouteStopDto getRouteStopById(Long routeStopId);

    List<RouteStopDto> getAllRouteStops();

    List<RouteStopDto> getStopsByRouteId(Long routeId);

    List<RouteStopResponse> getStopsByRouteIdOrdered(Long routeId);

    RouteStopDto updateRouteStop(Long routeStopId, RouteStopDto routeStopDto);

    void deleteRouteStop(Long routeStopId);
}

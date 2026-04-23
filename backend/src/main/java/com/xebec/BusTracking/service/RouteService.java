package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.RouteDto;

import java.util.List;

public interface RouteService {
    RouteDto addRoute(RouteDto routeDto);

    RouteDto getRouteById(Long routeId);

    List<RouteDto> getAllRoutes();
    
    List<RouteDto> getActiveRoutes();

    RouteDto updateRoute(Long routeId, RouteDto routeDto);

    void deleteRoute(Long routeId);

    List<RouteDto> findByOriginAndDestination(String origin, String destination);

    long routeCount();
}

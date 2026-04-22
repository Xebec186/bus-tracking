package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.RouteDto;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.Route;
import com.xebec.BusTracking.model.RouteStop;
import com.xebec.BusTracking.model.Stop;
import com.xebec.BusTracking.repository.RouteRepository;
import com.xebec.BusTracking.repository.RouteStopRepository;
import com.xebec.BusTracking.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final ModelMapper modelMapper;

    @Override
    public RouteDto addRoute(RouteDto routeDto) {
        Route route = modelMapper.map(routeDto, Route.class);

        Route addedRoute = routeRepository.save(route);

        return modelMapper.map(addedRoute, RouteDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteDto getRouteById(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with given id: " + routeId));
        return enrich(modelMapper.map(route, RouteDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteDto> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(route -> enrich(modelMapper.map(route, RouteDto.class)))
                .toList();
    }

    private RouteDto enrich(RouteDto dto) {
        if (dto == null || dto.getId() == null) return dto;
        List<RouteStop> stops = routeStopRepository.findByRouteIdOrderByStopSequence(dto.getId());
        dto.setStopCount(stops.size());
        if (!stops.isEmpty()) {
            dto.setStartStopName(stops.get(0).getStop() != null ? stops.get(0).getStop().getName() : null);
            dto.setEndStopName(stops.get(stops.size() - 1).getStop() != null ? stops.get(stops.size() - 1).getStop().getName() : null);
        }
        return dto;
    }

    @Override
    public RouteDto updateRoute(Long routeId, RouteDto routeDto) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with given id: " + routeId));

        route.setName(routeDto.getName());
        route.setNumber(routeDto.getNumber());
        route.setDistanceKm(routeDto.getDistanceKm());
        route.setEstimatedDurationMinutes(routeDto.getEstimatedDurationMinutes());

        Route updatedRoute = routeRepository.save(route);

        return modelMapper.map(updatedRoute, RouteDto.class);
    }

    @Override
    public void deleteRoute(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with given id: " + routeId));
        routeRepository.delete(route);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteDto> findByOriginAndDestination(String origin, String destination) {
        String normalizedOrigin = origin == null ? "" : origin.trim().toLowerCase(Locale.ROOT);
        String normalizedDestination = destination == null ? "" : destination.trim().toLowerCase(Locale.ROOT);

        return routeRepository.findAll().stream()
                .filter(route -> routeContainsStops(route.getId(), normalizedOrigin, normalizedDestination))
                .map(route -> enrich(modelMapper.map(route, RouteDto.class)))
                .toList();
    }

    private boolean routeContainsStops(Long routeId, String origin, String destination) {
        List<RouteStop> routeStops = routeStopRepository.findByRouteIdOrderByStopSequence(routeId);
        Set<String> stopNames = routeStops.stream()
                .map(RouteStop::getStop)
                .map(Stop::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return stopNames.contains(origin) && stopNames.contains(destination);
    }
}

package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.RouteStopDto;
import com.xebec.BusTracking.dto.response.RouteStopResponse;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.Route;
import com.xebec.BusTracking.model.RouteStop;
import com.xebec.BusTracking.model.Stop;
import com.xebec.BusTracking.repository.RouteRepository;
import com.xebec.BusTracking.repository.RouteStopRepository;
import com.xebec.BusTracking.repository.StopRepository;
import com.xebec.BusTracking.service.RouteStopService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class RouteStopServiceImpl implements RouteStopService {

    private final ModelMapper modelMapper;
    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;

    @Override
    public RouteStopDto addRouteStop(RouteStopDto routeStopDto) {
        RouteStop routeStop = modelMapper.map(routeStopDto, RouteStop.class);

        Long routeId = routeStopDto.getRouteId();
        Long stopId = routeStopDto.getStopId();
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with given id: " + routeId));
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with given id: " + stopId));

        routeStop.setStop(stop);
        routeStop.setRoute(route);

        RouteStop addedRouteStop = routeStopRepository.save(routeStop);
        return modelMapper.map(addedRouteStop, RouteStopDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteStopDto getRouteStopById(Long routeStopId) {
        RouteStop routeStop = routeStopRepository.findById(routeStopId)
                .orElseThrow(() -> new ResourceNotFoundException("Route stop not found with given id: " + routeStopId));
        return modelMapper.map(routeStop, RouteStopDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteStopDto> getAllRouteStops() {
        return routeStopRepository.findAll().stream()
                .map(routeStop -> modelMapper.map(routeStop, RouteStopDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteStopDto> getStopsByRouteId(Long routeId) {
        return routeStopRepository.findByRouteId(routeId).stream()
                .map(routeStop -> modelMapper.map(routeStop, RouteStopDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteStopResponse> getStopsByRouteIdOrdered(Long routeId) {
        List<RouteStop> routeStops = routeStopRepository.findByRouteIdOrderByStopSequence(routeId);
        List<RouteStopResponse> routeStopResponses = new ArrayList<>();
        routeStops.forEach(routeStop -> {
            RouteStopResponse routeStopResponse = RouteStopResponse.from(routeStop, routeStop.getStop());
            routeStopResponses.add(routeStopResponse);
        });

        return routeStopResponses;
    }

    @Override
    public RouteStopDto updateRouteStop(Long routeStopId, RouteStopDto routeStopDto) {
        RouteStop routeStop = routeStopRepository.findById(routeStopId)
                .orElseThrow(() -> new ResourceNotFoundException("Route stop not found with given id: " + routeStopId));

        routeStop.setStopSequence(routeStopDto.getStopSequence());
        routeStop.setEstimatedArrivalMinutes(routeStopDto.getEstimatedArrivalMinutes());

        RouteStop updatedRouteStop = routeStopRepository.save(routeStop);
        return modelMapper.map(updatedRouteStop, RouteStopDto.class);
    }

    @Override
    public void deleteRouteStop(Long routeStopId) {
        RouteStop routeStop = routeStopRepository.findById(routeStopId)
                .orElseThrow(() -> new ResourceNotFoundException("Route stop not found with given id: " + routeStopId));
        routeStopRepository.delete(routeStop);
    }
}

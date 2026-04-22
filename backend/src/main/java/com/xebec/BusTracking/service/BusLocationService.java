package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.BusLocationDto;

import java.util.List;

public interface BusLocationService {
    BusLocationDto addBusLocation(BusLocationDto busLocationDtoDto);

    BusLocationDto getBusLocationById(Long busLocationId);

    List<BusLocationDto> getAllBusLocations();

    BusLocationDto getLatestLocationByBusId(Long busId);

    BusLocationDto updateBusLocation(Long busLocationId, BusLocationDto busLocationDto);

    void deleteBusLocation(Long busLocationId);
}

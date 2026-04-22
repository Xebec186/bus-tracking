package com.xebec.BusTracking.service;

import com.xebec.BusTracking.dto.BusDto;

import java.util.List;

public interface BusService {
    BusDto addBus(BusDto busDto);

    BusDto getBusById(Long busId);

    List<BusDto> getAllBuses();

    BusDto updateBus(Long busId, BusDto busDto);

    void deleteBus(Long busId);

    void assignDriver(Long busId, Long driverId);

    void removeDriver(Long busId);
}

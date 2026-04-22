package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.BusLocationDto;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.Bus;
import com.xebec.BusTracking.model.BusLocation;
import com.xebec.BusTracking.repository.BusLocationRepository;
import com.xebec.BusTracking.repository.BusRepository;
import com.xebec.BusTracking.security.MyUserDetails;
import com.xebec.BusTracking.service.BusLocationService;
import com.xebec.BusTracking.service.RealtimePublisherService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BusLocationServiceImpl implements BusLocationService {

    private final ModelMapper modelMapper;
    private final BusLocationRepository busLocationRepository;
    private final BusRepository busRepository;
    private final RealtimePublisherService realtimePublisherService;

    @Override
    public BusLocationDto addBusLocation(BusLocationDto busLocationDto) {
        BusLocation busLocation = modelMapper.map(busLocationDto, BusLocation.class);

        Long busId = busLocationDto.getBusId();
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with given id: " + busId));

        Long currentUserId = currentAuthenticatedUserId();
        if (bus.getDriver() == null || bus.getDriver().getId() == null || !bus.getDriver().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not allowed to update location for this bus");
        }

        busLocation.setBus(bus);

        BusLocation addedBusLocation = busLocationRepository.save(busLocation);
        realtimePublisherService.publishTrackingUpdate();
        return modelMapper.map(addedBusLocation, BusLocationDto.class);
    }

    private Long currentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof MyUserDetails principal)) {
            throw new AccessDeniedException("Authentication required");
        }
        return principal.getUser().getId();
    }

    @Override
    @Transactional(readOnly = true)
    public BusLocationDto getBusLocationById(Long busLocationId) {
        BusLocation busLocation = busLocationRepository.findById(busLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus location not found with id: " + busLocationId));
        return modelMapper.map(busLocation, BusLocationDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusLocationDto> getAllBusLocations() {
        return busLocationRepository.findAll().stream()
                .map((busLocation) -> modelMapper.map(busLocation, BusLocationDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BusLocationDto getLatestLocationByBusId(Long busId) {
        BusLocation busLocation = busLocationRepository
                .findTopByBusIdOrderByTimestampDesc(busId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No location found for bus with id: " + busId)
                );

        return modelMapper.map(busLocation, BusLocationDto.class);
    }

    @Override
    public BusLocationDto updateBusLocation(Long busLocationId, BusLocationDto busLocationDto) {
        BusLocation busLocation = busLocationRepository.findById(busLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus location not found with id: " + busLocationId));

        busLocation.setHeading(busLocationDto.getHeading());
        busLocation.setLongitude(busLocationDto.getLongitude());
        busLocation.setLatitude(busLocationDto.getLatitude());
        busLocation.setTimestamp(busLocationDto.getTimestamp());
        busLocation.setSpeed(busLocationDto.getSpeed());

        BusLocation updatedBusLocation = busLocationRepository.save(busLocation);
        realtimePublisherService.publishTrackingUpdate();

        return modelMapper.map(updatedBusLocation, BusLocationDto.class);
    }

    @Override
    public void deleteBusLocation(Long busLocationId) {
        BusLocation busLocation = busLocationRepository.findById(busLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus location not found with id: " + busLocationId));
        busLocationRepository.delete(busLocation);
    }
}

package com.xebec.BusTracking.service.impl;

import com.xebec.BusTracking.dto.StopDto;
import com.xebec.BusTracking.exception.ResourceNotFoundException;
import com.xebec.BusTracking.model.Stop;
import com.xebec.BusTracking.repository.StopRepository;
import com.xebec.BusTracking.service.StopService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StopServiceImpl implements StopService {

    private final StopRepository stopRepository;
    private final ModelMapper modelMapper;

    @Override
    public StopDto addStop(StopDto stopDto) {
        Stop stop = modelMapper.map(stopDto, Stop.class);
        Stop addedStop = stopRepository.save(stop);
        return modelMapper.map(addedStop, StopDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public StopDto getStopById(Long stopId) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with given id: " + stopId));
        return modelMapper.map(stop, StopDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StopDto> getAllStops() {
        return stopRepository.findAll().stream()
                .map(stop -> modelMapper.map(stop, StopDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StopDto> searchStopsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }

        return stopRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(stop -> modelMapper.map(stop, StopDto.class))
                .toList();
    }

    @Override
    public StopDto updateStop(Long stopId, StopDto stopDto) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with given id: " + stopId));

        stop.setName(stopDto.getName());
        stop.setLongitude(stopDto.getLongitude());
        stop.setLatitude(stopDto.getLatitude());
        stop.setDescription(stopDto.getDescription());

        Stop updatedStop = stopRepository.save(stop);

        return modelMapper.map(updatedStop, StopDto.class);
    }

    @Override
    public void deleteStop(Long stopId) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with given id: " + stopId));
        stopRepository.delete(stop);
    }
}

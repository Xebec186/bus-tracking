package com.xebec.BusTracking.repository;

import com.xebec.BusTracking.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<Stop, Long> {
    List<Stop> findByNameContainingIgnoreCase(String name);
}

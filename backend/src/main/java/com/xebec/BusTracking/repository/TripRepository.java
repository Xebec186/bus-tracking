package com.xebec.BusTracking.repository;

import com.xebec.BusTracking.model.Trip;
import com.xebec.BusTracking.model.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByStatus(TripStatus status);

    long countByStatus(TripStatus status);

    // Filter by underlying schedule.route
    List<Trip> findByScheduleRouteId(Long routeId);

    // Filter by bus directly
    List<Trip> findByBusId(Long busId);

    List<Trip> findByScheduleId(Long scheduleId);

    List<Trip> findByScheduleIdAndStatus(Long scheduleId, TripStatus status);

    // Filter by bus driver (if you add driver on Bus or Trip)
    List<Trip> findByBusDriverId(Long driverId);

    // By actualDepartureTime window
    List<Trip> findByActualDepartureTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<Trip> findByBusIdAndStatus(Long busId, TripStatus status);

    @Query("select t from Trip t join fetch t.schedule s join fetch s.route where t.bus.id = :busId and t.status = :status")
    List<Trip> findByBusIdAndStatusWithScheduleAndRoute(@Param("busId") Long busId, @Param("status") TripStatus status);

    @Query("SELECT t FROM Trip t JOIN FETCH t.schedule s JOIN FETCH s.route JOIN FETCH t.bus WHERE t.status = :status")
    List<Trip> findByStatusWithScheduleAndRoute(@Param("status") TripStatus status);
}

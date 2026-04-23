package com.xebec.BusTracking.repository;

import com.xebec.BusTracking.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query("SELECT r FROM Route r LEFT JOIN FETCH r.routeStops rs LEFT JOIN FETCH rs.stop")
    List<Route> findAllWithStops();

    @Query("SELECT DISTINCT r FROM Route r JOIN r.schedules s WHERE s.status = com.xebec.BusTracking.model.ScheduleStatus.ACTIVE")
    List<Route> findAllWithActiveSchedules();
}

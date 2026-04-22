package com.xebec.BusTracking.repository;

import com.xebec.BusTracking.model.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    List<RouteStop> findByRouteId(Long routeId);

//    List<RouteStop> findByRouteIdOrderByStopSequence(Long routeId);

    @Query("SELECT rs FROM RouteStop rs JOIN FETCH rs.stop WHERE rs.route.id = :routeId ORDER BY rs.stopSequence ASC")
    List<RouteStop> findByRouteIdOrderByStopSequence(@Param("routeId") Long routeId);

    @Query("select rs from RouteStop rs join fetch rs.stop where rs.route.id = :routeId order by rs.stopSequence asc")
    List<RouteStop> findByRouteIdOrderByStopSequenceWithStops(@Param("routeId") Long routeId);
}

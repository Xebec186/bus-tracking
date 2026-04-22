package com.xebec.BusTracking.repository;

import com.xebec.BusTracking.model.BusLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusLocationRepository extends JpaRepository<BusLocation, Long> {
    Optional<BusLocation> findTopByBusIdOrderByTimestampDesc(Long busId);
    void deleteByTimestampBefore(LocalDateTime threshold);

    @Query("""
    SELECT b FROM BusLocation b
    WHERE b.timestamp = (
        SELECT MAX(b2.timestamp) FROM BusLocation b2 WHERE b2.bus.id = b.bus.id
    )
    AND b.timestamp >= :cutoff
    """)
    List<BusLocation> findLatestLocationsNewerThan(@Param("cutoff") LocalDateTime cutoff);
}

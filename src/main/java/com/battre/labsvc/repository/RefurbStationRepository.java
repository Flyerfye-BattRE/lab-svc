package com.battre.labsvc.repository;

import com.battre.labsvc.model.RefurbStationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface RefurbStationRepository extends JpaRepository<RefurbStationType, Integer> {
    @Query("SELECT rst.refurbStnId, rsct.stationClass " +
            "FROM RefurbStationType AS rst " +
            "INNER JOIN RefurbStationClassType AS rsct ON rsct.refurbStationClassId = rst.refurbStationClassId " +
            "WHERE rst.inUse = false AND rst.activeBatteryId IS NULL " +
            "ORDER BY rst.lastActiveDate ASC")
    List<Object[]> getAvailableRefurbStns();

    @Transactional
    @Modifying
    @Query("UPDATE RefurbStationType " +
            "SET inUse = true, activeBatteryId = :batteryId, lastActiveDate = :lastActiveDate " +
            "WHERE refurbStnId = :refurbStnId")
    void markRefurbStnInUse(@Param("refurbStnId") int refurbStnId,
                            @Param("batteryId") int batteryId,
                            @Param("lastActiveDate") Timestamp lastActiveDate);

    @Transactional
    @Modifying
    @Query("UPDATE RefurbStationType " +
            "SET inUse = false, activeBatteryId = NULL, lastActiveDate = :lastActiveDate " +
            "WHERE refurbStnId = :refurbStnId")
    void markRefurbStnFree(@Param("refurbStnId") int refurbStnId,
                           @Param("lastActiveDate") Timestamp lastActiveDate);
}

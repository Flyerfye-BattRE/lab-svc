package com.battre.labsvc.repository;

import com.battre.labsvc.model.RefurbStationType;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RefurbStationRepository extends JpaRepository<RefurbStationType, Integer> {
    // Leverages JPA built in query func
    RefurbStationType findByRefurbStnId(int refurbStnId);

    @Query("SELECT rst.refurbStnId, rsct.stationClass " +
            "FROM RefurbStationType AS rst " +
            "INNER JOIN RefurbStationClassType AS rsct ON rsct.refurbStationClassId = rst.refurbStationClassId " +
            "WHERE rst.inUse = false AND rst.activeBatteryId IS NULL " +
            "ORDER BY rst.lastActiveDate ASC")
    List<Object[]> getAvailableRefurbStns();

    @Query("SELECT rst " +
            "FROM RefurbStationType AS rst " +
            "ORDER BY refurbStnId ")
    List<RefurbStationType> getRefurbStationLogs();

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

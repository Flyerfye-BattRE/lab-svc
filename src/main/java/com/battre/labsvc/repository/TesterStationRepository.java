package com.battre.labsvc.repository;

import com.battre.labsvc.model.TesterStationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface TesterStationRepository extends JpaRepository<TesterStationType, Integer> {
    // Leverages JPA built in query func
    TesterStationType findByTesterStnId(int testerStnId);

    @Query("SELECT tst " +
            "FROM TesterStationType AS tst " +
            "WHERE inUse = false AND activeBatteryId IS NULL " +
            "ORDER BY lastActiveDate ASC")
    List<TesterStationType> getAvailableTesterStations();

    @Query("SELECT tst " +
            "FROM TesterStationType AS tst " +
            "ORDER BY testerStnId ")
    List<TesterStationType> getTesterStationLogs();

    @Transactional
    @Modifying
    @Query("UPDATE TesterStationType " +
            "SET inUse = true, activeBatteryId = :batteryId, lastActiveDate = :lastActiveDate " +
            "WHERE testerStnId = :testerId")
    void markTesterInUse(@Param("testerId") int testerId,
                         @Param("batteryId") int batteryId,
                         @Param("lastActiveDate") Timestamp lastActiveDate);

    @Transactional
    @Modifying
    @Query("UPDATE TesterStationType " +
            "SET inUse = false, activeBatteryId = NULL, lastActiveDate = :lastActiveDate " +
            "WHERE testerStnId = :testerId")
    void markTesterFree(@Param("testerId") int testerId,
                        @Param("lastActiveDate") Timestamp lastActiveDate);
}

package com.battre.labsvc.repository;

import com.battre.labsvc.model.RefurbPlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefurbPlanRepository extends JpaRepository<RefurbPlanType, Integer> {
    // Leverages JPA built in query func
    RefurbPlanType findByRefurbPlanId(int refurbPlanId);

    @Query("SELECT refurbPlanId " +
            "FROM RefurbPlanType " +
            "WHERE batteryId = :batteryId AND refurbPlanEndDate IS NULL " +
            "ORDER BY refurbPlanId ASC " +
            "LIMIT 1 ")
    Optional<Integer> getPendingRefurbPlanForBatteryId(@Param("batteryId") int batteryId);

    @Transactional
    @Modifying
    @Query("UPDATE RefurbPlanType " +
            "SET available = false " +
            "WHERE refurbPlanId = :refurbPlanId")
    void markRefurbPlanBusy(@Param("refurbPlanId") int refurbPlanId);

    @Transactional
    @Modifying
    @Query("UPDATE RefurbPlanType " +
            "SET available = true " +
            "WHERE refurbPlanId = :refurbPlanId")
    void markRefurbPlanAvail(@Param("refurbPlanId") int refurbPlanId);

    // Form a combined result where true indicates all required refurb actions are complete
    @Query("SELECT CASE WHEN " +
            // check if needed resolder still needs to be performed
            "CASE WHEN resolder = TRUE AND resolderRecordId IS NULL THEN FALSE ELSE TRUE END = TRUE AND " +
            // check if needed repack still needs to be performed
            "CASE WHEN repack = TRUE AND repackRecordId IS NULL THEN FALSE ELSE TRUE END = TRUE AND " +
            // check if needed processorSwap still needs to be performed
            "CASE WHEN processorSwap = TRUE AND processorSwapRecordId IS NULL THEN FALSE ELSE TRUE END = TRUE AND " +
            // check if needed capacitorSwap still needs to be performed
            "CASE WHEN capacitorSwap = TRUE AND capacitorSwapRecordId IS NULL THEN FALSE ELSE TRUE END = TRUE " +
            "THEN TRUE ELSE FALSE END " +
            "FROM RefurbPlanType " +
            "WHERE refurbPlanId = :refurbPlanId")
    Boolean checkRefurbPlanCompleted(@Param("refurbPlanId") int refurbPlanId);

    @Transactional
    @Modifying
    @Query("UPDATE RefurbPlanType " +
            "SET refurbPlanEndDate = :refurbPlanEndDate " +
            "WHERE refurbPlanId = :refurbPlanId")
    void endRefurbPlanEntry(@Param("refurbPlanId") int refurbPlanId,
                            @Param("refurbPlanEndDate") Timestamp refurbPlanEndDate);

    @Transactional
    @Modifying
    @Query("UPDATE RefurbPlanType " +
            "SET refurbPlanPriority = :refurbPlanPriority " +
            "WHERE batteryId = :batteryId")
    void setBatteryRefurbPriority(@Param("batteryId") int batteryId,
                                  @Param("refurbPlanPriority") int refurbPlanPriority);

    @Transactional
    @Modifying
    @Query("UPDATE RefurbPlanType " +
            "SET resolderRecordId = :resolderRecordId " +
            "WHERE refurbPlanId = :refurbPlanId")
    void setResolderRecord(@Param("refurbPlanId") int refurbPlanId,
                           @Param("resolderRecordId") int resolderRecordId);

    @Transactional
    @Modifying
    @Query("UPDATE RefurbPlanType " +
            "SET repackRecordId = :repackRecordId " +
            "WHERE refurbPlanId = :refurbPlanId")
    void setRepackRecord(@Param("refurbPlanId") int refurbPlanId,
                         @Param("repackRecordId") int repackRecordId);

    @Transactional
    @Modifying
    @Query("UPDATE RefurbPlanType " +
            "SET processorSwapRecordId = :processorSwapRecordId " +
            "WHERE refurbPlanId = :refurbPlanId")
    void setProcessorSwapRecord(@Param("refurbPlanId") int refurbPlanId,
                                @Param("processorSwapRecordId") int processorSwapRecordId);

    @Transactional
    @Modifying
    @Query("UPDATE RefurbPlanType " +
            "SET capacitorSwapRecordId = :capacitorSwapRecordId " +
            "WHERE refurbPlanId = :refurbPlanId")
    void setCapacitorSwapRecord(@Param("refurbPlanId") int refurbPlanId,
                                @Param("capacitorSwapRecordId") int capacitorSwapRecordId);

    @Query("SELECT refurbPlanId, batteryId, " +
            // needResolder
            "CASE WHEN resolder = TRUE AND resolderRecordId IS NULL THEN TRUE ELSE FALSE END, " +
            // needRepack
            "CASE WHEN repack = TRUE AND repackRecordId IS NULL THEN TRUE ELSE FALSE END, " +
            // needProcessorSwap
            "CASE WHEN processorSwap = TRUE AND processorSwapRecordId IS NULL THEN TRUE ELSE FALSE END, " +
            // needCapacitorSwap
            "CASE WHEN capacitorSwap = TRUE AND capacitorSwapRecordId IS NULL THEN TRUE ELSE FALSE END " +
            "FROM RefurbPlanType " +
            "WHERE refurbPlanEndDate IS NULL AND available = TRUE " +
            "ORDER BY refurbPlanPriority ASC, refurbPlanId ASC ")
    List<Object[]> getCurrentRefurbSchemeStatuses();

    @Query("SELECT rpt " +
            "FROM RefurbPlanType AS rpt " +
            "WHERE refurbPlanEndDate IS NULL " +
            "ORDER BY refurbPlanPriority ASC, refurbPlanId ASC ")
    List<RefurbPlanType> getCurrentRefurbPlans();

    @Query("SELECT rpt " +
            "FROM RefurbPlanType AS rpt " +
            "ORDER BY refurbPlanPriority ASC, refurbPlanId ASC " +
            "LIMIT 1000")
    List<RefurbPlanType> getRefurbPlans();
}

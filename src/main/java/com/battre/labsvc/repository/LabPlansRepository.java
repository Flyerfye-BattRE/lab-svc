package com.battre.labsvc.repository;

import com.battre.labsvc.model.LabPlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface LabPlansRepository extends JpaRepository<LabPlanType, Integer> {
    // Used to update lab plan and validate that there is only one active lab plan per battery
    @Query("SELECT labPlanId " +
            "FROM LabPlanType " +
            "WHERE batteryId = :batteryId AND labPlanEndDate IS NULL " +
            "ORDER BY labPlanId DESC")
    List<Integer> getLabPlansForBatteryId(@Param("batteryId") int batteryId);

    @Transactional
    @Modifying
    @Query("UPDATE LabPlanType " +
            "SET testerRecordId = :testerRecordId " +
            "WHERE labPlanId = :labPlanId")
    void setTesterRecordForLabPlan(@Param("labPlanId") int labPlanId, @Param("testerRecordId") int testerRecordId);

    @Transactional
    @Modifying
    @Query("UPDATE LabPlanType " +
            "SET refurbPlanId = :refurbPlanId " +
            "WHERE labPlanId = :labPlanId")
    void setRefurbPlanForLabPlan(@Param("labPlanId") int labPlanId, @Param("refurbPlanId") int refurbPlanId);


    @Transactional
    @Modifying
    @Query("UPDATE LabPlanType " +
            "SET labPlanEndDate = :labPlanEndDate " +
            "WHERE labPlanId = :labPlanId")
    void endLabPlan(@Param("labPlanId") int labPlanId, @Param("labPlanEndDate") Timestamp labPlanEndDate);
}

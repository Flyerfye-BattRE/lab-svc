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
  // Leverages JPA built in query func
  LabPlanType findByLabPlanId(int labPlanId);

  LabPlanType findByRefurbPlanId(int refurbPlanId);

  LabPlanType findByBatteryId(int batteryId);

  @Query(
      "SELECT labPlanId "
          + "FROM LabPlanType "
          + "WHERE batteryId = :batteryId AND labPlanEndDate IS NULL "
          + "ORDER BY labPlanId DESC")
  List<Integer> getLabPlanIdsForBatteryId(@Param("batteryId") int batteryId);

  @Query(
      "SELECT lpt "
          + "FROM LabPlanType AS lpt "
          + "WHERE lpt.labPlanEndDate IS NULL "
          + "ORDER BY labPlanId")
  List<LabPlanType> getCurrentLabPlans();

  @Query("SELECT lpt " + "FROM LabPlanType AS lpt " + "ORDER BY labPlanId " + "LIMIT 1000")
  List<LabPlanType> getLabPlans();

  @Query("SELECT lpst.status, COUNT(lpt.labPlanId) AS planCount "
          + "FROM LabPlanType lpt "
          + "JOIN LabPlanStatusType lpst ON lpt.labPlanStatusId = lpst.labPlanStatusId "
          + "GROUP BY lpst.status")
  List<Object[]>  getLabPlanStatusCounts();

  @Transactional
  @Modifying
  @Query(
      "UPDATE LabPlanType "
          + "SET testerRecordId = :testerRecordId "
          + "WHERE labPlanId = :labPlanId")
  void setTesterRecordForLabPlan(
      @Param("labPlanId") int labPlanId, @Param("testerRecordId") int testerRecordId);

  @Transactional
  @Modifying
  @Query(
      "UPDATE LabPlanType " + "SET refurbPlanId = :refurbPlanId " + "WHERE labPlanId = :labPlanId")
  void setRefurbPlanForLabPlan(
      @Param("labPlanId") int labPlanId, @Param("refurbPlanId") int refurbPlanId);

  @Transactional
  @Modifying
  @Query(
      "UPDATE LabPlanType "
          + "SET labPlanStatusId = ("
          + "SELECT labPlanStatusId "
          + "FROM LabPlanStatusType "
          + "WHERE status = :planStatus"
          + ") "
          + "WHERE labPlanId = :labPlanId")
  void setPlanStatusesForPlanId(
      @Param("labPlanId") int labPlanId, @Param("planStatus") String planStatus);

  @Transactional
  @Modifying
  @Query(
      "UPDATE LabPlanType "
          + "SET labPlanEndDate = :labPlanEndDate "
          + "WHERE labPlanId = :labPlanId")
  void endLabPlan(
      @Param("labPlanId") int labPlanId, @Param("labPlanEndDate") Timestamp labPlanEndDate);

  @Transactional
  @Modifying
  @Query(
      "UPDATE LabPlanType "
          + "SET labPlanEndDate = :labPlanEndDate "
          + "WHERE refurbPlanId = :refurbPlanId")
  void endLabPlanEntryForRefurbPlan(
      @Param("refurbPlanId") int refurbPlanId, @Param("labPlanEndDate") Timestamp labPlanEndDate);
}

package com.battre.labsvc.repository;

import com.battre.labsvc.model.TesterBacklogType;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TesterBacklogRepository extends JpaRepository<TesterBacklogType, Integer> {
  // Leverages JPA built in query func
  TesterBacklogType findByTesterBacklogId(int testerBacklogId);

  @Query(
      "SELECT testerBacklogId "
          + "FROM TesterBacklogType "
          + "WHERE batteryId = :batteryId AND testerBacklogEndDate IS NULL "
          + "ORDER BY testerBacklogId ASC "
          + "LIMIT 1")
  Optional<Integer> getCurrentTesterBacklogForBatteryId(@Param("batteryId") int batteryId);

  @Transactional
  @Modifying
  @Query(
      "UPDATE TesterBacklogType "
          + "SET testerBacklogPriority = :testerBacklogPriority "
          + "WHERE batteryId = :batteryId")
  void setBatteryTesterPriority(
      @Param("batteryId") int batteryId, @Param("testerBacklogPriority") int testerBacklogPriority);

  @Query(
      "SELECT tbt "
          + "FROM TesterBacklogType AS tbt "
          + "WHERE testerBacklogEndDate IS NULL "
          + "ORDER BY testerBacklogPriority ASC, testerBacklogId ASC")
  List<TesterBacklogType> getCurrentTesterBacklog();

  @Query(
      "SELECT tbt "
          + "FROM TesterBacklogType AS tbt "
          + "ORDER BY testerBacklogPriority ASC, testerBacklogId ASC "
          + "LIMIT 1000")
  List<TesterBacklogType> getTesterBacklog();

  @Transactional
  @Modifying
  @Query(
      "UPDATE TesterBacklogType "
          + "SET testerBacklogEndDate = :testerBacklogEndDate "
          + "WHERE testerBacklogId = :testerBacklogId")
  void endTesterBacklogEntry(
      @Param("testerBacklogId") int testerBacklogId,
      @Param("testerBacklogEndDate") Timestamp testerBacklogEndDate);
}

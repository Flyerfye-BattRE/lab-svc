package com.battre.labsvc.repository;

import com.battre.labsvc.model.TesterBacklogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface TesterBacklogRepository extends JpaRepository<TesterBacklogType, Integer> {
    // Leverages JPA built in query func
    TesterBacklogType findByTesterBacklogId(int testerBacklogId);

    @Query("SELECT testerBacklogId, terminalLayoutId, testSchemeId, batteryId " +
            "FROM TesterBacklogType " +
            "WHERE testerBacklogEndDate IS NULL " +
            "ORDER BY testerBacklogPriority ASC, testerBacklogId ASC ")
    List<Object[]> getPendingTesterBacklog();

    @Transactional
    @Modifying
    @Query("UPDATE TesterBacklogType " +
            "SET testerBacklogEndDate = :testerBacklogEndDate " +
            "WHERE testerBacklogId = :testerBacklogId")
    void endTesterBacklogEntry(@Param("testerBacklogId") int testerBacklogId, @Param("testerBacklogEndDate") Timestamp testerBacklogEndDate);
}

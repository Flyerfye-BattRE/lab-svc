package com.battre.labsvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "TesterBacklog", schema = "LabSvcSchema")
public class TesterBacklogType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tester_backlog_id")
    private Integer testerBacklogId;

    @Column(name = "terminal_layout_id")
    private Integer terminalLayoutId;

    @Column(name = "test_scheme_id")
    private Integer testSchemeId;

    @Column(name = "battery_id")
    private Integer batteryId;

    @Column(name = "tester_backlog_priority")
    private Integer testerBacklogPriority;

    @Column(name = "tester_backlog_start_date")
    private Timestamp testerBacklogStartDate;

    @Column(name = "tester_backlog_end_date")
    private Timestamp testerBacklogEndDate;

    public TesterBacklogType() {
        // Default constructor for Spring Data JPA
    }

    public TesterBacklogType(int batteryId, int testSchemeId, int terminalLayoutId) {
        this.batteryId = batteryId;
        this.testSchemeId = testSchemeId;
        this.terminalLayoutId = terminalLayoutId;
        this.testerBacklogPriority = 50;
        this.testerBacklogStartDate = Timestamp.from(Instant.now());
    }

    public Integer getTesterBacklogId() {
        return testerBacklogId;
    }

    public void setTesterBacklogId(Integer testerBacklogId) {
        this.testerBacklogId = testerBacklogId;
    }

    public Integer getTerminalLayoutId() {
        return terminalLayoutId;
    }

    public void setTerminalLayoutId(Integer terminalLayoutId) {
        this.terminalLayoutId = terminalLayoutId;
    }

    public Integer getTestSchemeId() {
        return testSchemeId;
    }

    public void setTestSchemeId(Integer testSchemeId) {
        this.testSchemeId = testSchemeId;
    }

    public Integer getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(Integer batteryId) {
        this.batteryId = batteryId;
    }

    public Integer getTesterBacklogPriority() {
        return testerBacklogPriority;
    }

    public void setTesterBacklogPriority(Integer testerBacklogPriority) {
        this.testerBacklogPriority = testerBacklogPriority;
    }

    public Timestamp getTesterBacklogStartDate() {
        return testerBacklogStartDate;
    }

    public void setTesterBacklogStartDate(Timestamp testerBacklogStartDate) {
        this.testerBacklogStartDate = testerBacklogStartDate;
    }

    public Timestamp getTesterBacklogEndDate() {
        return testerBacklogEndDate;
    }

    public void setTesterBacklogEndDate(Timestamp testerBacklogEndDate) {
        this.testerBacklogEndDate = testerBacklogEndDate;
    }
}

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
    private Long testerBacklogId;

    @Column(name = "terminal_layout_id")
    private int terminalLayoutId;

    @Column(name = "battery_id")
    private int batteryId;

    @Column(name = "tester_backlog_priority")
    private int testerBacklogPriority;

    @Column(name = "tester_backlog_start_date")
    private Timestamp testerBacklogStartDate;

    @Column(name = "tester_backlog_end_date")
    private Timestamp testerBacklogEndDate;

    public TesterBacklogType(int batteryId, int terminalLayoutId) {
        this.batteryId = batteryId;
        this.terminalLayoutId = terminalLayoutId;
        this.testerBacklogPriority = 50;
        this.testerBacklogStartDate = Timestamp.from(Instant.now());
    }

    public Long getTesterBacklogId() {
        return testerBacklogId;
    }

    public void setTesterBacklogId(Long testerBacklogId) {
        this.testerBacklogId = testerBacklogId;
    }

    public int getTerminalLayoutId() {
        return terminalLayoutId;
    }

    public void setTerminalLayoutId(int terminalLayoutId) {
        this.terminalLayoutId = terminalLayoutId;
    }

    public int getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(int batteryId) {
        this.batteryId = batteryId;
    }

    public int getTesterBacklogPriority() {
        return testerBacklogPriority;
    }

    public void setTesterBacklogPriority(int testerBacklogPriority) {
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

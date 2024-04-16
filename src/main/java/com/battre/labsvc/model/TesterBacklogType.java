package com.battre.labsvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "TesterBacklog", schema = "LabSvcDb")
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
}

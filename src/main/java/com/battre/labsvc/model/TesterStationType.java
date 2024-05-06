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
@Table(name = "TesterStations", schema = "LabSvcSchema")
public class TesterStationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tester_stn_id")
    private Integer testerStnId;

    @Column(name = "terminal_layout_id", nullable = false)
    private Integer terminalLayoutId;

    @Column(name = "in_use", nullable = false)
    private Boolean inUse = false;

    @Column(name = "active_battery_id")
    private Integer activeBatteryId;

    @Column(name = "last_active_date")
    private Timestamp lastActiveDate;

    @Column(name = "last_calibration_date", nullable = false)
    private Timestamp lastCalibrationDate;

    @Column(name = "next_calibration_date", nullable = false)
    private Timestamp nextCalibrationDate;

    public Boolean getInUse() {
        return inUse;
    }

    public void setInUse(Boolean inUse) {
        this.inUse = inUse;
    }

    public Integer getActiveBatteryId() {
        return activeBatteryId;
    }

    public void setActiveBatteryId(Integer activeBatteryId) {
        this.activeBatteryId = activeBatteryId;
    }

    public Timestamp getLastActiveDate() {
        return lastActiveDate;
    }

    public void setLastActiveDate() {
        this.lastActiveDate = Timestamp.from(Instant.now());
    }
}
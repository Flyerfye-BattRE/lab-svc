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
@Table(name = "RefurbBacklog", schema = "LabSvcSchema")
public class RefurbBacklogType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refurb_backlog_id")
    private Integer refurbBacklogId;

    @Column(name = "refurb_plan_id", nullable = false)
    private Integer refurbPlanId;

    @Column(name = "battery_id", nullable = false)
    private Integer batteryId;

    @Column(name = "refurb_backlog_priority", nullable = false)
    private Integer refurbBacklogPriority;

    @Column(name = "refurb_backlog_start_date", nullable = false)
    private Timestamp refurbBacklogStartDate;

    @Column(name = "refurb_backlog_end_date")
    private Timestamp refurbBacklogEndDate;

    public RefurbBacklogType(Integer refurbPlanId, Integer batteryId) {
        this.refurbPlanId = refurbPlanId;
        this.batteryId = batteryId;
        this.refurbBacklogPriority = 50;
        this.refurbBacklogStartDate = Timestamp.from(Instant.now());
    }

    public Integer getRefurbBacklogId() {
        return refurbBacklogId;
    }

    public Integer getRefurbPlanId() {
        return refurbPlanId;
    }

    public void setRefurbPlanId(Integer refurbPlanId) {
        this.refurbPlanId = refurbPlanId;
    }

    public Integer getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(Integer batteryId) {
        this.batteryId = batteryId;
    }

    public Integer getRefurbBacklogPriority() {
        return refurbBacklogPriority;
    }

    public void setRefurbBacklogPriority(Integer refurbBacklogPriority) {
        this.refurbBacklogPriority = refurbBacklogPriority;
    }

    public Timestamp getRefurbBacklogStartDate() {
        return refurbBacklogStartDate;
    }

    public void setRefurbBacklogStartDate(Timestamp refurbBacklogStartDate) {
        this.refurbBacklogStartDate = refurbBacklogStartDate;
    }

    public Timestamp getRefurbBacklogEndDate() {
        return refurbBacklogEndDate;
    }

    public void setRefurbBacklogEndDate(Timestamp refurbBacklogEndDate) {
        this.refurbBacklogEndDate = refurbBacklogEndDate;
    }
}

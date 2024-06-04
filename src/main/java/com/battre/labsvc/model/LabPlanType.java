package com.battre.labsvc.model;

import com.battre.labsvc.enums.LabPlanStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "LabPlans", schema = "LabSvcSchema")
public class LabPlanType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lab_plan_id")
    private int labPlanId;

    @Column(name = "lab_plan_status_id")
    private int labPlanStatusId;

    @Column(name = "lab_plan_start_date")
    private Timestamp labPlanStartDate;

    @Column(name = "lab_plan_end_date")
    private Timestamp labPlanEndDate;

    @Column(name = "battery_id")
    private int batteryId;

    @Column(name = "tester_record_id")
    private int testerRecordId;

    @Column(name = "refurb_plan_id")
    private int refurbPlanId;

    public LabPlanType() {
        // Default constructor for Spring Data JPA
    }

    public LabPlanType(int batteryId) {
        this.batteryId = batteryId;
        this.labPlanStatusId = LabPlanStatusEnum.TESTER_BACKLOG_NEW.getStatusCode();
        this.labPlanStartDate = Timestamp.from(Instant.now());
    }

    public int getLabPlanId() {
        return labPlanId;
    }

    public int getLabPlanStatusId() {
        return labPlanStatusId;
    }

    public void setLabPlanStatusId(int labPlanStatusId) {
        this.labPlanStatusId = labPlanStatusId;
    }

    public Timestamp getLabPlanStartDate() {
        return labPlanStartDate;
    }

    public void setLabPlanStartDate(Timestamp labPlanStartDate) {
        this.labPlanStartDate = labPlanStartDate;
    }

    public Timestamp getLabPlanEndDate() {
        return labPlanEndDate;
    }

    public void setLabPlanEndDate(Timestamp labPlanEndDate) {
        this.labPlanEndDate = labPlanEndDate;
    }

    public int getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(int batteryId) {
        this.batteryId = batteryId;
    }

    public int getTesterRecordId() {
        return testerRecordId;
    }

    public void setTesterRecordId(int testerRecordId) {
        this.testerRecordId = testerRecordId;
    }

    public int getRefurbPlanId() {
        return refurbPlanId;
    }

    public void setRefurbPlanId(int refurbPlanId) {
        this.refurbPlanId = refurbPlanId;
    }
}
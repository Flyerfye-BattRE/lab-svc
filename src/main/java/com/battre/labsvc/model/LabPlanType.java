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
@Table(name = "LabPlans", schema = "LabSvcDb")
public class LabPlanType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lab_plan_id")
    private Long labPlanId;

    @Column(name = "lab_plan_start_date")
    private Timestamp labPlanStartDate;

    @Column(name = "lab_plan_end_date")
    private Timestamp labPlanEndDate;

    @Column(name = "battery_id")
    private int batteryId;


    @Column(name = "tester_record_id")
    @JoinColumn(name = "tester_record_id")
    private int testerRecordId;


    @Column(name = "refurb_plan_id")
    @JoinColumn(name = "refurb_plan_id")
    private int refurbPlanId;

    public LabPlanType(int batteryId) {
        this.batteryId = batteryId;
        this.labPlanStartDate = Timestamp.from(Instant.now());
    }
}
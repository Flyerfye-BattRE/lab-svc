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
@Table(name = "RefurbRecords", schema = "LabSvcSchema")
public class RefurbRecordType {
    @Column(name = "refurb_date", nullable = false)
    private Timestamp refurbDate;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refurb_record_id")
    private Integer refurbRecordId;
    @Column(name = "refurb_plan_id", nullable = false)
    private Integer refurbPlanId;
    @Column(name = "refurb_stn_id", nullable = false)
    private Integer refurbStnId;
    @Column(name = "station_class", nullable = false)
    private String stationClass;
    @Column(name = "battery_id", nullable = false)
    private Integer batteryId;
    @Column(name = "result_type_id", nullable = false)
    private Integer resultTypeId;

    public RefurbRecordType() {
        // Default constructor for Spring Data JPA
        Timestamp.from(Instant.now());
    }

    public RefurbRecordType(Integer refurbPlanId,
                            Integer refurbStnId,
                            String stationClass,
                            Integer batteryId,
                            Integer resultTypeId,
                            Timestamp refurbDate) {
        this.refurbStnId = refurbStnId;
        this.stationClass = stationClass;
        this.batteryId = batteryId;
        this.refurbPlanId = refurbPlanId;
        this.resultTypeId = resultTypeId;
        this.refurbDate = refurbDate;
    }

    public Integer getRefurbRecordId() {
        return refurbRecordId;
    }

    public Integer getRefurbStnId() {
        return refurbStnId;
    }

    public String getStationClass() {
        return stationClass;
    }

    public Integer getBatteryId() {
        return batteryId;
    }

    public Integer getRefurbPlanId() {
        return refurbPlanId;
    }

    public Integer getResultTypeId() {
        return resultTypeId;
    }

    public Timestamp getRefurbDate() {
        return refurbDate;
    }
}

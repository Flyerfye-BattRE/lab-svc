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
@Table(name = "TesterRecords", schema = "LabSvcSchema")
public class TesterRecordType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tester_record_id")
    private Integer testerRecordId;
    @Column(name = "tester_stn_id", nullable = false)
    private final Integer testerStnId;
    @Column(name = "battery_id", nullable = false)
    private final Integer batteryId;
    @Column(name = "test_scheme_id", nullable = false)
    private final Integer testSchemeId;
    @Column(name = "refurb_scheme_id", nullable = false)
    private final Integer refurbSchemeId;
    @Column(name = "result_type_id", nullable = false)
    private final Integer resultTypeId;
    @Column(name = "test_date", nullable = false)
    private final Timestamp testDate;

    public TesterRecordType() {
        // Default constructor for Spring Data JPA
        this.testerStnId = -1;
        this.batteryId = -1;
        this.testSchemeId = -1;
        this.refurbSchemeId = -1;
        this.resultTypeId = -1;
        this.testDate = Timestamp.from(Instant.now());
    }

    public TesterRecordType(Integer testerStnId,
                            Integer batteryId,
                            Integer testSchemeId,
                            Integer refurbSchemeId,
                            Integer resultTypeId,
                            Timestamp testDate) {
        this.testerStnId = testerStnId;
        this.batteryId = batteryId;
        this.testSchemeId = testSchemeId;
        this.refurbSchemeId = refurbSchemeId;
        this.resultTypeId = resultTypeId;
        this.testDate = testDate;
    }

    public Integer getTesterRecordId() {
        return testerRecordId;
    }

    // added for testing purposes
    public void setTesterRecordId(Integer testerRecordId) {
        this.testerRecordId = testerRecordId;
    }

    public Integer getTesterStnId() {
        return testerStnId;
    }

    public Integer getBatteryId() {
        return batteryId;
    }

    public Integer getTestSchemeId() {
        return testSchemeId;
    }

    public Integer getRefurbSchemeId() {
        return refurbSchemeId;
    }

    public Integer getResultTypeId() {
        return resultTypeId;
    }

    public Timestamp getTestDate() {
        return testDate;
    }
}
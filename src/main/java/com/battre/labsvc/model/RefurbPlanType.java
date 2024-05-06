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
@Table(name = "RefurbPlans", schema = "LabSvcSchema")
public class RefurbPlanType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refurb_plan_id")
    private Integer refurbPlanId;

    @Column(name = "battery_id", nullable = false)
    private final Integer batteryId;

    @Column(name = "refurb_start_date", nullable = false)
    private final Timestamp refurbStartDate;

    @Column(name = "refurb_end_date")
    private Timestamp refurbEndDate;

    @Column(name = "resolder")
    private Boolean resolder = false;

    @Column(name = "repack")
    private Boolean repack = false;

    @Column(name = "processor_swap")
    private Boolean processorSwap = false;

    @Column(name = "capacitor_swap")
    private Boolean capacitorSwap = false;

    @Column(name = "retest")
    private Boolean retest = false;

    public RefurbPlanType(Integer batteryId,
                          Boolean resolder,
                          Boolean repack,
                          Boolean processorSwap,
                          Boolean capacitorSwap,
                          Boolean retest) {
        this.batteryId = batteryId;
        this.refurbStartDate = Timestamp.from(Instant.now());
        this.resolder = resolder;
        this.repack = repack;
        this.processorSwap = processorSwap;
        this.capacitorSwap = capacitorSwap;
        this.retest = retest;
    }

    public Integer getRefurbPlanId() {
        return refurbPlanId;
    }

    public Integer getBatteryId() {
        return batteryId;
    }

    public Timestamp getRefurbStartDate() {
        return refurbStartDate;
    }

    public Timestamp getRefurbEndDate() {
        return refurbEndDate;
    }

    public Boolean getResolder() {
        return resolder;
    }

    public Boolean getRepack() {
        return repack;
    }

    public Boolean getProcessorSwap() {
        return processorSwap;
    }

    public Boolean getCapacitorSwap() {
        return capacitorSwap;
    }

    public Boolean getRetest() {
        return retest;
    }
}

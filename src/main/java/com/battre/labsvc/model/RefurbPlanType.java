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
    private Integer batteryId;

    @Column(name = "refurb_plan_priority", nullable = false)
    private Integer refurbPlanPriority;

    @Column(name = "refurb_plan_start_date", nullable = false)
    private Timestamp refurbPlanStartDate;

    @Column(name = "refurb_plan_end_date")
    private Timestamp refurbPlanEndDate;

    @Column(name = "available")
    private Boolean available = true;

    @Column(name = "resolder")
    private Boolean resolder = false;

    @Column(name = "resolder_record_id")
    private Integer resolderRecordId;

    @Column(name = "repack")
    private Boolean repack = false;

    @Column(name = "repack_record_id")
    private Integer repackRecordId;

    @Column(name = "processor_swap")
    private Boolean processorSwap = false;

    @Column(name = "processor_swap_record_id")
    private Integer processorSwapRecordId;

    @Column(name = "capacitor_swap")
    private Boolean capacitorSwap = false;

    @Column(name = "capacitor_swap_record_id")
    private Integer capacitorSwapRecordId;

    public RefurbPlanType() {
        // Default constructor for Spring Data JPA
        this.batteryId = -1;
        this.refurbPlanPriority = 50;
        this.refurbPlanStartDate = Timestamp.from(Instant.now());
    }

    public RefurbPlanType(Integer batteryId,
                          Boolean resolder,
                          Boolean repack,
                          Boolean processorSwap,
                          Boolean capacitorSwap) {
        this.batteryId = batteryId;
        this.refurbPlanPriority = 50;
        this.refurbPlanStartDate = Timestamp.from(Instant.now());
        this.resolder = resolder;
        this.repack = repack;
        this.processorSwap = processorSwap;
        this.capacitorSwap = capacitorSwap;
    }

    public Integer getRefurbPlanId() {
        return refurbPlanId;
    }

    public Integer getBatteryId() {
        return batteryId;
    }

    public Integer getRefurbPlanPriority() {
        return refurbPlanPriority;
    }

    public Timestamp getRefurbPlanStartDate() {
        return refurbPlanStartDate;
    }

    public Timestamp getRefurbPlanEndDate() {
        return refurbPlanEndDate;
    }

    public Boolean isAvailable() {
        return available;
    }

    public Boolean getResolder() {
        return resolder;
    }

    public Integer getResolderRecordId() {
        return resolderRecordId;
    }

    public Boolean getRepack() {
        return repack;
    }

    public Integer getRepackRecordId() {
        return repackRecordId;
    }

    public Boolean getProcessorSwap() {
        return processorSwap;
    }

    public Integer getProcessorSwapRecordId() {
        return processorSwapRecordId;
    }

    public Boolean getCapacitorSwap() {
        return capacitorSwap;
    }

    public Integer getCapacitorSwapRecordId() {
        return capacitorSwapRecordId;
    }
}

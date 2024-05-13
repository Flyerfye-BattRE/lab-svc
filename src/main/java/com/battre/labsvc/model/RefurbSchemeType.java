package com.battre.labsvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "RefurbSchemes", schema = "LabSvcSchema")
public class RefurbSchemeType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refurb_scheme_id")
    private Integer refurbSchemeId;

    @Column(name = "resolder", nullable = false)
    private boolean resolder;

    @Column(name = "repack", nullable = false)
    private boolean repack;

    @Column(name = "processor_swap", nullable = false)
    private boolean processorSwap;

    @Column(name = "capacitor_swap", nullable = false)
    private boolean capacitorSwap;

    public RefurbSchemeType() {
        // Default constructor for Spring Data JPA
    }

    public RefurbSchemeType(Integer refurbSchemeId, boolean resolder, boolean repack, boolean processorSwap, boolean capacitorSwap) {
        this.refurbSchemeId = refurbSchemeId;
        this.resolder = resolder;
        this.repack = repack;
        this.processorSwap = processorSwap;
        this.capacitorSwap = capacitorSwap;
    }

    public Integer getRefurbSchemeId() {
        return refurbSchemeId;
    }

    public boolean isResolder() {
        return resolder;
    }

    public boolean isRepack() {
        return repack;
    }

    public boolean isProcessorSwap() {
        return processorSwap;
    }

    public boolean isCapacitorSwap() {
        return capacitorSwap;
    }
}
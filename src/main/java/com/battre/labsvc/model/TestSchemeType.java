package com.battre.labsvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TestSchemes", schema = "LabSvcSchema")
public class TestSchemeType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_scheme_id")
    private Integer testSchemeId;

    @Column(name = "checkerboard", nullable = false)
    private boolean checkerboard;

    @Column(name = "null_line", nullable = false)
    private boolean nullLine;

    @Column(name = "vapor_sim", nullable = false)
    private boolean vaporSim;

    @Column(name = "blackout", nullable = false)
    private boolean blackout;

    @Column(name = "oven_screen", nullable = false)
    private boolean ovenScreen;

    public Integer getTestSchemeId() {
        return testSchemeId;
    }

    public boolean isCheckerboard() {
        return checkerboard;
    }

    public boolean isNullLine() {
        return nullLine;
    }

    public boolean isVaporSim() {
        return vaporSim;
    }

    public boolean isBlackout() {
        return blackout;
    }

    public boolean isOvenScreen() {
        return ovenScreen;
    }
}
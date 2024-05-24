package com.battre.labsvc.model;

import com.battre.labsvc.enums.RefurbStationClass;

import java.sql.Timestamp;

public record RefurbResultRecord(int refurbPlanId,
                                 int refurbStnId,
                                 RefurbStationClass refurbStnClass,
                                 int batteryId,
                                 int resultTypeId,
                                 Timestamp testDate) {
    @Override
    public String toString() {
        return "RefurbResultRecord{" +
                "refurbPlanId=" + refurbPlanId +
                ", refurbStnId=" + refurbStnId +
                ", refurbStnClass=" + refurbStnClass +
                ", batteryId=" + batteryId +
                ", resultTypeId=" + resultTypeId +
                ", testDate=" + testDate +
                '}';
    }
}
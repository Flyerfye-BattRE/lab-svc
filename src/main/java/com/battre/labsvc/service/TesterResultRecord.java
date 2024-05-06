package com.battre.labsvc.service;

import java.sql.Timestamp;

public record TesterResultRecord(int testerStnId, int batteryId, int terminalLayoutId, int testSchemeId,
                                 int resultTypeId, Timestamp testDate) {
    @Override
    public String toString() {
        return "TesterResultRecord{" +
                "testerStnId=" + testerStnId +
                ", batteryId=" + batteryId +
                ", terminalLayoutId=" + terminalLayoutId +
                ", testSchemeId=" + testSchemeId +
                ", resultTypeId=" + resultTypeId +
                ", testDate=" + testDate +
                '}';
    }
}
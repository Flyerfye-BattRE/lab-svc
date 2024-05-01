package com.battre.labsvc.service;

public record TesterResultRecord(int testerStnId, int batteryId, int testSchemeId, int resultTypeId) {
    @Override
    public String toString() {
        return "TesterResultRecord{" +
                "testerStnId=" + testerStnId +
                ", batteryId=" + batteryId +
                ", testSchemeId=" + testSchemeId +
                ", resultTypeId=" + resultTypeId +
                '}';
    }
}
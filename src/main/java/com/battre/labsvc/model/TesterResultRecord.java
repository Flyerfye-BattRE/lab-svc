package com.battre.labsvc.model;

import java.sql.Timestamp;

public record TesterResultRecord(
    int testerStnId,
    int batteryId,
    int testSchemeId,
    int refurbSchemeId,
    int terminalLayoutId,
    int resultTypeId,
    Timestamp testDate) {
  @Override
  public String toString() {
    return "TesterResultRecord{"
        + "testerStnId="
        + testerStnId
        + ", batteryId="
        + batteryId
        + ", terminalLayoutId="
        + terminalLayoutId
        + ", testSchemeId="
        + testSchemeId
        + ", refurbSchemeId="
        + refurbSchemeId
        + ", resultTypeId="
        + resultTypeId
        + ", testDate="
        + testDate
        + '}';
  }
}

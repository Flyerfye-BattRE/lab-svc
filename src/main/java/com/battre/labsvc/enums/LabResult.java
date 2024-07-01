package com.battre.labsvc.enums;

public enum LabResult {
  // Corresponds to values defined in the sql data init file under resources/initdb
  PASS(1),
  FAIL_RETRY(2),
  FAIL_REJECT(3),
  EXCEPTION(4);

  private final int statusCode;

  LabResult(int statusCode) {
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return this.statusCode;
  }
}

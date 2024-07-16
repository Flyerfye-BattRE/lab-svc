package com.battre.labsvc.enums;

import com.battre.stubs.services.LabPlanStatus;

public enum LabPlanStatusEnum {
  UNKNOWN(0, "UNKNOWN", LabPlanStatus.UNKNOWN),
  TESTER_BACKLOG_NEW(1, "TESTER BACKLOG-NEW", LabPlanStatus.TESTER_BACKLOG_NEW),
  TESTER_BACKLOG_RETRY(2, "TESTER BACKLOG-RETRY", LabPlanStatus.TESTER_BACKLOG_RETRY),
  TESTER_FAILED(3, "TESTER-FAILED", LabPlanStatus.TESTER_FAILED),
  REFURB_BACKLOG_NEW(4, "REFURB BACKLOG-NEW", LabPlanStatus.REFURB_BACKLOG_NEW),
  REFURB_BACKLOG_CONT(5, "REFURB BACKLOG-CONT", LabPlanStatus.REFURB_BACKLOG_CONT),
  REFURB_BACKLOG_RETRY(6, "REFURB BACKLOG-RETRY", LabPlanStatus.REFURB_BACKLOG_RETRY),
  REFURB_FAILED(7, "REFURB-FAILED", LabPlanStatus.REFURB_FAILED),
  PASS(8, "PASS", LabPlanStatus.PASS),
  DESTROYED(9, "DESTROYED", LabPlanStatus.DESTROYED),
  LOST(10, "LOST", LabPlanStatus.LOST);

  private final int statusCode;
  private final String statusDescription;
  private final LabPlanStatus grpcStatus;

  LabPlanStatusEnum(int statusCode, String statusDescription, LabPlanStatus grpcStatus) {
    this.statusCode = statusCode;
    this.statusDescription = statusDescription;
    this.grpcStatus = grpcStatus;
  }

  public static LabPlanStatusEnum fromStatusCode(int statusCode) {
    for (LabPlanStatusEnum status : values()) {
      if (status.statusCode == statusCode) {
        return status;
      }
    }
    return UNKNOWN;
  }

  public static LabPlanStatusEnum fromStatusDescription(String statusDescription) {
    for (LabPlanStatusEnum status : values()) {
      if (status.statusDescription.equals(statusDescription)) {
        return status;
      }
    }
    return UNKNOWN;
  }

  public static LabPlanStatusEnum fromGrpcStatus(LabPlanStatus grpcStatus) {
    for (LabPlanStatusEnum status : values()) {
      if (status.grpcStatus.equals(grpcStatus)) {
        return status;
      }
    }
    return UNKNOWN;
  }

  public int getStatusCode() {
    return this.statusCode;
  }

  public String getStatusDescription() {
    return this.statusDescription;
  }

  public LabPlanStatus getGrpcStatus() {
    return this.grpcStatus;
  }
}

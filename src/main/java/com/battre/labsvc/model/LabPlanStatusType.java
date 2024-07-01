package com.battre.labsvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "LabPlanStatus", schema = "LabSvcSchema")
public class LabPlanStatusType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "lab_plan_status_id")
  private int labPlanStatusId;

  @Column(name = "status", nullable = false, length = 45)
  private String status;

  public int getLabPlanStatusId() {
    return labPlanStatusId;
  }

  public void setLabPlanStatusId(int labPlanStatusId) {
    this.labPlanStatusId = labPlanStatusId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}

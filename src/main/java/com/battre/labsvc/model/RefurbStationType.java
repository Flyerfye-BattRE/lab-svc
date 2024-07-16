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
@Table(name = "RefurbStations", schema = "LabSvcSchema")
public class RefurbStationType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "refurb_stn_id")
  private Integer refurbStnId;

  @Column(name = "refurb_station_class_id", nullable = false)
  private Integer refurbStationClassId;

  @Column(name = "in_use")
  private boolean inUse = false;

  @Column(name = "active_battery_id")
  private Integer activeBatteryId;

  @Column(name = "last_active_date")
  private Timestamp lastActiveDate;

  @Column(name = "last_calibration_date", nullable = false)
  private Timestamp lastCalibrationDate;

  @Column(name = "next_calibration_date", nullable = false)
  private Timestamp nextCalibrationDate;

  public RefurbStationType() {
    // Default constructor for Spring Data JPA
  }

  public RefurbStationType(Integer refurbStnId, Integer refurbStationClassId) {
    this.refurbStnId = refurbStnId;
    this.refurbStationClassId = refurbStationClassId;
  }

  public Integer getRefurbStnId() {
    return refurbStnId;
  }

  public Integer getRefurbStationClassId() {
    return refurbStationClassId;
  }

  public void setRefurbStationClassId(Integer refurbStationClassId) {
    this.refurbStationClassId = refurbStationClassId;
  }

  public boolean isInUse() {
    return inUse;
  }

  public void setInUse(boolean inUse) {
    this.inUse = inUse;
  }

  public Integer getActiveBatteryId() {
    return activeBatteryId;
  }

  public void setActiveBatteryId(Integer activeBatteryId) {
    this.activeBatteryId = activeBatteryId;
  }

  public Timestamp getLastActiveDate() {
    return lastActiveDate;
  }

  public void setLastActiveDate() {
    this.lastActiveDate = Timestamp.from(Instant.now());
  }

  public Timestamp getLastCalibrationDate() {
    return lastCalibrationDate;
  }

  public void setLastCalibrationDate() {
    this.lastCalibrationDate = Timestamp.from(Instant.now());
  }

  public Timestamp getNextCalibrationDate() {
    return nextCalibrationDate;
  }

  public void setNextCalibrationDate(Timestamp nextCalibrationDate) {
    this.nextCalibrationDate = nextCalibrationDate;
  }
}

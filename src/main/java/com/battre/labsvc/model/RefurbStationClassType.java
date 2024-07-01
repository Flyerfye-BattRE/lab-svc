package com.battre.labsvc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "RefurbStationClasses", schema = "LabSvcSchema")
public class RefurbStationClassType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "refurb_station_class_id")
  private Integer refurbStationClassId;

  @Column(name = "station_class", nullable = false, length = 45)
  private String stationClass;

  public RefurbStationClassType() {
    // Default constructor for Spring Data JPA
  }

  public Integer getRefurbStationClassId() {
    return refurbStationClassId;
  }

  public String getStationClass() {
    return stationClass;
  }
}

package com.battre.labsvc.repository;

import com.battre.labsvc.model.LabPlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabPlansRepository extends JpaRepository<LabPlanType, Integer> {
}

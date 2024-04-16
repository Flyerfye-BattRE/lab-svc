package com.battre.labsvc.repository;

import com.battre.labsvc.model.LabPlanType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabPlansRepository extends CrudRepository<LabPlanType, Integer> {
}

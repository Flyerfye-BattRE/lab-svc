package com.battre.labsvc.repository;

import com.battre.labsvc.model.RefurbPlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefurbPlansRepository extends JpaRepository<RefurbPlanType, Integer> {
    // Custom database queries can be added here
}
package com.battre.labsvc.repository;

import com.battre.labsvc.model.RefurbBacklogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefurbBacklogRepository extends JpaRepository<RefurbBacklogType, Integer> {
    // Custom database queries can be added here
}

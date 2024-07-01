package com.battre.labsvc.repository;

import com.battre.labsvc.model.RefurbRecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefurbRecordsRepository extends JpaRepository<RefurbRecordType, Integer> {
  // Custom database queries can be added here
}

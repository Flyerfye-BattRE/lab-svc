package com.battre.labsvc.repository;

import com.battre.labsvc.model.TesterRecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TesterRecordsRepository extends JpaRepository<TesterRecordType, Integer> {
    // Custom database queries can be added here
}

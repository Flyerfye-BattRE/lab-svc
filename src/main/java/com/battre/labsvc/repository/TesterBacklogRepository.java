package com.battre.labsvc.repository;

import com.battre.labsvc.model.TesterBacklogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TesterBacklogRepository extends JpaRepository<TesterBacklogType, Integer> {
}

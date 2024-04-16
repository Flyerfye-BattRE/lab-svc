package com.battre.labsvc.repository;

import com.battre.labsvc.model.TesterBacklogType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TesterBacklogRepository extends CrudRepository<TesterBacklogType, Integer> {
}

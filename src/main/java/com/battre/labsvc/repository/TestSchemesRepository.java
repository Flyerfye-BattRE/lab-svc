package com.battre.labsvc.repository;

import com.battre.labsvc.model.TestSchemeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestSchemesRepository extends JpaRepository<TestSchemeType, Integer> {
  @Query(
      "SELECT t "
          + "FROM TestSchemeType t "
          + "WHERE t.testSchemeId = :testSchemeId "
          + "ORDER BY t.testSchemeId ASC "
          + "LIMIT 1")
  Optional<TestSchemeType> getDataForTestScheme(@Param("testSchemeId") int testSchemeId);
}

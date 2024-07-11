package com.battre.labsvc.repository;

import com.battre.labsvc.model.RefurbSchemeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefurbSchemesRepository extends JpaRepository<RefurbSchemeType, Integer> {
  @Query(
      "SELECT r "
          + "FROM RefurbSchemeType r "
          + "WHERE r.refurbSchemeId = :refurbSchemeId "
          + "ORDER BY r.refurbSchemeId ASC "
          + "LIMIT 1")
  Optional<RefurbSchemeType> getDataForRefurbScheme(@Param("refurbSchemeId") int refurbSchemeId);
}

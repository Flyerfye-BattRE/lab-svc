package com.battre.labsvc.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.battre.labsvc.model.RefurbSchemeType;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class RefurbSchemesRepositoryTest {
  private static final Logger logger =
      Logger.getLogger(RefurbSchemesRepositoryTest.class.getName());

  @Autowired private RefurbSchemesRepository refurbSchemesRepo;

  @Test
  @Sql(scripts = {"/testdb/test-rscr-populateRefurbSchemes.sql"})
  public void testGetDataForRefurbScheme() {
    Optional<RefurbSchemeType> result = refurbSchemesRepo.getDataForRefurbScheme(2);

    // Verify the result
    assertFalse(result.get().isResolder());
    assertTrue(result.get().isRepack());
    assertFalse(result.get().isProcessorSwap());
    assertTrue(result.get().isCapacitorSwap());
  }
}

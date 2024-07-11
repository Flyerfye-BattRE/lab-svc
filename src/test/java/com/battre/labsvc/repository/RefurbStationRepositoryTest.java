package com.battre.labsvc.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.battre.labsvc.model.RefurbStationType;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class RefurbStationRepositoryTest {
  private static final Logger logger =
      Logger.getLogger(RefurbStationRepositoryTest.class.getName());

  @Autowired private RefurbStationRepository refurbStationRepo;

  @Test
  @Sql(scripts = {"/testdb/test-rstr-populateRefurbStation.sql"})
  public void testGetAvailableRefurbStns() {
    List<Object[]> availStns = refurbStationRepo.getAvailableRefurbStns();

    // Verify the result
    // confirm refurb stations returned
    assertNotNull(availStns);
    assertEquals(1, availStns.size());
    // returned fields are: refurbStnId, stationClass
    int refurbStnId = (Integer) availStns.get(0)[0];
    String stationClass = (String) availStns.get(0)[1];
    assertEquals(2, refurbStnId);
    assertEquals("CAPACITOR_SWAP", stationClass);
  }

  @Test
  public void testGetRefurbStationLogs() {
    // TODO: Implement test
  }

  @Test
  @Sql(scripts = {"/testdb/test-rstr-populateRefurbStation.sql"})
  public void testMarkRefurbStnInUse() {
    Timestamp date = Timestamp.valueOf("2024-05-10 12:00:00");
    refurbStationRepo.markRefurbStnInUse(2, 3, date);

    // Verify the result
    RefurbStationType result = refurbStationRepo.findByRefurbStnId(2);
    assertTrue(result.isInUse());
  }

  @Test
  @Sql(scripts = {"/testdb/test-rstr-populateRefurbStation.sql"})
  public void testMarkRefurbStnFree() {
    Timestamp date = Timestamp.valueOf("2024-05-10 12:00:00");
    refurbStationRepo.markRefurbStnFree(1, date);

    // Verify the result
    RefurbStationType result = refurbStationRepo.findByRefurbStnId(1);
    assertFalse(result.isInUse());
  }
}

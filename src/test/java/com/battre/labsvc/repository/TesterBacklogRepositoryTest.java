package com.battre.labsvc.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.battre.labsvc.model.TesterBacklogType;
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
public class TesterBacklogRepositoryTest {
  private static final Logger logger =
      Logger.getLogger(TesterBacklogRepositoryTest.class.getName());

  @Autowired private TesterBacklogRepository testerBacklogRepo;

  @Test
  public void testGetCurrentTesterBacklogForBatteryId() {
    // TODO: Implement test
  }

  @Test
  public void testSetBatteryTesterPriority() {
    // TODO: Implement test
  }

  @Test
  @Sql(scripts = {"/testdb/test-tbr-populateTesterBacklog.sql"})
  public void testGetCurrentTesterBacklog() {
    List<TesterBacklogType> testerBacklog = testerBacklogRepo.getCurrentTesterBacklog();

    // Verify the result
    // confirm pending tester backlog returned
    assertNotNull(testerBacklog);
    assertEquals(1, testerBacklog.size());
    // returned fields are: testerBacklogId, terminalLayoutId, testSchemeId, batteryId
    int testerBacklogId = testerBacklog.get(0).getTesterBacklogId();
    int terminalLayoutId = testerBacklog.get(0).getTerminalLayoutId();
    int testSchemeId = testerBacklog.get(0).getTestSchemeId();
    int batteryId = testerBacklog.get(0).getBatteryId();
    assertEquals(2, testerBacklogId);
    assertEquals(4, terminalLayoutId);
    assertEquals(5, testSchemeId);
    assertEquals(6, batteryId);
  }

  @Test
  public void testGetTesterBacklog() {
    // TODO: Implement test
  }

  @Test
  @Sql(scripts = {"/testdb/test-tbr-populateTesterBacklog.sql"})
  public void testEndTesterBacklogEntry() {
    Timestamp endDate = Timestamp.valueOf("2024-05-10 12:00:00");
    testerBacklogRepo.endTesterBacklogEntry(2, endDate);

    // Verify the result
    TesterBacklogType result = testerBacklogRepo.findByTesterBacklogId(2);
    assertEquals(endDate, result.getTesterBacklogEndDate());
  }
}

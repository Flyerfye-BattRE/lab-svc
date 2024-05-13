package com.battre.labsvc.repository;

import com.battre.labsvc.model.TesterBacklogType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class TesterBacklogRepositoryTest {
    private static final Logger logger = Logger.getLogger(TesterBacklogRepositoryTest.class.getName());

    @Autowired
    private TesterBacklogRepository testerBacklogRepo;

    @Test
    @Sql(scripts = {"/testdb/test-tbr-populateTesterBacklog.sql"})
    public void testGetPendingTesterBacklog() {
        List<Object[]> pendingBacklog = testerBacklogRepo.getPendingTesterBacklog();

        // Verify the result
        // confirm pending tester backlog returned
        assertNotNull(pendingBacklog);
        assertEquals(1, pendingBacklog.size());
        // returned fields are: testerBacklogId, terminalLayoutId, testSchemeId, batteryId
        int testerBacklogId = (Integer) pendingBacklog.get(0)[0];
        int terminalLayoutId = (Integer) pendingBacklog.get(0)[1];
        int testSchemeId = (Integer) pendingBacklog.get(0)[2];
        int batteryId = (Integer) pendingBacklog.get(0)[3];
        assertEquals(2, testerBacklogId);
        assertEquals(4, terminalLayoutId);
        assertEquals(5, testSchemeId);
        assertEquals(6, batteryId);
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

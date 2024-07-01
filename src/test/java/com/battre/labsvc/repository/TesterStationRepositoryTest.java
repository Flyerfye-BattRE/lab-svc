package com.battre.labsvc.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.battre.labsvc.model.TesterStationType;
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
public class TesterStationRepositoryTest {
    private static final Logger logger = Logger.getLogger(TesterStationRepositoryTest.class.getName());

    @Autowired
    private TesterStationRepository testerStationRepo;

    @Test
    @Sql(scripts = {"/testdb/test-tstr-populateTesterStation.sql"})
    public void testGetAvailableTesterStations() {
        List<TesterStationType> availStns = testerStationRepo.getAvailableTesterStations();

        // Verify the result
        // confirm tester stations returned
        assertNotNull(availStns);
        assertEquals(1, availStns.size());
        assertEquals(2, availStns.get(0).getTesterStnId());
        assertEquals(6, availStns.get(0).getTerminalLayoutId());
    }

    @Test
    public void testGetTesterStationLogs() {
        // TODO: Implement test
    }

    @Test
    @Sql(scripts = {"/testdb/test-tstr-populateTesterStation.sql"})
    public void testMarkTesterInUse() {
        Timestamp date = Timestamp.valueOf("2024-05-10 12:00:00");
        testerStationRepo.markTesterInUse(2, 3, date);

        // Verify the result
        TesterStationType result = testerStationRepo.findByTesterStnId(2);
        assertEquals(true, result.isInUse());

    }

    @Test
    @Sql(scripts = {"/testdb/test-tstr-populateTesterStation.sql"})
    public void testMarkTesterFree() {
        Timestamp date = Timestamp.valueOf("2024-05-10 12:00:00");
        testerStationRepo.markTesterFree(1, date);

        // Verify the result
        TesterStationType result = testerStationRepo.findByTesterStnId(1);
        assertEquals(false, result.isInUse());
    }
}

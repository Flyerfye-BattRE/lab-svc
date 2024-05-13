package com.battre.labsvc.repository;

import com.battre.labsvc.model.TesterStationType;
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
public class TesterStationRepositoryTest {
    private static final Logger logger = Logger.getLogger(TesterStationRepositoryTest.class.getName());

    @Autowired
    private TesterStationRepository testerStationRepo;

    @Test
    @Sql(scripts = {"/testdb/test-tstr-populateTesterStation.sql"})
    public void testGetAvailableTesterStations() {
        List<Object[]> availStns = testerStationRepo.getAvailableTesterStations();

        // Verify the result
        // confirm tester stations returned
        assertNotNull(availStns);
        assertEquals(1, availStns.size());
        // returned fields are: testerStnId, terminalLayoutId
        int testerStnId = (Integer) availStns.get(0)[0];
        int terminalLayoutId = (Integer) availStns.get(0)[1];
        assertEquals(2, testerStnId);
        assertEquals(6, terminalLayoutId);
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

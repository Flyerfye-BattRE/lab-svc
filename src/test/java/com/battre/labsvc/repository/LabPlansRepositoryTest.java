package com.battre.labsvc.repository;

import com.battre.labsvc.model.LabPlanType;
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
public class LabPlansRepositoryTest {
    private static final Logger logger = Logger.getLogger(LabPlansRepositoryTest.class.getName());

    @Autowired
    private LabPlansRepository labPlansRepo;

    @Test
    @Sql(scripts = {"/testdb/test-lpr-populateLabPlans.sql"})
    public void testGetLabPlansForBatteryId() {
        List<Integer> labPlanIds = labPlansRepo.getLabPlanIdsForBatteryId(2);

        // Verify the result
        assertNotNull(labPlanIds);
        assertEquals(1, labPlanIds.size());
        assertEquals(2, labPlanIds.get(0));
    }

    @Test
    @Sql(scripts = {"/testdb/test-lpr-populateLabPlans.sql"})
    public void testSetTesterRecordForLabPlan() {
        labPlansRepo.setTesterRecordForLabPlan(1, 7);

        // Verify the result
        LabPlanType result = labPlansRepo.findByLabPlanId(1);
        assertEquals(7, result.getTesterRecordId());
    }

    @Test
    @Sql(scripts = {"/testdb/test-lpr-populateLabPlans.sql"})
    public void testSetRefurbPlanForLabPlan() {
        labPlansRepo.setRefurbPlanForLabPlan(2, 8);

        // Verify the result
        LabPlanType result = labPlansRepo.findByLabPlanId(2);
        assertEquals(8, result.getRefurbPlanId());
    }

    @Test
    @Sql(scripts = {"/testdb/test-lpr-populateLabPlans.sql"})
    public void testEndLabPlan() {
        Timestamp endDate = Timestamp.valueOf("2024-05-10 12:00:00");
        labPlansRepo.endLabPlan(1, endDate);

        // Verify the result
        LabPlanType result = labPlansRepo.findByLabPlanId(1);
        assertEquals(endDate, result.getLabPlanEndDate());
    }

    @Test
    @Sql(scripts = {"/testdb/test-lpr-populateLabPlans.sql"})
    public void testEndLabPlanEntryForRefurbPlan() {
        Timestamp endDate = Timestamp.valueOf("2024-05-10 12:00:00");
        labPlansRepo.endLabPlanEntryForRefurbPlan(4, endDate);

        // Verify the result
        LabPlanType result = labPlansRepo.findByRefurbPlanId(4);
        assertEquals(endDate, result.getLabPlanEndDate());
    }
}

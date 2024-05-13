package com.battre.labsvc.repository;

import com.battre.labsvc.model.RefurbPlanType;
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
public class RefurbPlanRepositoryTest {
    private static final Logger logger = Logger.getLogger(RefurbPlanRepositoryTest.class.getName());

    @Autowired
    private RefurbPlanRepository refurbPlanRepo;

    @Test
    @Sql(scripts = {"/testdb/test-rpr-populateRefurbPlan.sql"})
    public void testMarkRefurbPlanBusy() {
        refurbPlanRepo.markRefurbPlanBusy(2);

        // Verify the result
        RefurbPlanType result = refurbPlanRepo.findByRefurbPlanId(2);
        assertEquals(false, result.isAvailable());

    }

    @Test
    @Sql(scripts = {"/testdb/test-rpr-populateRefurbPlan.sql"})
    public void testMarkRefurbPlanAvail() {
        refurbPlanRepo.markRefurbPlanAvail(1);

        // Verify the result
        RefurbPlanType result = refurbPlanRepo.findByRefurbPlanId(1);
        assertEquals(true, result.isAvailable());
    }

    @Test
    @Sql(scripts = {"/testdb/test-rpr-populateRefurbPlan.sql"})
    public void testCheckRefurbPlanCompleted() {
        // Verify the result
        // confirm one refurb plan completed, confirm another not
        assertEquals(false, refurbPlanRepo.checkRefurbPlanCompleted(1));
        assertEquals(true, refurbPlanRepo.checkRefurbPlanCompleted(2));
    }

    @Test
    @Sql(scripts = {"/testdb/test-rpr-populateRefurbPlan.sql"})
    public void testEndRefurbPlanEntry() {
        Timestamp endDate = Timestamp.valueOf("2024-05-10 12:00:00");
        refurbPlanRepo.endRefurbPlanEntry(1, endDate);

        // Verify the result
        // confirm refurb end date set
        RefurbPlanType result = refurbPlanRepo.findByRefurbPlanId(1);
        assertEquals(endDate, result.getRefurbPlanEndDate());

    }

    @Test
    @Sql(scripts = {"/testdb/test-rpr-populateRefurbPlan.sql"})
    public void testSetResolderRecord() {
        refurbPlanRepo.setResolderRecord(2, 5);

        // Verify the result
        RefurbPlanType result = refurbPlanRepo.findByRefurbPlanId(2);
        assertEquals(5, result.getResolderRecordId());
    }

    @Test
    @Sql(scripts = {"/testdb/test-rpr-populateRefurbPlan.sql"})
    public void testSetRepackRecord() {
        refurbPlanRepo.setRepackRecord(2, 6);

        // Verify the result
        RefurbPlanType result = refurbPlanRepo.findByRefurbPlanId(2);
        assertEquals(6, result.getRepackRecordId());
    }

    @Test
    @Sql(scripts = {"/testdb/test-rpr-populateRefurbPlan.sql"})
    public void testSetProcessorSwapRecord() {
        refurbPlanRepo.setProcessorSwapRecord(2, 7);

        // Verify the result
        RefurbPlanType result = refurbPlanRepo.findByRefurbPlanId(2);
        assertEquals(7, result.getProcessorSwapRecordId());
    }

    @Test
    @Sql(scripts = {"/testdb/test-rpr-populateRefurbPlan.sql"})
    public void testSetCapacitorSwapRecord() {
        refurbPlanRepo.setCapacitorSwapRecord(2, 8);

        // Verify the result
        RefurbPlanType result = refurbPlanRepo.findByRefurbPlanId(2);
        assertEquals(8, result.getCapacitorSwapRecordId());
    }

    @Test
    @Sql(scripts = {"/testdb/test-rpr-populateRefurbPlan.sql"})
    public void testGetPendingRefurbPlans() {
        List<Object[]> pendingPlans = refurbPlanRepo.getPendingRefurbPlans();

        // Verify the result
        // confirm pending refurb plans returned
        assertNotNull(pendingPlans);
        assertEquals(1, pendingPlans.size());
        // refurbPlanId is first field returned
        int refurbPlanId = (Integer) pendingPlans.get(0)[0];
        assertEquals(1, refurbPlanId);
    }
}

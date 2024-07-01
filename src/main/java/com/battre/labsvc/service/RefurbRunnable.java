package com.battre.labsvc.service;

import com.battre.labsvc.enums.LabResult;
import com.battre.labsvc.enums.RefurbStationClass;
import com.battre.labsvc.model.RefurbResultRecord;
import com.battre.labsvc.repository.RefurbStationRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class RefurbRunnable implements Runnable {
    private static final Logger logger = Logger.getLogger(RefurbRunnable.class.getName());

    private final RefurbStationRepository refurbStationRepo;
    private final BlockingQueue<RefurbResultRecord> resultQueue;
    private final int refurbPlanId;
    private final int refurbStnId;
    private final RefurbStationClass refurbStnClass;
    private final int batteryId;
    private final Random random;

    RefurbRunnable(RefurbStationRepository refurbStationRepo,
                   BlockingQueue<RefurbResultRecord> resultQueue,
                   int refurbPlanId,
                   int refurbStnId,
                   RefurbStationClass refurbStnClass,
                   int batteryId) {
        this.refurbStationRepo = refurbStationRepo;
        this.resultQueue = resultQueue;
        this.refurbPlanId = refurbPlanId;
        this.refurbStnId = refurbStnId;
        this.refurbStnClass = refurbStnClass;
        this.batteryId = batteryId;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            logger.info("Refurb stn [" + refurbStnId + "]: refurbishing battery " + batteryId);
            RefurbResultRecord result = performRefurb();
            // Puts a test result in the queue
            resultQueue.put(result);
            // Call the TestStationsRepo to release the current tester
            refurbStationRepo.markRefurbStnFree(refurbStnId, Timestamp.from(Instant.now()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private RefurbResultRecord performRefurb() {
        int resultId = generateResultId();
        return new RefurbResultRecord(refurbPlanId,
                refurbStnId,
                refurbStnClass,
                batteryId,
                resultId,
                Timestamp.from(Instant.now()));
    }

    // Pass         (resultId=1) has 90% chance
    // Fail-Retry  (resultId=2) has 5% chance
    // Fail-Reject  (resultId=3) has 5% chance
    private int generateResultId() {
        double resultChance = random.nextDouble();
        logger.info("Refurb Stn [" + refurbStnId + "]: chance " + resultChance);

        if (resultChance < 0.90) {
            return LabResult.PASS.getStatusCode();
        } else if (resultChance < 0.95) {
            return LabResult.FAIL_RETRY.getStatusCode();
        } else {
            return LabResult.FAIL_REJECT.getStatusCode();
        }
    }
}

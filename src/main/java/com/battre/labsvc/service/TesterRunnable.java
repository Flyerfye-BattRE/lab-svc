package com.battre.labsvc.service;

import com.battre.labsvc.enums.LabResult;
import com.battre.labsvc.repository.TesterStationRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class TesterRunnable implements Runnable {
    public static final int NUM_REFURB_SCHEMES = 10;
    private static final Logger logger = Logger.getLogger(TesterRunnable.class.getName());

    private final TesterStationRepository testerStationsRepo;
    private final BlockingQueue<TesterResultRecord> resultQueue;
    private final int testerId;
    private final int testSchemeId;
    private final int batteryId;
    private final int terminalLayoutId;
    private final Random random;

    TesterRunnable(TesterStationRepository testerStationsRepo,
                   BlockingQueue<TesterResultRecord> resultQueue,
                   int testerId,
                   int testSchemeId,
                   int batteryId,
                   int terminalLayoutId) {
        this.testerStationsRepo = testerStationsRepo;
        this.resultQueue = resultQueue;
        this.testerId = testerId;
        this.testSchemeId = testSchemeId;
        this.batteryId = batteryId;
        this.terminalLayoutId = terminalLayoutId;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            logger.info("Tester [" + testerId + "]: testing battery " + batteryId);
            TesterResultRecord result = performTest();
            // Puts a test result in the queue
            resultQueue.put(result);
            // Call the TestStationsRepo to release the current tester
            testerStationsRepo.markTesterFree(testerId, Timestamp.from(Instant.now()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private TesterResultRecord performTest() {
        int refurbSchemeId = random.nextInt(NUM_REFURB_SCHEMES) + 1;
        int resultId = generateResultId();
        return new TesterResultRecord(testerId, batteryId, testSchemeId, refurbSchemeId, terminalLayoutId, resultId, Timestamp.from(Instant.now()));
    }

    // Pass         (resultId=1) has 85% chance
    // Fail-Retry   (resultId=2) has 10% chance
    // Fail-Reject  (resultId=3) has 5% chance
    private int generateResultId() {
        double resultChance = random.nextDouble();
        logger.info("Tester [" + testerId + "]: chance " + resultChance);

        if (resultChance < 0.85) {
            return LabResult.PASS.getStatusCode();
        } else if (resultChance < 0.95) {
            return LabResult.FAIL_RETRY.getStatusCode();
        } else {
            return LabResult.FAIL_REJECT.getStatusCode();
        }
    }
}

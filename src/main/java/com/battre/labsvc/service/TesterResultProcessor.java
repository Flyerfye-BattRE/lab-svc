package com.battre.labsvc.service;

import com.battre.labsvc.enums.TestResult;
import com.battre.labsvc.model.RefurbBacklogType;
import com.battre.labsvc.model.RefurbPlanType;
import com.battre.labsvc.model.TestSchemeType;
import com.battre.labsvc.model.TesterBacklogType;
import com.battre.labsvc.model.TesterRecordType;
import com.battre.labsvc.repository.LabPlansRepository;
import com.battre.labsvc.repository.RefurbBacklogRepository;
import com.battre.labsvc.repository.RefurbPlansRepository;
import com.battre.labsvc.repository.TestSchemesRepository;
import com.battre.labsvc.repository.TesterBacklogRepository;
import com.battre.labsvc.repository.TesterRecordsRepository;
import com.battre.stubs.services.BatteryIdStatus;
import com.battre.stubs.services.BatteryStatus;
import com.battre.stubs.services.OpsSvcGrpc;
import com.battre.stubs.services.StorageSvcGrpc;
import com.battre.stubs.services.UpdateBatteryStatusRequest;
import com.battre.stubs.services.UpdateBatteryStatusResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class TesterResultProcessor implements Runnable {
    private static final Logger logger = Logger.getLogger(TesterResultProcessor.class.getName());
    private final LabPlansRepository labPlansRepo;
    private final RefurbBacklogRepository refurbBacklogRepo;
    private final RefurbPlansRepository refurbPlansRepo;
    private final TesterBacklogRepository testerBacklogRepo;
    private final TesterRecordsRepository testerRecordsRepo;
    private final TestSchemesRepository testSchemesRepo;
    private final BlockingQueue<TesterResultRecord> resultQueue;
    // Check every 5 seconds
    private final long checkInterval = 5000;
    private final Object lock = new Object();
    @GrpcClient("opsSvc")
    private OpsSvcGrpc.OpsSvcStub opsSvcClient;
    @GrpcClient("storageSvc")
    private StorageSvcGrpc.StorageSvcStub storageSvcClient;
    private volatile boolean active = true;

    @Autowired
    public TesterResultProcessor(
//    public TesterResultProcessor(OpsSvcGrpc.OpsSvcStub opsSvcClient,
//                                 StorageSvcGrpc.StorageSvcStub storageSvcClient,
            LabPlansRepository labPlansRepo,
            RefurbBacklogRepository refurbBacklogRepo,
            RefurbPlansRepository refurbPlansRepo,
            TesterBacklogRepository testerBacklogRepo,
            TesterRecordsRepository testerRecordsRepo,
            TestSchemesRepository testSchemesRepo,
            BlockingQueue<TesterResultRecord> resultQueue) {
//        this.opsSvcClient = opsSvcClient;
//        this.storageSvcClient = storageSvcClient;
        this.labPlansRepo = labPlansRepo;
        this.refurbBacklogRepo = refurbBacklogRepo;
        this.refurbPlansRepo = refurbPlansRepo;
        this.testerRecordsRepo = testerRecordsRepo;
        this.testerBacklogRepo = testerBacklogRepo;
        this.testSchemesRepo = testSchemesRepo;
        this.resultQueue = resultQueue;

    }

    @Override
    public void run() {
        try {
            while (active) {
                synchronized (lock) {
                    logger.info("Running run");
                    processResults();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Backgrounder interrupted");
        } catch (Exception e) {
            System.err.println("Error in backgrounder operation: " + e.getMessage());
        }
    }

    public void triggerResultProcessing() {
        logger.info("Triggering result processing");
        synchronized (lock) {
            lock.notify();  // Wake the waiting thread
        }
    }

    public void stop() {
        logger.info("Stopping thread");
        active = false;
        triggerResultProcessing();  // Ensure the loop exits if it is waiting
    }

    private void processResults() throws InterruptedException {
        logger.info("Running processResults");
        //      For each result
        //          Pull item from Q
        // Block the thread until resultQueue contains a result
        TesterResultRecord trr = resultQueue.take();

        // Create and save TesterRecord
        TesterRecordType trt = new TesterRecordType(
                trr.testerStnId(),
                trr.batteryId(),
                trr.testSchemeId(),
                trr.resultTypeId(),
                trr.testDate());
        TesterRecordType savedRecord = testerRecordsRepo.save(trt);

        // Update LabPlan with tester record id and check exactly 1 open plan
        List<Integer> labPlans = labPlansRepo.getLabPlansForBatteryId(trr.batteryId());
        if (labPlans.size() != 1) {
            logger.severe("# of lab plans for battery [" + labPlans.size() + "] is not exactly 1: " + labPlans);
        }
        // Update only the latest lab plan if there is more than one
        labPlansRepo.setTesterRecordForLabPlan(labPlans.get(0), savedRecord.getTesterRecordId());

        if (trr.resultTypeId() == TestResult.PASS.getStatusCode()) {
            // Pass
            logger.info("Battery [" + trr.batteryId() + "] PASSES: Refurb Scheme is " + trr.testSchemeId());
            // Create refurb plan
            Optional<TestSchemeType> testSchemeDataResponse = testSchemesRepo.getDataForTestScheme(trr.testSchemeId());

            if (testSchemeDataResponse.isPresent()) {
                TestSchemeType testSchemeData = testSchemeDataResponse.get();

                RefurbPlanType rpt = new RefurbPlanType(
                        trr.batteryId(),
                        testSchemeData.isCheckerboard(),
                        testSchemeData.isNullLine(),
                        testSchemeData.isVaporSim(),
                        testSchemeData.isBlackout(),
                        testSchemeData.isOvenScreen()
                );
                RefurbPlanType savedRefurbPlan = refurbPlansRepo.save(rpt);

                // Update lab plan with refurb plan id
                labPlansRepo.setRefurbPlanForLabPlan(labPlans.get(0), savedRefurbPlan.getRefurbPlanId());

                // Create refurb backlog entry
                RefurbBacklogType rbt = new RefurbBacklogType(savedRefurbPlan.getRefurbPlanId(), trr.batteryId());
                refurbBacklogRepo.save(rbt);

                // Call OpsSvc to update battery status to refurb
                updateOpsSvcBatteryStatus(trr.batteryId(), BatteryStatus.REFURB);
            } else {
                logger.severe("No test scheme data [" + trr.testSchemeId() + "] for battery: " + trr.batteryId());
            }
        } else if (trr.resultTypeId() == TestResult.FAIL_RETRY.getStatusCode()) {
            // Fail-Retry
            logger.info("Battery [" + trr.batteryId() + "] FAILS > Retry");
            // Add to TesterBacklog again (new entry)
            TesterBacklogType testerBacklogEntry = new TesterBacklogType(
                    trr.batteryId(),
                    trr.terminalLayoutId()
            );
            testerBacklogRepo.save(testerBacklogEntry);
        } else {
            // Fail-Reject
            logger.info("Battery [" + trr.batteryId() + "] FAILS > Rejected");
            // Update lab plan with end date
            labPlansRepo.endLabPlan(labPlans.get(0), Timestamp.from(Instant.now()));

            // Call OpsSvc to update battery status to rejected
            updateOpsSvcBatteryStatus(trr.batteryId(), BatteryStatus.REJECTED);

            // TODO: Call StorageSvc to remove battery/update avail capacity
        }
    }

    private boolean updateOpsSvcBatteryStatus(int batteryId, BatteryStatus status) {
        BatteryIdStatus batteryIdStatus = BatteryIdStatus.newBuilder()
                .setBatteryId(batteryId)
                .setBatteryStatus(status)
                .build();
        UpdateBatteryStatusRequest request =
                UpdateBatteryStatusRequest.newBuilder().setBatteries(batteryIdStatus).build();

        CompletableFuture<UpdateBatteryStatusResponse> responseFuture = new CompletableFuture<>();
        StreamObserver<UpdateBatteryStatusResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(UpdateBatteryStatusResponse response) {
                responseFuture.complete(response);
            }

            @Override
            public void onError(Throwable t) {
                // Handle any errors
                logger.severe("updateOpsSvcBatteryStatus() errored: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("updateOpsSvcBatteryStatus() completed");
            }
        };

        opsSvcClient.updateBatteryStatus(request, responseObserver);

        boolean result = false;
        // Wait for the response or 1 sec handle timeout
        try {
            // Blocks until the response is available
            result = responseFuture.get(5, TimeUnit.SECONDS).getSuccess();
            logger.info("updateOpsSvcBatteryStatus() responseFuture response: " + result);
        } catch (Exception e) {
            logger.severe("updateOpsSvcBatteryStatus() responseFuture error: " + e.getMessage());
        }

        return result;
    }
}
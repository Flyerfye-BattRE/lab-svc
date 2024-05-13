package com.battre.labsvc.service;

import com.battre.labsvc.enums.LabResult;
import com.battre.labsvc.model.RefurbPlanType;
import com.battre.labsvc.model.RefurbSchemeType;
import com.battre.labsvc.model.TesterBacklogType;
import com.battre.labsvc.model.TesterRecordType;
import com.battre.labsvc.repository.LabPlansRepository;
import com.battre.labsvc.repository.RefurbPlanRepository;
import com.battre.labsvc.repository.RefurbSchemesRepository;
import com.battre.labsvc.repository.TesterBacklogRepository;
import com.battre.labsvc.repository.TesterRecordsRepository;
import com.battre.stubs.services.BatteryIdStatus;
import com.battre.stubs.services.BatteryStatus;
import com.battre.stubs.services.OpsSvcGrpc;
import com.battre.stubs.services.RemoveBatteryRequest;
import com.battre.stubs.services.RemoveBatteryResponse;
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
    private final RefurbPlanRepository refurbPlanRepo;
    private final TesterBacklogRepository testerBacklogRepo;
    private final TesterRecordsRepository testerRecordsRepo;
    private final RefurbSchemesRepository refurbSchemesRepo;
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
            LabPlansRepository labPlansRepo,
            RefurbPlanRepository refurbPlanRepo,
            TesterBacklogRepository testerBacklogRepo,
            TesterRecordsRepository testerRecordsRepo,
            RefurbSchemesRepository refurbSchemesRepo,
            BlockingQueue<TesterResultRecord> resultQueue) {
        this.labPlansRepo = labPlansRepo;
        this.refurbPlanRepo = refurbPlanRepo;
        this.testerRecordsRepo = testerRecordsRepo;
        this.testerBacklogRepo = testerBacklogRepo;
        this.refurbSchemesRepo = refurbSchemesRepo;
        this.resultQueue = resultQueue;

    }

    @Override
    public void run() {
        try {
            while (active) {
                synchronized (lock) {
                    processTesterResults();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Tester results processor interrupted");
        } catch (Exception e) {
            System.err.println("Error in tester results processor  operation: " + e.getMessage());
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

    private void processTesterResults() throws InterruptedException {
        logger.info("Running processTesterResults");
        // Block the thread until resultQueue contains a result
        //      For each result
        //          Pull item from Q
        TesterResultRecord trr = resultQueue.take();

        // Create and save TesterRecord
        TesterRecordType trt = new TesterRecordType(
                trr.testerStnId(),
                trr.batteryId(),
                trr.testSchemeId(),
                trr.refurbSchemeId(),
                trr.resultTypeId(),
                trr.testDate());
        TesterRecordType savedRecord = testerRecordsRepo.save(trt);

        // Update LabPlan with tester record id and check exactly 1 open plan
        List<Integer> labPlans = labPlansRepo.getLabPlanIdsForBatteryId(trr.batteryId());
        if (labPlans.size() != 1) {
            logger.severe("# of lab plans [" + labPlans.size() + "] for battery [" + trr.batteryId() + "] is not exactly 1: " + labPlans);
        }
        // Update only the latest lab plan if there is more than one
        labPlansRepo.setTesterRecordForLabPlan(labPlans.get(0), savedRecord.getTesterRecordId());

        if (trr.resultTypeId() == LabResult.PASS.getStatusCode()) {
            // Pass
            logger.info("Battery [" + trr.batteryId() + "] PASSES: Refurb Scheme is " + trr.refurbSchemeId());
            // Create refurb plan
            Optional<RefurbSchemeType> refurbSchemeDataResponse = refurbSchemesRepo.getDataForRefurbScheme(trr.refurbSchemeId());

            if (refurbSchemeDataResponse.isPresent()) {
                RefurbSchemeType refurbSchemeData = refurbSchemeDataResponse.get();

                RefurbPlanType rpt = new RefurbPlanType(
                        trr.batteryId(),
                        refurbSchemeData.isResolder(),
                        refurbSchemeData.isRepack(),
                        refurbSchemeData.isProcessorSwap(),
                        refurbSchemeData.isCapacitorSwap()
                );
                RefurbPlanType savedRefurbPlan = refurbPlanRepo.save(rpt);

                // Update lab plan with refurb plan id
                labPlansRepo.setRefurbPlanForLabPlan(labPlans.get(0), savedRefurbPlan.getRefurbPlanId());

                // Call OpsSvc to update battery status to refurb
                updateOpsSvcBatteryStatus(trr.batteryId(), BatteryStatus.REFURB);
            } else {
                logger.severe("No refurb scheme data [" + trr.refurbSchemeId() + "] for battery: " + trr.batteryId());
            }
        } else if (trr.resultTypeId() == LabResult.FAIL_RETRY.getStatusCode()) {
            // Fail-Retry
            logger.info("Battery [" + trr.batteryId() + "] FAILS > Retry");
            // Add to TesterBacklog again (new entry)
            TesterBacklogType testerBacklogEntry = new TesterBacklogType(
                    trr.batteryId(),
                    trr.testSchemeId(),
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

            // Call StorageSvc to remove battery/update avail capacity
            updateStorageSvcRemoveBattery(trr.batteryId());
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

    private boolean updateStorageSvcRemoveBattery(int batteryId) {
        RemoveBatteryRequest request =
                RemoveBatteryRequest.newBuilder().setBatteryId(batteryId).build();

        CompletableFuture<RemoveBatteryResponse> responseFuture = new CompletableFuture<>();
        StreamObserver<RemoveBatteryResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(RemoveBatteryResponse response) {
                responseFuture.complete(response);
            }

            @Override
            public void onError(Throwable t) {
                // Handle any errors
                logger.severe("updateStorageSvcRemoveBattery() errored: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("updateStorageSvcRemoveBattery() completed");
            }
        };

        storageSvcClient.removeBattery(request, responseObserver);

        boolean result = false;
        // Wait for the response or 1 sec handle timeout
        try {
            // Blocks until the response is available
            result = responseFuture.get(5, TimeUnit.SECONDS).getSuccess();
            logger.info("updateStorageSvcRemoveBattery() responseFuture response: " + result);
        } catch (Exception e) {
            logger.severe("updateStorageSvcRemoveBattery() responseFuture error: " + e.getMessage());
        }

        return result;
    }
}
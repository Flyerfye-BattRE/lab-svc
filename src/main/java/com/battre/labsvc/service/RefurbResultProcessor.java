package com.battre.labsvc.service;

import com.battre.grpcifc.GrpcMethodInvoker;
import com.battre.labsvc.enums.LabPlanStatusEnum;
import com.battre.labsvc.enums.LabResult;
import com.battre.labsvc.enums.RefurbStationClass;
import com.battre.labsvc.model.RefurbRecordType;
import com.battre.labsvc.model.RefurbResultRecord;
import com.battre.labsvc.repository.LabPlansRepository;
import com.battre.labsvc.repository.RefurbPlanRepository;
import com.battre.labsvc.repository.RefurbRecordsRepository;
import com.battre.labsvc.repository.RefurbStationRepository;
import com.battre.stubs.services.BatteryIdStatus;
import com.battre.stubs.services.BatteryStatus;
import com.battre.stubs.services.RemoveStorageBatteryRequest;
import com.battre.stubs.services.RemoveStorageBatteryResponse;
import com.battre.stubs.services.UpdateBatteryStatusRequest;
import com.battre.stubs.services.UpdateBatteryStatusResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class RefurbResultProcessor implements Runnable {
    private static final Logger logger = Logger.getLogger(RefurbResultProcessor.class.getName());
    private final LabPlansRepository labPlansRepo;
    private final RefurbPlanRepository refurbPlanRepo;
    private final RefurbStationRepository refurbStationsRepo;
    private final RefurbRecordsRepository refurbRecordsRepo;
    private final BlockingQueue<RefurbResultRecord> resultQueue;
    // Check every 5 seconds
    private final long checkInterval = 5000;
    private final Object lock = new Object();
    private final GrpcMethodInvoker grpcMethodInvoker;
    private volatile boolean active = true;

    @Autowired
    public RefurbResultProcessor(
            LabPlansRepository labPlansRepo,
            RefurbPlanRepository refurbPlanRepo,
            RefurbStationRepository refurbStationsRepo,
            RefurbRecordsRepository refurbRecordsRepo,
            GrpcMethodInvoker grpcMethodInvoker,
            BlockingQueue<RefurbResultRecord> resultQueue) {
        this.labPlansRepo = labPlansRepo;
        this.refurbPlanRepo = refurbPlanRepo;
        this.refurbStationsRepo = refurbStationsRepo;
        this.refurbRecordsRepo = refurbRecordsRepo;
        this.resultQueue = resultQueue;
        this.grpcMethodInvoker = grpcMethodInvoker;
    }

    @Override
    public void run() {
        try {
            while (active) {
                synchronized (lock) {
                    processRefurbResults();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Refurb results processor interrupted");
        } catch (Exception e) {
            System.err.println("Error in refurb results processor operation: " + e.getMessage());
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

    void processRefurbResults() throws InterruptedException {
        logger.info("Running processRefurbResults");
        // Block the thread until resultQueue contains a result
        //      For each result
        //          Pull item from Q
        RefurbResultRecord rrr = resultQueue.take();
        String refurbStnClass = rrr.refurbStnClass().toString();

        // when no refurb is necessary, no record needs to be created/no associated id
        int refurbRecordId = -1;
        if (rrr.refurbStnId() > 0) {
            // Create and save TesterRecord
            RefurbRecordType rrt = new RefurbRecordType(
                    rrr.refurbPlanId(),
                    rrr.refurbStnId(),
                    rrr.refurbStnClass().toString(),
                    rrr.batteryId(),
                    rrr.resultTypeId(),
                    rrr.testDate());
            RefurbRecordType savedRecord = refurbRecordsRepo.save(rrt);
            refurbRecordId = savedRecord.getRefurbRecordId();
        }

        // Update LabPlan with tester record id and check exactly 1 open plan
        List<Integer> labPlans = labPlansRepo.getLabPlanIdsForBatteryId(rrr.batteryId());
        if (labPlans.size() != 1) {
            logger.severe("# of lab plans [" + labPlans.size() + "] for battery [" + rrr.batteryId() + "] is not exactly 1: " + labPlans);
            logger.severe("Refurb Info: " + rrr);
        }

        // if the result was not a retry, record the result in the battery's refurb plan
        if (rrr.resultTypeId() != LabResult.FAIL_RETRY.getStatusCode()) {
            // link the refurb record to the appropriate refurb class in the RefurbPlans table
            switch (refurbStnClass) {
                case "NO_REFURB":
                    //No refurb was necessary
                    break;
                case "RESOLDER":
                    refurbPlanRepo.setResolderRecord(rrr.refurbPlanId(), refurbRecordId);
                    break;
                case "REPACK":
                    refurbPlanRepo.setRepackRecord(rrr.refurbPlanId(), refurbRecordId);
                    break;
                case "PROCESSOR_SWAP":
                    refurbPlanRepo.setProcessorSwapRecord(rrr.refurbPlanId(), refurbRecordId);
                    break;
                case "CAPACITOR_SWAP":
                    refurbPlanRepo.setCapacitorSwapRecord(rrr.refurbPlanId(), refurbRecordId);
                    break;
                default:
                    logger.severe("Unable to set refurb record id, unrecognized refurb class: " + refurbStnClass);
                    break;
            }
        }

        // if no refurb was necessary, no station needs to be updated
        if (refurbStnClass != RefurbStationClass.NO_REFURB.toString()) {
            refurbStationsRepo.markRefurbStnFree(rrr.refurbStnId(), Timestamp.from(Instant.now()));
        }

        if (rrr.resultTypeId() == LabResult.PASS.getStatusCode()) {
            // Pass
            logger.info("Battery [" + rrr.batteryId() + "] PASSES: Refurb type is " + rrr.refurbStnClass());

            // Allows battery to continue to next step if there are additional refurb classes to complete
            refurbPlanRepo.markRefurbPlanAvail(rrr.refurbPlanId());

            // Move to storage/update Ops Svc if there are no more refurb steps to complete
            if (refurbPlanRepo.checkRefurbPlanCompleted(rrr.refurbPlanId())) {
                logger.info("Refurb Plan [" + rrr.refurbPlanId() + "] COMPLETED for Battery [" + rrr.batteryId() + "]");

                labPlansRepo.endLabPlanEntryForRefurbPlan(rrr.refurbPlanId(), Timestamp.from(Instant.now()));
                labPlansRepo.setPlanStatusesForPlanId(labPlans.get(0), LabPlanStatusEnum.PASS.toString());
                refurbPlanRepo.endRefurbPlanEntry(rrr.refurbPlanId(), Timestamp.from(Instant.now()));
                // Call OpsSvc to update battery status to Storage once refurb is done
                updateOpsSvcBatteryStatus(rrr.batteryId(), BatteryStatus.STORAGE);
            } else {
                labPlansRepo.setPlanStatusesForPlanId(labPlans.get(0), LabPlanStatusEnum.REFURB_BACKLOG_CONT.toString());
            }
        } else if (rrr.resultTypeId() == LabResult.FAIL_RETRY.getStatusCode()) {
            // Fail-Retry
            logger.info("Battery [" + rrr.batteryId() + "] FAILS > Retry refurb");

            refurbPlanRepo.markRefurbPlanAvail(rrr.refurbPlanId());
            labPlansRepo.setPlanStatusesForPlanId(labPlans.get(0), LabPlanStatusEnum.REFURB_BACKLOG_RETRY.toString());
        } else if (rrr.resultTypeId() == LabResult.FAIL_REJECT.getStatusCode()) {
            // Fail-Reject
            logger.info("Battery [" + rrr.batteryId() + "] FAILS > Rejected");
            // Update lab plan with end date
            labPlansRepo.endLabPlan(labPlans.get(0), Timestamp.from(Instant.now()));
            labPlansRepo.setPlanStatusesForPlanId(labPlans.get(0), LabPlanStatusEnum.REFURB_REJECTED.toString());
            refurbPlanRepo.endRefurbPlanEntry(rrr.refurbPlanId(), Timestamp.from(Instant.now()));

            // Call OpsSvc to update battery status to rejected
            updateOpsSvcBatteryStatus(rrr.batteryId(), BatteryStatus.REJECTED);

            // Call StorageSvc to remove battery/update avail capacity
            updateStorageSvcRemoveBattery(rrr.batteryId());
        } else {
            logger.info("Unrecognized status code for Battery [" + rrr.batteryId() + "]: " + rrr.resultTypeId());
        }
    }

    private boolean updateOpsSvcBatteryStatus(int batteryId, BatteryStatus status) {
        BatteryIdStatus batteryIdStatus = BatteryIdStatus.newBuilder()
                .setBatteryId(batteryId)
                .setBatteryStatus(status)
                .build();
        UpdateBatteryStatusRequest request =
                UpdateBatteryStatusRequest.newBuilder().setBattery(batteryIdStatus).build();

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
                responseFuture.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                logger.info("updateOpsSvcBatteryStatus() completed");
            }
        };

        grpcMethodInvoker.callMethod(
                "opssvc",
                "updateBatteryStatus",
                request,
                responseObserver
        );

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
        RemoveStorageBatteryRequest request =
                RemoveStorageBatteryRequest.newBuilder().setBatteryId(batteryId).build();

        CompletableFuture<RemoveStorageBatteryResponse> responseFuture = new CompletableFuture<>();
        StreamObserver<RemoveStorageBatteryResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(RemoveStorageBatteryResponse response) {
                responseFuture.complete(response);
            }

            @Override
            public void onError(Throwable t) {
                // Handle any errors
                logger.severe("updateStorageSvcRemoveBattery() errored: " + t.getMessage());
                responseFuture.completeExceptionally(t);
            }


            @Override
            public void onCompleted() {
                logger.info("updateStorageSvcRemoveBattery() completed");
            }
        };

        grpcMethodInvoker.callMethod(
                "storagesvc",
                "removeStorageBattery",
                request,
                responseObserver
        );

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
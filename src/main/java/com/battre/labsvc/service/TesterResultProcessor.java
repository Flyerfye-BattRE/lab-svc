package com.battre.labsvc.service;

import com.battre.grpcifc.GrpcMethodInvoker;
import com.battre.labsvc.enums.LabPlanStatusEnum;
import com.battre.labsvc.enums.LabResult;
import com.battre.labsvc.model.RefurbPlanType;
import com.battre.labsvc.model.RefurbSchemeType;
import com.battre.labsvc.model.TesterBacklogType;
import com.battre.labsvc.model.TesterRecordType;
import com.battre.labsvc.model.TesterResultRecord;
import com.battre.labsvc.repository.LabPlansRepository;
import com.battre.labsvc.repository.RefurbPlanRepository;
import com.battre.labsvc.repository.RefurbSchemesRepository;
import com.battre.labsvc.repository.TesterBacklogRepository;
import com.battre.labsvc.repository.TesterRecordsRepository;
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
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
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
    private final GrpcMethodInvoker grpcMethodInvoker;
    private volatile boolean active = true;

    @Autowired
    public TesterResultProcessor(
            LabPlansRepository labPlansRepo,
            RefurbPlanRepository refurbPlanRepo,
            TesterBacklogRepository testerBacklogRepo,
            TesterRecordsRepository testerRecordsRepo,
            RefurbSchemesRepository refurbSchemesRepo,
            GrpcMethodInvoker grpcMethodInvoker,
            BlockingQueue<TesterResultRecord> resultQueue) {
        this.labPlansRepo = labPlansRepo;
        this.refurbPlanRepo = refurbPlanRepo;
        this.testerRecordsRepo = testerRecordsRepo;
        this.testerBacklogRepo = testerBacklogRepo;
        this.refurbSchemesRepo = refurbSchemesRepo;
        this.resultQueue = resultQueue;
        this.grpcMethodInvoker = grpcMethodInvoker;
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

    void processTesterResults() throws InterruptedException {
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
                labPlansRepo.setPlanStatusesForPlanId(labPlans.get(0), LabPlanStatusEnum.REFURB_BACKLOG_NEW.getStatusDescription());

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
            labPlansRepo.setPlanStatusesForPlanId(labPlans.get(0), LabPlanStatusEnum.TESTER_BACKLOG_RETRY.getStatusDescription());
        } else {
            // Fail-Reject
            logger.info("Battery [" + trr.batteryId() + "] FAILS > Rejected");
            // Update lab plan with end date
            labPlansRepo.endLabPlan(labPlans.get(0), Timestamp.from(Instant.now()));
            labPlansRepo.setPlanStatusesForPlanId(labPlans.get(0), LabPlanStatusEnum.TESTER_REJECTED.getStatusDescription());

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
                UpdateBatteryStatusRequest.newBuilder().setBattery(batteryIdStatus).build();

        UpdateBatteryStatusResponse response = grpcMethodInvoker.invokeNonblock(
                "opssvc",
                "updateBatteryStatus",
                request
        );

        return response.getSuccess();
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

        RemoveStorageBatteryResponse response = grpcMethodInvoker.invokeNonblock(
                "storagesvc",
                "removeStorageBattery",
                request
        );

        return response.getSuccess();
    }
}
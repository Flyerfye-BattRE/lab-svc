package com.battre.labsvc.controller;

import com.battre.labsvc.enums.LabPlanStatusEnum;
import com.battre.labsvc.enums.RefurbStationClassEnum;
import com.battre.labsvc.model.LabPlanType;
import com.battre.labsvc.model.RefurbPlanType;
import com.battre.labsvc.model.RefurbStationType;
import com.battre.labsvc.model.TesterBacklogType;
import com.battre.labsvc.model.TesterStationType;
import com.battre.labsvc.service.LabSvc;
import com.battre.stubs.services.BatteryIdType;
import com.battre.stubs.services.ChangeBatteryRefurbPriorityRequest;
import com.battre.stubs.services.ChangeBatteryRefurbPriorityResponse;
import com.battre.stubs.services.ChangeBatteryTesterPriorityRequest;
import com.battre.stubs.services.ChangeBatteryTesterPriorityResponse;
import com.battre.stubs.services.GetLabPlanRequest;
import com.battre.stubs.services.GetLabPlanResponse;
import com.battre.stubs.services.GetRefurbMaintenanceLogsRequest;
import com.battre.stubs.services.GetRefurbMaintenanceLogsResponse;
import com.battre.stubs.services.GetRefurbPlanRequest;
import com.battre.stubs.services.GetRefurbPlanResponse;
import com.battre.stubs.services.GetTesterBacklogRequest;
import com.battre.stubs.services.GetTesterBacklogResponse;
import com.battre.stubs.services.GetTesterMaintenanceLogsRequest;
import com.battre.stubs.services.GetTesterMaintenanceLogsResponse;
import com.battre.stubs.services.LabPlan;
import com.battre.stubs.services.LabSvcGrpc;
import com.battre.stubs.services.ProcessLabBatteriesRequest;
import com.battre.stubs.services.ProcessLabBatteriesResponse;
import com.battre.stubs.services.RefurbPlan;
import com.battre.stubs.services.RefurbStation;
import com.battre.stubs.services.RemoveLabBatteryRequest;
import com.battre.stubs.services.RemoveLabBatteryResponse;
import com.battre.stubs.services.TesterBacklog;
import com.battre.stubs.services.TesterStation;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

@GrpcService
public class LabSvcController extends LabSvcGrpc.LabSvcImplBase {
    private static final Logger logger = Logger.getLogger(LabSvcController.class.getName());

    private final LabSvc labSvc;

    @Autowired
    public LabSvcController(LabSvc labSvc) {
        this.labSvc = labSvc;
    }

    private static Timestamp toTimestamp(java.sql.Timestamp timestamp) {
        return Timestamp.newBuilder()
                .setSeconds(timestamp.toInstant().getEpochSecond())
                .setNanos(timestamp.toInstant().getNano())
                .build();
    }

    @Override
    public void processLabBatteries(ProcessLabBatteriesRequest request, StreamObserver<ProcessLabBatteriesResponse> responseObserver) {
        logger.info("processLabBatteries() started");

        boolean processBatteriesStatus = addBatteriesToLabAndTesterBacklog(request.getBatteryIdTypesList());

        ProcessLabBatteriesResponse response = ProcessLabBatteriesResponse.newBuilder()
                .setSuccess(processBatteriesStatus)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("processLabBatteries() completed");
    }

    @Transactional
    protected boolean addBatteriesToLabAndTesterBacklog(List<BatteryIdType> batteryIdsTypes) {
        return labSvc.addBatteriesToLabPlans(batteryIdsTypes) &&
                labSvc.addBatteriesToTesterBacklog(batteryIdsTypes);
    }

    @Override
    public void getLabPlans(
            GetLabPlanRequest request,
            StreamObserver<GetLabPlanResponse> responseObserver) {
        logger.info("getLabPlans() started");

        GetLabPlanResponse response = buildGetLabPlanResponse(labSvc.getLabPlans());

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("getLabPlans() completed");
    }

    @Override
    public void getCurrentLabPlans(
            GetLabPlanRequest request,
            StreamObserver<GetLabPlanResponse> responseObserver) {
        logger.info("getCurrentLabPlans() started");

        GetLabPlanResponse response = buildGetLabPlanResponse(labSvc.getCurrentLabPlans());

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("getCurrentLabPlans() completed");
    }

    private GetLabPlanResponse buildGetLabPlanResponse(List<LabPlanType> labPlans) {
        GetLabPlanResponse.Builder responseBuilder = GetLabPlanResponse.newBuilder();
        for (LabPlanType labPlan : labPlans) {
            LabPlanStatusEnum status = LabPlanStatusEnum.fromStatusCode(labPlan.getLabPlanStatusId());

            LabPlan.Builder labPlanBuilder = LabPlan.newBuilder()
                    .setLabPlanId(labPlan.getLabPlanId())
                    .setLabPlanStatus(status.getGrpcStatus())
                    .setBatteryId(labPlan.getBatteryId())
                    .setTesterRecordId(labPlan.getTesterRecordId())
                    .setRefurbPlanId(labPlan.getRefurbPlanId());

            responseBuilder.addLabPlanList(labPlanBuilder.build());
        }

        return responseBuilder.build();
    }

    @Override
    public void getCurrentTesterBacklog(GetTesterBacklogRequest request,
                                        StreamObserver<GetTesterBacklogResponse> responseObserver) {
        logger.info("getCurrentTesterBacklog() started");

        GetTesterBacklogResponse response = buildGetTesterBacklogResponse(labSvc.getCurrentTesterBacklog());

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("getCurrentTesterBacklog() completed");
    }

    @Override
    public void getTesterBacklog(GetTesterBacklogRequest request,
                                 StreamObserver<GetTesterBacklogResponse> responseObserver) {
        logger.info("getTesterBacklog() started");

        GetTesterBacklogResponse response = buildGetTesterBacklogResponse(labSvc.getTesterBacklog());

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("getTesterBacklog() completed");
    }

    private GetTesterBacklogResponse buildGetTesterBacklogResponse(List<TesterBacklogType> testerBacklogList) {
        GetTesterBacklogResponse.Builder responseBuilder = GetTesterBacklogResponse.newBuilder();
        for (TesterBacklogType backlogEntry : testerBacklogList) {
            TesterBacklog.Builder testerBacklogBuilder = TesterBacklog.newBuilder()
                    .setTesterBacklogId(backlogEntry.getTesterBacklogId())
                    .setBatteryId(backlogEntry.getBatteryId())
                    .setTesterBacklogPriority(backlogEntry.getTesterBacklogPriority())
                    .setTesterBacklogStartDate(toTimestamp(backlogEntry.getTesterBacklogStartDate()))
                    .setTesterBacklogEndDate(toTimestamp(backlogEntry.getTesterBacklogEndDate()));

            responseBuilder.addTesterBacklogList(testerBacklogBuilder.build());
        }

        return responseBuilder.build();
    }

    @Override
    public void getCurrentRefurbPlans(GetRefurbPlanRequest request,
                                      StreamObserver<GetRefurbPlanResponse> responseObserver) {
        logger.info("getCurrentRefurbPlans() started");

        GetRefurbPlanResponse response = buildGetRefurbPlanResponse(labSvc.getCurrentRefurbPlans());

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("getCurrentRefurbPlans() completed");
    }

    @Override
    public void getRefurbPlans(GetRefurbPlanRequest request,
                               StreamObserver<GetRefurbPlanResponse> responseObserver) {
        logger.info("getRefurbPlans() started");

        GetRefurbPlanResponse response = buildGetRefurbPlanResponse(labSvc.getRefurbPlans());

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("getRefurbPlans() completed");
    }

    private GetRefurbPlanResponse buildGetRefurbPlanResponse(List<RefurbPlanType> refurbPlanList) {
        GetRefurbPlanResponse.Builder responseBuilder = GetRefurbPlanResponse.newBuilder();
        for (RefurbPlanType planEntry : refurbPlanList) {
            RefurbPlan.Builder refurbPlanBuilder = RefurbPlan.newBuilder()
                    .setRefurbPlanId(planEntry.getRefurbPlanId())
                    .setBatteryId(planEntry.getBatteryId())
                    .setRefurbPlanPriority(planEntry.getRefurbPlanPriority())
                    .setRefurbPlanStartDate(toTimestamp(planEntry.getRefurbPlanStartDate()))
                    .setRefurbPlanEndDate(toTimestamp(planEntry.getRefurbPlanEndDate()))
                    .setAvailable(planEntry.isAvailable())
                    .setResolder(planEntry.getResolder())
                    .setResolderRecordId(planEntry.getResolderRecordId())
                    .setRepack(planEntry.getRepack())
                    .setRepackRecordId(planEntry.getRepackRecordId())
                    .setProcessorSwap(planEntry.getProcessorSwap())
                    .setProcessorSwapRecordId(planEntry.getProcessorSwapRecordId())
                    .setCapacitorSwap(planEntry.getCapacitorSwap())
                    .setCapacitorSwapRecordId(planEntry.getCapacitorSwapRecordId());

            responseBuilder.addRefurbPlanList(refurbPlanBuilder.build());
        }

        return responseBuilder.build();
    }

    @Override
    public void changeBatteryTesterPriority(ChangeBatteryTesterPriorityRequest request,
                                            StreamObserver<ChangeBatteryTesterPriorityResponse> responseObserver) {
        logger.info("changeBatteryTesterPriority() started");

        boolean success = labSvc.changeBatteryTesterPriority(request.getBatteryId(), request.getPriority());

        ChangeBatteryTesterPriorityResponse response = ChangeBatteryTesterPriorityResponse.newBuilder()
                .setSuccess(success)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("changeBatteryTesterPriority() completed");
    }

    @Override
    public void changeBatteryRefurbPriority(ChangeBatteryRefurbPriorityRequest request,
                                            StreamObserver<ChangeBatteryRefurbPriorityResponse> responseObserver) {
        logger.info("changeBatteryRefurbPriority() started");

        boolean success = labSvc.changeBatteryRefurbPriority(request.getBatteryId(), request.getPriority());

        ChangeBatteryRefurbPriorityResponse response = ChangeBatteryRefurbPriorityResponse.newBuilder()
                .setSuccess(success)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("changeBatteryRefurbPriority() completed");
    }

    @Override
    public void getTesterMaintenanceLogs(GetTesterMaintenanceLogsRequest request,
                                         StreamObserver<GetTesterMaintenanceLogsResponse> responseObserver) {
        logger.info("getTesterMaintenanceLogs() started");

        List<TesterStationType> testerStationList = labSvc.getTesterStationLogs();

        GetTesterMaintenanceLogsResponse.Builder responseBuilder = GetTesterMaintenanceLogsResponse.newBuilder();
        for (TesterStationType testerStation : testerStationList) {
            TesterStation.Builder testerStationBuilder = TesterStation.newBuilder()
                    .setTesterStnId(testerStation.getTesterStnId())
                    .setTerminalLayoutId(testerStation.getTerminalLayoutId())
                    .setInUse(testerStation.isInUse())
                    .setActiveBatteryId(testerStation.getActiveBatteryId())
                    .setLastActiveDate(toTimestamp(testerStation.getLastActiveDate()))
                    .setLastCalibrationDate(toTimestamp(testerStation.getLastCalibrationDate()))
                    .setNextCalibrationDate(toTimestamp(testerStation.getNextCalibrationDate()));

            responseBuilder.addTesterStationList(testerStationBuilder.build());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();

        logger.info("getTesterMaintenanceLogs() completed");
    }

    @Override
    public void getRefurbMaintenanceLogs(GetRefurbMaintenanceLogsRequest request,
                                         StreamObserver<GetRefurbMaintenanceLogsResponse> responseObserver) {
        logger.info("getRefurbMaintenanceLogs() started");

        List<RefurbStationType> refurbStationList = labSvc.getRefurbStationLogs();

        GetRefurbMaintenanceLogsResponse.Builder responseBuilder = GetRefurbMaintenanceLogsResponse.newBuilder();
        for (RefurbStationType refurbStation : refurbStationList) {
            RefurbStationClassEnum refurbStnClassId =
                    RefurbStationClassEnum.fromClassCode(refurbStation.getRefurbStationClassId());

            RefurbStation.Builder refurbStationBuilder = RefurbStation.newBuilder()
                    .setRefurbStnId(refurbStation.getRefurbStnId())
                    .setRefurbStationClass(refurbStnClassId.getGrpcClass())
                    .setInUse(refurbStation.isInUse())
                    .setActiveBatteryId(refurbStation.getActiveBatteryId())
                    .setLastActiveDate(toTimestamp(refurbStation.getLastActiveDate()))
                    .setLastCalibrationDate(toTimestamp(refurbStation.getLastCalibrationDate()))
                    .setNextCalibrationDate(toTimestamp(refurbStation.getNextCalibrationDate()));

            responseBuilder.addRefurbStationList(refurbStationBuilder.build());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();

        logger.info("getRefurbMaintenanceLogs() completed");
    }

    @Override
    public void removeLabBattery(RemoveLabBatteryRequest request,
                                 StreamObserver<RemoveLabBatteryResponse> responseObserver) {
        logger.info("removeLabBattery() started");

        boolean success = labSvc.removeBattery(request.getBatteryId());

        RemoveLabBatteryResponse response = RemoveLabBatteryResponse.newBuilder()
                .setSuccess(success)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("removeLabBattery() completed");
    }
}

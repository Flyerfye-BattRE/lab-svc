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
import com.battre.stubs.services.GetLabPlansRequest;
import com.battre.stubs.services.GetLabPlansResponse;
import com.battre.stubs.services.GetRefurbStnInfoRequest;
import com.battre.stubs.services.GetRefurbStnInfoResponse;
import com.battre.stubs.services.GetRefurbPlansRequest;
import com.battre.stubs.services.GetRefurbPlansResponse;
import com.battre.stubs.services.GetTesterBacklogRequest;
import com.battre.stubs.services.GetTesterBacklogResponse;
import com.battre.stubs.services.GetTesterStnInfoRequest;
import com.battre.stubs.services.GetTesterStnInfoResponse;
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
import com.google.protobuf.Int32Value;
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
            GetLabPlansRequest request,
            StreamObserver<GetLabPlansResponse> responseObserver) {
        logger.info("getLabPlans() started");

        GetLabPlansResponse response = buildGetLabPlansResponse(labSvc.getLabPlans());

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("getLabPlans() completed");
    }

    @Override
    public void getCurrentLabPlans(
            GetLabPlansRequest request,
            StreamObserver<GetLabPlansResponse> responseObserver) {
        logger.info("getCurrentLabPlans() started");

        GetLabPlansResponse response = buildGetLabPlansResponse(labSvc.getCurrentLabPlans());

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("getCurrentLabPlans() completed");
    }

    private GetLabPlansResponse buildGetLabPlansResponse(List<LabPlanType> labPlans) {
        GetLabPlansResponse.Builder responseBuilder = GetLabPlansResponse.newBuilder();
        for (LabPlanType labPlan : labPlans) {
            LabPlanStatusEnum status = LabPlanStatusEnum.fromStatusCode(labPlan.getLabPlanStatusId());

            LabPlan.Builder labPlanBuilder = LabPlan.newBuilder()
                    .setLabPlanId(labPlan.getLabPlanId())
                    .setLabPlanStatus(status.getGrpcStatus())
                    .setBatteryId(labPlan.getBatteryId());

            if (labPlan.getTesterRecordId() != null) {
                labPlanBuilder.setOptionalTesterRecordId(Int32Value.of(labPlan.getTesterRecordId()));
            }
            if (labPlan.getRefurbPlanId() != null) {
                labPlanBuilder.setOptionalRefurbPlanId(Int32Value.of(labPlan.getRefurbPlanId()));
            }

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
    public void getCurrentRefurbPlans(GetRefurbPlansRequest request,
                                      StreamObserver<GetRefurbPlansResponse> responseObserver) {
        logger.info("getCurrentRefurbPlans() started");

        GetRefurbPlansResponse response = buildGetRefurbPlansResponse(labSvc.getCurrentRefurbPlans());

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("getCurrentRefurbPlans() completed");
    }

    @Override
    public void getRefurbPlans(GetRefurbPlansRequest request,
                               StreamObserver<GetRefurbPlansResponse> responseObserver) {
        logger.info("getRefurbPlans() started");

        GetRefurbPlansResponse response = buildGetRefurbPlansResponse(labSvc.getRefurbPlans());

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("getRefurbPlans() completed");
    }

    private GetRefurbPlansResponse buildGetRefurbPlansResponse(List<RefurbPlanType> refurbPlanList) {
        GetRefurbPlansResponse.Builder responseBuilder = GetRefurbPlansResponse.newBuilder();
        for (RefurbPlanType planEntry : refurbPlanList) {
            RefurbPlan.Builder refurbPlanBuilder = RefurbPlan.newBuilder()
                    .setRefurbPlanId(planEntry.getRefurbPlanId())
                    .setBatteryId(planEntry.getBatteryId())
                    .setRefurbPlanPriority(planEntry.getRefurbPlanPriority())
                    .setRefurbPlanStartDate(toTimestamp(planEntry.getRefurbPlanStartDate()))
                    .setRefurbPlanEndDate(toTimestamp(planEntry.getRefurbPlanEndDate()))
                    .setAvailable(planEntry.isAvailable())
                    .setResolder(planEntry.getResolder())
                    .setRepack(planEntry.getRepack())
                    .setProcessorSwap(planEntry.getProcessorSwap())
                    .setCapacitorSwap(planEntry.getCapacitorSwap());


            if (planEntry.getResolderRecordId() != null) {
                refurbPlanBuilder.setOptionalResolderRecordId(Int32Value.of(planEntry.getResolderRecordId()));
            }
            if (planEntry.getRepackRecordId() != null) {
                refurbPlanBuilder.setOptionalRepackRecordId(Int32Value.of(planEntry.getRepackRecordId()));
            }
            if (planEntry.getProcessorSwapRecordId() != null) {
                refurbPlanBuilder.setOptionalProcessorSwapRecordId(Int32Value.of(planEntry.getProcessorSwapRecordId()));
            }
            if (planEntry.getCapacitorSwapRecordId() != null) {
                refurbPlanBuilder.setOptionalCapacitorSwapRecordId(Int32Value.of(planEntry.getCapacitorSwapRecordId()));
            }

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
    public void getTesterStnInfo(GetTesterStnInfoRequest request,
                                         StreamObserver<GetTesterStnInfoResponse> responseObserver) {
        logger.info("getTesterStnInfo() started");

        List<TesterStationType> testerStationList = labSvc.getTesterStationLogs();

        GetTesterStnInfoResponse.Builder responseBuilder = GetTesterStnInfoResponse.newBuilder();
        for (TesterStationType testerStation : testerStationList) {
            TesterStation.Builder testerStationBuilder = TesterStation.newBuilder()
                    .setTesterStnId(testerStation.getTesterStnId())
                    .setTerminalLayoutId(testerStation.getTerminalLayoutId())
                    .setInUse(testerStation.isInUse())
                    .setLastActiveDate(toTimestamp(testerStation.getLastActiveDate()))
                    .setLastCalibrationDate(toTimestamp(testerStation.getLastCalibrationDate()))
                    .setNextCalibrationDate(toTimestamp(testerStation.getNextCalibrationDate()));

            if (testerStation.getActiveBatteryId() != null) {
                testerStationBuilder.setOptionalActiveBatteryId(Int32Value.of(testerStation.getActiveBatteryId()));
            }

            responseBuilder.addTesterStationList(testerStationBuilder.build());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();

        logger.info("getTesterStnInfo() completed");
    }

    @Override
    public void getRefurbStnInfo(GetRefurbStnInfoRequest request,
                                         StreamObserver<GetRefurbStnInfoResponse> responseObserver) {
        logger.info("getRefurbStnInfo() started");

        List<RefurbStationType> refurbStationList = labSvc.getRefurbStationLogs();

        GetRefurbStnInfoResponse.Builder responseBuilder = GetRefurbStnInfoResponse.newBuilder();
        for (RefurbStationType refurbStation : refurbStationList) {
            RefurbStationClassEnum refurbStnClassId =
                    RefurbStationClassEnum.fromClassCode(refurbStation.getRefurbStationClassId());

            RefurbStation.Builder refurbStationBuilder = RefurbStation.newBuilder()
                    .setRefurbStnId(refurbStation.getRefurbStnId())
                    .setRefurbStationClass(refurbStnClassId.getGrpcClass())
                    .setInUse(refurbStation.isInUse())
                    .setLastActiveDate(toTimestamp(refurbStation.getLastActiveDate()))
                    .setLastCalibrationDate(toTimestamp(refurbStation.getLastCalibrationDate()))
                    .setNextCalibrationDate(toTimestamp(refurbStation.getNextCalibrationDate()));

            if (refurbStation.getActiveBatteryId() != null) {
                refurbStationBuilder.setOptionalActiveBatteryId(Int32Value.of(refurbStation.getActiveBatteryId()));
            }

            responseBuilder.addRefurbStationList(refurbStationBuilder.build());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();

        logger.info("getRefurbStnInfo() completed");
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

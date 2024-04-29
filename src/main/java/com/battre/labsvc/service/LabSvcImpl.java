package com.battre.labsvc.service;

import com.battre.stubs.services.BatteryIdType;
import com.battre.stubs.services.LabSvcGrpc;
import com.battre.stubs.services.ProcessLabBatteriesRequest;
import com.battre.stubs.services.ProcessLabBatteriesResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

@GrpcService
public class LabSvcImpl extends LabSvcGrpc.LabSvcImplBase {
    private static final Logger logger = Logger.getLogger(LabSvcImpl.class.getName());

    private final LabSvc labSvc;

    @Autowired
    public LabSvcImpl(LabSvc labSvc) {
        this.labSvc = labSvc;
    }

    @Override
    public void processLabBatteries(ProcessLabBatteriesRequest request, StreamObserver<ProcessLabBatteriesResponse> responseObserver) {
        logger.info("processLabBatteries() invoked");

        boolean processBatteriesStatus = addBatteriesToLabAndTesterBacklog(request.getBatteryIdTypesList());

        ProcessLabBatteriesResponse response = ProcessLabBatteriesResponse.newBuilder()
                .setSuccess(processBatteriesStatus)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("processLabBatteries() finished");
    }

    @Transactional
    private boolean addBatteriesToLabAndTesterBacklog(List<BatteryIdType> batteryIdsTypes) {
        return labSvc.addBatteriesToLabPlans(batteryIdsTypes) &&
                labSvc.addBatteriesToTesterBacklog(batteryIdsTypes);
    }


}

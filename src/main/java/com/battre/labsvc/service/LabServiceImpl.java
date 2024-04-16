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
public class LabServiceImpl extends LabSvcGrpc.LabSvcImplBase {
    private static final Logger logger = Logger.getLogger(LabServiceImpl.class.getName());

    private final LabService labService;

    @Autowired
    public LabServiceImpl(LabService labService) {
        this.labService = labService;
    }

    @Override
    public void processLabBatteries(ProcessLabBatteriesRequest request, StreamObserver<ProcessLabBatteriesResponse> response){
        logger.info("processLabBatteries invoked");

        boolean processBatteriesStatus = addBatteriesToLabAndTesterBacklog(request.getBatteryIdTypesList());

        ProcessLabBatteriesResponse myResponse = ProcessLabBatteriesResponse.newBuilder()
                .setSuccess(processBatteriesStatus)
                .build();

        response.onNext(myResponse);
        response.onCompleted();

        logger.info("processLabBatteries finished");
    }

    @Transactional
    private boolean addBatteriesToLabAndTesterBacklog(List<BatteryIdType> batteryIdsTypes) {
        return labService.addBatteriesToLabPlans(batteryIdsTypes) &&
                labService.addBatteriesToTesterBacklog(batteryIdsTypes);
    }



}

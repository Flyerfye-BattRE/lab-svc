package com.battre.labsvc.service;

import com.battre.labsvc.model.LabPlanType;
import com.battre.labsvc.model.TesterBacklogType;
import com.battre.labsvc.repository.LabPlansRepository;
import com.battre.labsvc.repository.TesterBacklogRepository;
import com.battre.stubs.services.BatteryIdType;
import com.battre.stubs.services.BatteryTypeTerminalPair;
import com.battre.stubs.services.GetBatteryTerminalLayoutsRequest;
import com.battre.stubs.services.GetBatteryTerminalLayoutsResponse;
import com.battre.stubs.services.SpecSvcGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class LabService {
    private static final Logger logger = Logger.getLogger(LabService.class.getName());

    private final LabPlansRepository labPlansRepository;

    private final TesterBacklogRepository testerBacklogRepository;

    @GrpcClient("specSvc")
    private SpecSvcGrpc.SpecSvcStub specSvcClient;

    @Autowired
    LabService(LabPlansRepository labPlansRepository, TesterBacklogRepository testerBacklogRepository) {
        this.labPlansRepository = labPlansRepository;
        this.testerBacklogRepository = testerBacklogRepository;
    }

    public boolean addBatteriesToLabPlans(List<BatteryIdType> batteryIdsTypes){
        //create new lab plan records for all the batteries
        for(BatteryIdType batteryInfo:batteryIdsTypes) {
            LabPlanType labPlan = new LabPlanType(
                    batteryInfo.getBatteryId()
            );

            labPlansRepository.save(labPlan);
        }

        return true;
    }
    public boolean addBatteriesToTesterBacklog(List<BatteryIdType> batteryIdsTypes){
        //query spec svc for terminal_layout_ids per batteryId
        Set<Integer> batteryTypesSet = new HashSet<>();

        //de-duplicate battery types before querying for corresponding terminal ids
        for(BatteryIdType batteryInfo:batteryIdsTypes) {
            batteryTypesSet.add(batteryInfo.getBatteryTypeId());
        }

        Map<Integer, Integer> batteryTypeToTerminalIds = getBatteryTerminalIdMap(batteryTypesSet.stream().toList());

        for(BatteryIdType batteryInfo:batteryIdsTypes) {
            TesterBacklogType testerBacklogEntry = new TesterBacklogType(
                    batteryInfo.getBatteryId(),
                    batteryTypeToTerminalIds.get(batteryInfo.getBatteryTypeId())
            );

            testerBacklogRepository.save(testerBacklogEntry);
        }

        return true;
    }

    private Map<Integer, Integer> getBatteryTerminalIdMap(List<Integer> batteryTypeIds){
        GetBatteryTerminalLayoutsRequest request = GetBatteryTerminalLayoutsRequest
                .newBuilder()
                .addAllBatteryTypeIds(batteryTypeIds)
                .build();

        CompletableFuture<GetBatteryTerminalLayoutsResponse> responseFuture = new CompletableFuture<>();

        // Create a StreamObserver to handle the response asynchronously
        StreamObserver<GetBatteryTerminalLayoutsResponse> responseObserver = new StreamObserver<>() {
            private GetBatteryTerminalLayoutsResponse batteryTerminalLayoutsResponse;

            @Override
            public void onNext(GetBatteryTerminalLayoutsResponse response) {
                responseFuture.complete(response);
            }

            @Override
            public void onError(Throwable t) {
                // Handle any errors
                logger.severe("getBatteryTerminalLayouts() errored: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // Handle the completion
                logger.info("getBatteryTerminalLayouts() completed");
            }
        };

        specSvcClient.getBatteryTerminalLayouts(request, responseObserver);

        Map<Integer, Integer> batteryTypeToTerminalIds = null;

        // Wait for the response or 1 sec handle timeout
        try {
            // Blocks until the response is available
            batteryTypeToTerminalIds = convertToBatteryTypeTerminalMap(
                    responseFuture.get(5, TimeUnit.SECONDS).getPairsList()
            );
        } catch (Exception e) {
            logger.severe("getBatteryTerminalLayouts responseFuture error: " + e.getMessage());
        }

        return batteryTypeToTerminalIds;
    }

    private Map<Integer, Integer> convertToBatteryTypeTerminalMap(List<BatteryTypeTerminalPair> batteryTypeTerminalList) {
        return batteryTypeTerminalList.stream()
                .collect(Collectors.toMap(
                        batteryTypeTerminal -> batteryTypeTerminal.getBatteryTypeId(),
                        batteryTypeTerminal -> batteryTypeTerminal.getBatteryTerminalLayoutId()
                ));
    }
}

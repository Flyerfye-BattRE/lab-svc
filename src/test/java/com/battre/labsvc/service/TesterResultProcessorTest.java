package com.battre.labsvc.service;

import com.battre.grpcifc.GrpcMethodInvoker;
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
import com.battre.stubs.services.RemoveBatteryRequest;
import com.battre.stubs.services.RemoveBatteryResponse;
import com.battre.stubs.services.UpdateBatteryStatusRequest;
import com.battre.stubs.services.UpdateBatteryStatusResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TesterResultProcessorTest {

    @Mock
    private LabPlansRepository labPlansRepo;
    @Mock
    private RefurbPlanRepository refurbPlanRepo;
    @Mock
    private TesterBacklogRepository testerBacklogRepo;
    @Mock
    private TesterRecordsRepository testerRecordsRepo;
    @Mock
    private RefurbSchemesRepository refurbSchemesRepo;
    @Mock
    private BlockingQueue<TesterResultRecord> resultQueue;
    @Mock
    private GrpcMethodInvoker grpcMethodInvoker;
    @InjectMocks
    private TesterResultProcessor testerResultProcessor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testerResultProcessor = new TesterResultProcessor(
                labPlansRepo,
                refurbPlanRepo,
                testerBacklogRepo,
                testerRecordsRepo,
                refurbSchemesRepo,
                grpcMethodInvoker,
                resultQueue
        );
    }

    public void mockUpdateBatteryStatus(UpdateBatteryStatusResponse response) {
        doAnswer(invocation -> {
            StreamObserver<UpdateBatteryStatusResponse> observer = invocation.getArgument(3);
            observer.onNext(response);
            observer.onCompleted();
            return null;
        }).when(grpcMethodInvoker).callMethod(
                eq("opssvc"),
                eq("updateBatteryStatus"),
                any(UpdateBatteryStatusRequest.class),
                any(StreamObserver.class)
        );
    }

    public void mockUpdateStorageSvcRemoveBattery(RemoveBatteryResponse response) {
        doAnswer(invocation -> {
            StreamObserver<RemoveBatteryResponse> observer = invocation.getArgument(3);
            observer.onNext(response);
            observer.onCompleted();
            return null;
        }).when(grpcMethodInvoker).callMethod(
                eq("storagesvc"),
                eq("removeBattery"),
                any(RemoveBatteryRequest.class),
                any(StreamObserver.class)
        );
    }

    @Test
    public void testProcessTesterResults_Pass() throws InterruptedException {
        TesterResultRecord resultRecord = new TesterResultRecord(
                1,  // testerStnId
                3,  // batteryId
                4,  // testSchemeId
                5,  // refurbSchemeId
                2,  // terminalLayoutId
                LabResult.PASS.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        when(resultQueue.take()).thenReturn(resultRecord);

        TesterRecordType testerRecord = new TesterRecordType(
                1,  // testerStnId
                3,  // batteryId
                4,  // testSchemeId
                5,  // refurbSchemeId
                LabResult.PASS.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        testerRecord.setTesterRecordId(5);
        when(testerRecordsRepo.save(any(TesterRecordType.class))).thenReturn(testerRecord);

        List<Integer> mockPlans = List.of(4);
        when(labPlansRepo.getLabPlanIdsForBatteryId(3)).thenReturn(mockPlans);

        Optional<RefurbSchemeType> refurbSchemeDataResponse = Optional.of(new RefurbSchemeType(
                5,
                false,
                true,
                false,
                true
        ));
        when(refurbSchemesRepo.getDataForRefurbScheme(5)).thenReturn(refurbSchemeDataResponse);

        RefurbPlanType refurbPlan = new RefurbPlanType(
                3,  // batteryId
                true,  // resolder
                true,  // repack
                true,  // processorSwap
                true  // capacitorSwap
        );
        refurbPlan.setRefurbPlanId(5);
        when(refurbPlanRepo.save(any(RefurbPlanType.class))).thenReturn(refurbPlan);

        UpdateBatteryStatusResponse response =
                UpdateBatteryStatusResponse.newBuilder().build();
        mockUpdateBatteryStatus(response);

        testerResultProcessor.processTesterResults();

        verify(testerRecordsRepo, times(1)).save(any(TesterRecordType.class));
        verify(labPlansRepo, times(1)).setTesterRecordForLabPlan(4, 5);

        verify(refurbPlanRepo, times(1)).save(any(RefurbPlanType.class));
        verify(labPlansRepo, times(1)).setRefurbPlanForLabPlan(anyInt(), anyInt());
        verify(grpcMethodInvoker, times(1)).callMethod(
                eq("opssvc"),
                eq("updateBatteryStatus"),
                any(UpdateBatteryStatusRequest.class),
                any(StreamObserver.class)
        );
    }

    @Test
    public void testProcessTesterResults_FailRetry() throws InterruptedException {
        TesterResultRecord resultRecord = new TesterResultRecord(
                1,  // testerStnId
                3,  // batteryId
                4,  // testSchemeId
                5,  // refurbSchemeId
                2,  // terminalLayoutId
                LabResult.FAIL_RETRY.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        when(resultQueue.take()).thenReturn(resultRecord);

        TesterRecordType testerRecord = new TesterRecordType(
                1,  // testerStnId
                3,  // batteryId
                4,  // testSchemeId
                5,  // refurbSchemeId
                LabResult.FAIL_RETRY.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        testerRecord.setTesterRecordId(5);
        when(testerRecordsRepo.save(any(TesterRecordType.class))).thenReturn(testerRecord);

        List<Integer> mockPlans = List.of(4);
        when(labPlansRepo.getLabPlanIdsForBatteryId(3)).thenReturn(mockPlans);

        testerResultProcessor.processTesterResults();

        verify(testerBacklogRepo, times(1)).save(any(TesterBacklogType.class));
    }

    @Test
    public void testProcessTesterResults_FailReject() throws InterruptedException {
        TesterResultRecord resultRecord = new TesterResultRecord(
                1,  // testerStnId
                3,  // batteryId
                4,  // testSchemeId
                5,  // refurbSchemeId
                2,  // terminalLayoutId
                LabResult.FAIL_REJECT.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        when(resultQueue.take()).thenReturn(resultRecord);

        TesterRecordType testerRecord = new TesterRecordType(
                1,  // testerStnId
                3,  // batteryId
                4,  // testSchemeId
                5,  // refurbSchemeId
                LabResult.FAIL_REJECT.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        testerRecord.setTesterRecordId(5);
        when(testerRecordsRepo.save(any(TesterRecordType.class))).thenReturn(testerRecord);

        List<Integer> mockPlans = List.of(4);
        when(labPlansRepo.getLabPlanIdsForBatteryId(3)).thenReturn(mockPlans);

        UpdateBatteryStatusResponse response =
                UpdateBatteryStatusResponse.newBuilder().build();
        mockUpdateBatteryStatus(response);

        RemoveBatteryResponse storageSvcResponse =
                RemoveBatteryResponse.newBuilder().build();
        mockUpdateStorageSvcRemoveBattery(storageSvcResponse);

        testerResultProcessor.processTesterResults();

        verify(labPlansRepo, times(1)).endLabPlan(eq(4), any(Timestamp.class));
        verify(grpcMethodInvoker, times(1)).callMethod(
                eq("opssvc"),
                eq("updateBatteryStatus"),
                any(UpdateBatteryStatusRequest.class),
                any(StreamObserver.class)
        );
        verify(grpcMethodInvoker, times(1)).callMethod(
                eq("storagesvc"),
                eq("removeBattery"),
                any(RemoveBatteryRequest.class),
                any(StreamObserver.class)
        );
    }
}
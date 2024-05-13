package com.battre.labsvc.service;


import com.battre.labsvc.enums.LabResult;
import com.battre.labsvc.enums.RefurbStationClass;
import com.battre.labsvc.model.RefurbRecordType;
import com.battre.labsvc.repository.LabPlansRepository;
import com.battre.labsvc.repository.RefurbPlanRepository;
import com.battre.labsvc.repository.RefurbRecordsRepository;
import com.battre.labsvc.repository.RefurbStationRepository;
import com.battre.stubs.services.OpsSvcGrpc;
import com.battre.stubs.services.RemoveBatteryRequest;
import com.battre.stubs.services.RemoveBatteryResponse;
import com.battre.stubs.services.StorageSvcGrpc;
import com.battre.stubs.services.UpdateBatteryStatusRequest;
import com.battre.stubs.services.UpdateBatteryStatusResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RefurbResultProcessorTest {
    @Mock
    private LabPlansRepository labPlansRepo;
    @Mock
    private RefurbPlanRepository refurbPlanRepo;
    @Mock
    private RefurbStationRepository refurbStationsRepo;
    @Mock
    private RefurbRecordsRepository refurbRecordsRepo;
    @Mock
    private BlockingQueue<RefurbResultRecord> resultQueue;
    @Mock
    private OpsSvcGrpc.OpsSvcStub opsSvcClient;
    @Mock
    private StorageSvcGrpc.StorageSvcStub storageSvcClient;
    @InjectMocks
    private RefurbResultProcessor refurbResultProcessor;
    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        refurbResultProcessor = new RefurbResultProcessor(labPlansRepo, refurbPlanRepo, refurbStationsRepo, refurbRecordsRepo, resultQueue);
        refurbResultProcessor.setOpsSvcClient(opsSvcClient);
        refurbResultProcessor.setStorageSvcClient(storageSvcClient);
    }

    public void mockUpdateBatteryStatus(OpsSvcGrpc.OpsSvcStub opsSvcClient, UpdateBatteryStatusResponse response) {
        doAnswer(invocation -> {
            StreamObserver<UpdateBatteryStatusResponse> observer = invocation.getArgument(1);
            observer.onNext(response);
            observer.onCompleted();
            return null;
        }).when(opsSvcClient).updateBatteryStatus(any(UpdateBatteryStatusRequest.class), any(StreamObserver.class));
    }

    public void mockUpdateStorageSvcRemoveBattery(StorageSvcGrpc.StorageSvcStub storageSvcClient, RemoveBatteryResponse response) {
        doAnswer(invocation -> {
            StreamObserver<RemoveBatteryResponse> observer = invocation.getArgument(1);
            observer.onNext(response);
            observer.onCompleted();
            return null;
        }).when(storageSvcClient).removeBattery(any(RemoveBatteryRequest.class), any(StreamObserver.class));
    }

    @Test
    public void testProcessRefurbResults_Pass() throws Exception {
        RefurbResultRecord resultRecord = new RefurbResultRecord(
                1,  // refurbPlanId
                2,  // refurbStnId
                RefurbStationClass.RESOLDER,  // refurbStnClass
                3,  // batteryId
                LabResult.PASS.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        when(resultQueue.take()).thenReturn(resultRecord);

        RefurbRecordType refurbRecord = new RefurbRecordType(
                1,  // refurbPlanId
                2,  // refurbStnId
                RefurbStationClass.RESOLDER.toString(),  // refurbStnClass
                3,  // batteryId
                LabResult.PASS.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        refurbRecord.setRefurbRecordId(5);
        when(refurbRecordsRepo.save(any(RefurbRecordType.class))).thenReturn(refurbRecord);
        when(refurbPlanRepo.checkRefurbPlanCompleted(anyInt())).thenReturn(true);

        UpdateBatteryStatusResponse response =
                UpdateBatteryStatusResponse.newBuilder().build();
        mockUpdateBatteryStatus(opsSvcClient, response);

        refurbResultProcessor.processRefurbResults();

        verify(refurbPlanRepo, times(1)).setResolderRecord(eq(1), eq(5));
        verify(refurbStationsRepo, times(1)).markRefurbStnFree(eq(2), any(Timestamp.class));
        verify(labPlansRepo, times(1)).endLabPlanEntryForRefurbPlan(eq(1), any(Timestamp.class));
        verify(refurbPlanRepo, times(1)).endRefurbPlanEntry(eq(1), any(Timestamp.class));
    }

    @Test
    public void testProcessRefurbResults_NoRefurb() throws Exception {
        RefurbResultRecord resultRecord = new RefurbResultRecord(
                1,  // refurbPlanId
                2,  // refurbStnId
                RefurbStationClass.NO_REFURB,  // refurbStnClass
                3,  // batteryId
                LabResult.PASS.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        when(resultQueue.take()).thenReturn(resultRecord);

        RefurbRecordType refurbRecord = new RefurbRecordType(
                1,  // refurbPlanId
                2,  // refurbStnId
                RefurbStationClass.NO_REFURB.toString(),  // refurbStnClass
                3,  // batteryId
                LabResult.PASS.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        refurbRecord.setRefurbRecordId(5);
        when(refurbRecordsRepo.save(any(RefurbRecordType.class))).thenReturn(refurbRecord);
        when(refurbPlanRepo.checkRefurbPlanCompleted(anyInt())).thenReturn(true);

        UpdateBatteryStatusResponse response =
                UpdateBatteryStatusResponse.newBuilder().build();
        mockUpdateBatteryStatus(opsSvcClient, response);

        refurbResultProcessor.processRefurbResults();

        verify(labPlansRepo, times(1)).endLabPlanEntryForRefurbPlan(eq(1), any(Timestamp.class));
        verify(refurbPlanRepo, times(1)).endRefurbPlanEntry(eq(1), any(Timestamp.class));
        verify(opsSvcClient, times(1)).updateBatteryStatus(
                any(UpdateBatteryStatusRequest.class),
                any(StreamObserver.class)
        );
    }

    @Test
    public void testProcessRefurbResults_FailRetry() throws Exception {
        RefurbResultRecord resultRecord = new RefurbResultRecord(
                1,  // refurbPlanId
                2,  // refurbStnId
                RefurbStationClass.REPACK,  // refurbStnClass
                3,  // batteryId
                LabResult.FAIL_RETRY.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        when(resultQueue.take()).thenReturn(resultRecord);

        RefurbRecordType refurbRecord = new RefurbRecordType(
                1,  // refurbPlanId
                2,  // refurbStnId
                RefurbStationClass.REPACK.toString(),  // refurbStnClass
                3,  // batteryId
                LabResult.FAIL_RETRY.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        refurbRecord.setRefurbRecordId(5);
        when(refurbRecordsRepo.save(any(RefurbRecordType.class))).thenReturn(refurbRecord);

        refurbResultProcessor.processRefurbResults();

        verify(refurbStationsRepo, times(1)).markRefurbStnFree(eq(2), any(Timestamp.class));
        verify(refurbPlanRepo, times(1)).markRefurbPlanAvail(eq(1));
    }

    @Test
    public void testProcessRefurbResults_FailReject() throws Exception {
        RefurbResultRecord resultRecord = new RefurbResultRecord(
                1,  // refurbPlanId
                2,  // refurbStnId
                RefurbStationClass.PROCESSOR_SWAP,  // refurbStnClass
                3,  // batteryId
                LabResult.FAIL_REJECT.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        when(resultQueue.take()).thenReturn(resultRecord);

        RefurbRecordType refurbRecord = new RefurbRecordType(
                1,  // refurbPlanId
                2,  // refurbStnId
                RefurbStationClass.PROCESSOR_SWAP.toString(),  // refurbStnClass
                3,  // batteryId
                LabResult.FAIL_REJECT.getStatusCode(),  // resultTypeId
                Timestamp.valueOf("2024-05-10 12:00:00")  // testDate
        );
        refurbRecord.setRefurbRecordId(5);
        when(refurbRecordsRepo.save(any(RefurbRecordType.class))).thenReturn(refurbRecord);

        List<Integer> mockPlans = List.of(4);
        when(labPlansRepo.getLabPlanIdsForBatteryId(3)).thenReturn(mockPlans);

        UpdateBatteryStatusResponse opsSvcResponse =
                UpdateBatteryStatusResponse.newBuilder().build();
        mockUpdateBatteryStatus(opsSvcClient, opsSvcResponse);

        RemoveBatteryResponse storageSvcResponse =
                RemoveBatteryResponse.newBuilder().build();
        mockUpdateStorageSvcRemoveBattery(storageSvcClient, storageSvcResponse);

        refurbResultProcessor.processRefurbResults();

        verify(refurbPlanRepo, times(1)).setProcessorSwapRecord(eq(1), eq(5));
        verify(refurbStationsRepo, times(1)).markRefurbStnFree(eq(2), any(Timestamp.class));
        verify(labPlansRepo, times(1)).endLabPlan(eq(4), any(Timestamp.class));
        verify(refurbPlanRepo, times(1)).endRefurbPlanEntry(eq(1), any(Timestamp.class));
        verify(opsSvcClient, times(1)).updateBatteryStatus(
                any(UpdateBatteryStatusRequest.class),
                any(StreamObserver.class)
        );
        verify(storageSvcClient, times(1)).removeBattery(
                any(RemoveBatteryRequest.class),
                any(StreamObserver.class)
        );
    }
}

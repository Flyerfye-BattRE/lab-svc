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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LabSvcTests {
    @Mock
    private LabPlansRepository labPlansRepo;
    @Mock
    private TesterBacklogRepository testerBacklogRepo;
    @Mock
    private SpecSvcGrpc.SpecSvcStub specSvcClient;
    private LabSvc labSvc;
    private AutoCloseable closeable;

    public static void mockGetBatteryTerminalLayouts(SpecSvcGrpc.SpecSvcStub specSvcClient, GetBatteryTerminalLayoutsResponse response) {
        doAnswer(invocation -> {
            StreamObserver<GetBatteryTerminalLayoutsResponse> observer = invocation.getArgument(1);
            observer.onNext(response);
            observer.onCompleted();
            return null;
        }).when(specSvcClient).getBatteryTerminalLayouts(any(GetBatteryTerminalLayoutsRequest.class), any());
    }

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
        labSvc = new LabSvc(labPlansRepo, testerBacklogRepo);
        labSvc.setSpecSvcClient(specSvcClient);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    public void testAddBatteriesToLabPlans() {
        List<BatteryIdType> batteryIdsTypes = List.of(
                BatteryIdType.newBuilder().setBatteryId(1).setBatteryTypeId(3).build(),
                BatteryIdType.newBuilder().setBatteryId(2).setBatteryTypeId(4).build(),
                BatteryIdType.newBuilder().setBatteryId(3).setBatteryTypeId(3).build()
        );

        boolean result = labSvc.addBatteriesToLabPlans(batteryIdsTypes);
        assertTrue(result);

        ArgumentCaptor<LabPlanType> captor = ArgumentCaptor.forClass(LabPlanType.class);
        verify(labPlansRepo, times(3)).save(captor.capture());
        List<LabPlanType> capturedVals = captor.getAllValues();
        assertEquals(3, capturedVals.size());
        assertEquals(1, capturedVals.get(0).getBatteryId());
        assertEquals(2, capturedVals.get(1).getBatteryId());
        assertEquals(3, capturedVals.get(2).getBatteryId());
    }

    @Test
    public void testAddBatteriesToTesterBacklog() {
        List<BatteryIdType> batteryIdsTypes = List.of(
                BatteryIdType.newBuilder().setBatteryId(4).setBatteryTypeId(2).build(),
                BatteryIdType.newBuilder().setBatteryId(5).setBatteryTypeId(1).build(),
                BatteryIdType.newBuilder().setBatteryId(6).setBatteryTypeId(3).build(),
                BatteryIdType.newBuilder().setBatteryId(7).setBatteryTypeId(4).build()
        );

        List<BatteryTypeTerminalPair> batteryTypeTerminalList = List.of(
                BatteryTypeTerminalPair.newBuilder().setBatteryTypeId(1).setBatteryTerminalLayoutId(4).build(),
                BatteryTypeTerminalPair.newBuilder().setBatteryTypeId(2).setBatteryTerminalLayoutId(3).build(),
                BatteryTypeTerminalPair.newBuilder().setBatteryTypeId(3).setBatteryTerminalLayoutId(5).build(),
                BatteryTypeTerminalPair.newBuilder().setBatteryTypeId(4).setBatteryTerminalLayoutId(2).build()
        );
        GetBatteryTerminalLayoutsResponse response =
                GetBatteryTerminalLayoutsResponse.newBuilder().addAllBatteries(batteryTypeTerminalList).build();
        mockGetBatteryTerminalLayouts(specSvcClient, response);

        boolean result = labSvc.addBatteriesToTesterBacklog(batteryIdsTypes);
        assertTrue(result);

        ArgumentCaptor<TesterBacklogType> captor = ArgumentCaptor.forClass(TesterBacklogType.class);
        verify(testerBacklogRepo, times(4)).save(captor.capture());
        List<TesterBacklogType> capturedVals = captor.getAllValues();
        assertEquals(4, capturedVals.size());
        assertEquals(4, capturedVals.get(0).getBatteryId());
        assertEquals(3, capturedVals.get(0).getTerminalLayoutId());
        assertEquals(5, capturedVals.get(1).getBatteryId());
        assertEquals(4, capturedVals.get(1).getTerminalLayoutId());
        assertEquals(6, capturedVals.get(2).getBatteryId());
        assertEquals(5, capturedVals.get(2).getTerminalLayoutId());
        assertEquals(7, capturedVals.get(3).getBatteryId());
        assertEquals(2, capturedVals.get(3).getTerminalLayoutId());
    }
}

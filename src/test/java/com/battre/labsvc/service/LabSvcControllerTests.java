package com.battre.labsvc.service;

import com.battre.labsvc.controller.LabSvcController;
import com.battre.stubs.services.ProcessLabBatteriesRequest;
import com.battre.stubs.services.ProcessLabBatteriesResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LabSvcControllerTests {
    @Mock
    private LabSvc labSvc;

    @Mock
    private StreamObserver<ProcessLabBatteriesResponse> responseProcessLabBatteriesResponse;

    private LabSvcController labSvcController;

    private AutoCloseable closeable;

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
        labSvcController = new LabSvcController(labSvc);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void testProcessLabBatteriesSuccess() {
        labSvcController = new LabSvcController(labSvc);

        when(labSvc.addBatteriesToLabPlans(any(List.class))).thenReturn(true);
        when(labSvc.addBatteriesToTesterBacklog(any(List.class))).thenReturn(true);
        ProcessLabBatteriesRequest request = ProcessLabBatteriesRequest.newBuilder().build();

        labSvcController.processLabBatteries(request, responseProcessLabBatteriesResponse);

        verify(labSvc).addBatteriesToLabPlans(any(List.class));
        verify(labSvc).addBatteriesToTesterBacklog(any(List.class));
        verify(responseProcessLabBatteriesResponse).onNext(ProcessLabBatteriesResponse.newBuilder().setSuccess(true).build());
        verify(responseProcessLabBatteriesResponse).onCompleted();
    }

    @Test
    void testProcessLabBatteriesAddBatteriesToLabPlansFail() {
        labSvcController = new LabSvcController(labSvc);

        when(labSvc.addBatteriesToLabPlans(any(List.class))).thenReturn(false);
        ProcessLabBatteriesRequest request = ProcessLabBatteriesRequest.newBuilder().build();

        labSvcController.processLabBatteries(request, responseProcessLabBatteriesResponse);

        verify(labSvc).addBatteriesToLabPlans(any(List.class));
        verify(responseProcessLabBatteriesResponse).onNext(ProcessLabBatteriesResponse.newBuilder().setSuccess(false).build());
        verify(responseProcessLabBatteriesResponse).onCompleted();
    }

    @Test
    void testProcessLabBatteriesAddBatteriesToTesterBacklogFail() {
        labSvcController = new LabSvcController(labSvc);

        when(labSvc.addBatteriesToLabPlans(any(List.class))).thenReturn(true);
        when(labSvc.addBatteriesToTesterBacklog(any(List.class))).thenReturn(false);
        ProcessLabBatteriesRequest request = ProcessLabBatteriesRequest.newBuilder().build();

        labSvcController.processLabBatteries(request, responseProcessLabBatteriesResponse);

        verify(labSvc).addBatteriesToLabPlans(any(List.class));
        verify(labSvc).addBatteriesToTesterBacklog(any(List.class));
        verify(responseProcessLabBatteriesResponse).onNext(ProcessLabBatteriesResponse.newBuilder().setSuccess(false).build());
        verify(responseProcessLabBatteriesResponse).onCompleted();
    }
}

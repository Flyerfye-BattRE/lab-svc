package com.battre.labsvc.service;

import com.battre.stubs.services.ProcessLabBatteriesRequest;
import com.battre.stubs.services.ProcessLabBatteriesResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LabSvcImplTests {
    @Mock
    private LabSvc labSvc;

    @Mock
    private StreamObserver<ProcessLabBatteriesResponse> responseProcessLabBatteriesResponse;

    private LabSvcImpl labSvcImpl;

    private AutoCloseable closeable;

    @BeforeEach
    public void openMocks() {
        closeable = MockitoAnnotations.openMocks(this);
        labSvcImpl = new LabSvcImpl(labSvc);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void testProcessLabBatteriesSuccess() {
        labSvcImpl = new LabSvcImpl(labSvc);

        when(labSvc.addBatteriesToLabPlans(any())).thenReturn(true);
        when(labSvc.addBatteriesToTesterBacklog(any())).thenReturn(true);
        ProcessLabBatteriesRequest request = ProcessLabBatteriesRequest.newBuilder().build();

        labSvcImpl.processLabBatteries(request, responseProcessLabBatteriesResponse);

        verify(labSvc).addBatteriesToLabPlans(any());
        verify(labSvc).addBatteriesToTesterBacklog(any());
        verify(responseProcessLabBatteriesResponse).onNext(ProcessLabBatteriesResponse.newBuilder().setSuccess(true).build());
        verify(responseProcessLabBatteriesResponse).onCompleted();
    }

    @Test
    void testProcessLabBatteriesAddBatteriesToLabPlansFail() {
        labSvcImpl = new LabSvcImpl(labSvc);

        when(labSvc.addBatteriesToLabPlans(any())).thenReturn(false);
        ProcessLabBatteriesRequest request = ProcessLabBatteriesRequest.newBuilder().build();

        labSvcImpl.processLabBatteries(request, responseProcessLabBatteriesResponse);

        verify(labSvc).addBatteriesToLabPlans(any());
        verify(responseProcessLabBatteriesResponse).onNext(ProcessLabBatteriesResponse.newBuilder().setSuccess(false).build());
        verify(responseProcessLabBatteriesResponse).onCompleted();
    }

    @Test
    void testProcessLabBatteriesAddBatteriesToTesterBacklogFail() {
        labSvcImpl = new LabSvcImpl(labSvc);

        when(labSvc.addBatteriesToLabPlans(any())).thenReturn(true);
        when(labSvc.addBatteriesToTesterBacklog(any())).thenReturn(false);
        ProcessLabBatteriesRequest request = ProcessLabBatteriesRequest.newBuilder().build();

        labSvcImpl.processLabBatteries(request, responseProcessLabBatteriesResponse);

        verify(labSvc).addBatteriesToLabPlans(any());
        verify(labSvc).addBatteriesToTesterBacklog(any());
        verify(responseProcessLabBatteriesResponse).onNext(ProcessLabBatteriesResponse.newBuilder().setSuccess(false).build());
        verify(responseProcessLabBatteriesResponse).onCompleted();
    }
}

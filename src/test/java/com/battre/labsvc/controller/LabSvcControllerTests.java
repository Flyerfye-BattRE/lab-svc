package com.battre.labsvc.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.battre.labsvc.service.LabSvc;
import com.battre.stubs.services.ProcessLabBatteriesRequest;
import com.battre.stubs.services.ProcessLabBatteriesResponse;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LabSvcControllerTests {
  @Mock private LabSvc labSvc;

  @Mock private StreamObserver<ProcessLabBatteriesResponse> responseProcessLabBatteriesResponse;

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
    verify(responseProcessLabBatteriesResponse)
        .onNext(ProcessLabBatteriesResponse.newBuilder().setSuccess(true).build());
    verify(responseProcessLabBatteriesResponse).onCompleted();
  }

  @Test
  void testProcessLabBatteriesAddBatteriesToLabPlansFail() {
    labSvcController = new LabSvcController(labSvc);

    when(labSvc.addBatteriesToLabPlans(any(List.class))).thenReturn(false);
    ProcessLabBatteriesRequest request = ProcessLabBatteriesRequest.newBuilder().build();

    labSvcController.processLabBatteries(request, responseProcessLabBatteriesResponse);

    verify(labSvc).addBatteriesToLabPlans(any(List.class));
    verify(responseProcessLabBatteriesResponse)
        .onNext(ProcessLabBatteriesResponse.newBuilder().setSuccess(false).build());
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
    verify(responseProcessLabBatteriesResponse)
        .onNext(ProcessLabBatteriesResponse.newBuilder().setSuccess(false).build());
    verify(responseProcessLabBatteriesResponse).onCompleted();
  }

  @Test
  void testGetLabPlans() {
    // TODO: Implement test
  }

  @Test
  void testGetCurrentLabPlans() {
    // TODO: Implement test
  }

  @Test
  void testGetCurrentTesterBacklog() {
    // TODO: Implement test
  }

  @Test
  void testGetTesterBacklog() {
    // TODO: Implement test
  }

  @Test
  void testGetCurrentRefurbPlans() {
    // TODO: Implement test
  }

  @Test
  void testGetRefurbPlans() {
    // TODO: Implement test
  }

  @Test
  void testChangeBatteryTesterPriority() {
    // TODO: Implement test
  }

  @Test
  void testChangeBatteryRefurbPriority() {
    // TODO: Implement test
  }

  @Test
  void testGetTesterStnInfo() {
    // TODO: Implement test
  }

  @Test
  void testGetRefurbStnInfo() {
    // TODO: Implement test
  }

  @Test
  void testRemoveLabBattery() {
    // TODO: Implement test
  }
}

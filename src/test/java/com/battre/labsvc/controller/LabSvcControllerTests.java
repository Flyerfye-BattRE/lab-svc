package com.battre.labsvc.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.battre.labsvc.model.LabPlanType;
import com.battre.labsvc.model.RefurbPlanType;
import com.battre.labsvc.model.RefurbStationType;
import com.battre.labsvc.model.TesterBacklogType;
import com.battre.labsvc.model.TesterStationType;
import com.battre.labsvc.service.LabSvc;
import com.battre.stubs.services.ChangeBatteryRefurbPriorityRequest;
import com.battre.stubs.services.ChangeBatteryRefurbPriorityResponse;
import com.battre.stubs.services.ChangeBatteryTesterPriorityRequest;
import com.battre.stubs.services.ChangeBatteryTesterPriorityResponse;
import com.battre.stubs.services.GetLabPlanStatusCountsRequest;
import com.battre.stubs.services.GetLabPlanStatusCountsResponse;
import com.battre.stubs.services.GetLabPlansRequest;
import com.battre.stubs.services.GetLabPlansResponse;
import com.battre.stubs.services.GetRefurbPlansRequest;
import com.battre.stubs.services.GetRefurbPlansResponse;
import com.battre.stubs.services.GetRefurbStnInfoRequest;
import com.battre.stubs.services.GetRefurbStnInfoResponse;
import com.battre.stubs.services.GetTesterBacklogRequest;
import com.battre.stubs.services.GetTesterBacklogResponse;
import com.battre.stubs.services.GetTesterStnInfoRequest;
import com.battre.stubs.services.GetTesterStnInfoResponse;
import com.battre.stubs.services.LabPlanStatus;
import com.battre.stubs.services.LabPlanStatusCount;
import com.battre.stubs.services.ProcessLabBatteriesRequest;
import com.battre.stubs.services.ProcessLabBatteriesResponse;
import com.battre.stubs.services.RemoveLabBatteryRequest;
import com.battre.stubs.services.RemoveLabBatteryResponse;
import io.grpc.stub.StreamObserver;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LabSvcControllerTests {
  @Mock private LabSvc labSvc;

  @Mock private StreamObserver<ProcessLabBatteriesResponse> responseProcessLabBatteriesResponse;
  @Mock private StreamObserver<GetLabPlansResponse> responseGetLabPlansResponse;
  @Mock private StreamObserver<GetTesterBacklogResponse> responseGetTesterBacklogResponse;
  @Mock private StreamObserver<GetRefurbPlansResponse> responseGetRefurbPlansResponse;
  @Mock private StreamObserver<ChangeBatteryTesterPriorityResponse> responseChangeBatteryTesterPriorityResponse;
  @Mock private StreamObserver<ChangeBatteryRefurbPriorityResponse> responseChangeBatteryRefurbPriorityResponse;
  @Mock private StreamObserver<GetTesterStnInfoResponse> responseGetTesterStnInfoResponse;
  @Mock private StreamObserver<GetRefurbStnInfoResponse> responseGetRefurbStnInfoResponse;
  @Mock private StreamObserver<RemoveLabBatteryResponse> responseRemoveLabBatteryResponse;
  @Mock private StreamObserver<GetLabPlanStatusCountsResponse> responseGetLabPlanStatusCountsResponse;

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
    
    List<LabPlanType> labPlans = List.of(
            new LabPlanType(1),
            new LabPlanType(2)
    );
    when(labSvc.getLabPlans()).thenReturn(labPlans);
    GetLabPlansRequest request = GetLabPlansRequest.newBuilder().build();

    labSvcController.getLabPlans(request, responseGetLabPlansResponse);

    verify(labSvc).getLabPlans();
    verify(responseGetLabPlansResponse).onNext(any(GetLabPlansResponse.class));
    verify(responseGetLabPlansResponse).onCompleted();
  }

  @Test
  void testGetCurrentLabPlans() {
    
    List<LabPlanType> labPlans = List.of(
            new LabPlanType(1),
            new LabPlanType(2)
    );
    when(labSvc.getCurrentLabPlans()).thenReturn(labPlans);
    GetLabPlansRequest request = GetLabPlansRequest.newBuilder().build();

    labSvcController.getCurrentLabPlans(request, responseGetLabPlansResponse);

    verify(labSvc).getCurrentLabPlans();
    verify(responseGetLabPlansResponse).onNext(any(GetLabPlansResponse.class));
    verify(responseGetLabPlansResponse).onCompleted();
  }

  @Test
  void testGetCurrentTesterBacklog() {
    TesterBacklogType testTesterBacklogType = new TesterBacklogType(1, 1, 1);
    testTesterBacklogType.setTesterBacklogId(3);
    
    List<TesterBacklogType> testerBacklog = List.of(
            testTesterBacklogType
    );
    when(labSvc.getCurrentTesterBacklog()).thenReturn(testerBacklog);
    GetTesterBacklogRequest request = GetTesterBacklogRequest.newBuilder().build();

    labSvcController.getCurrentTesterBacklog(request, responseGetTesterBacklogResponse);

    verify(labSvc).getCurrentTesterBacklog();
    verify(responseGetTesterBacklogResponse).onNext(any(GetTesterBacklogResponse.class));
    verify(responseGetTesterBacklogResponse).onCompleted();
  }

  @Test
  void testGetTesterBacklog() {
    TesterBacklogType testTesterBacklogType = new TesterBacklogType(1, 1, 1);
    testTesterBacklogType.setTesterBacklogId(2);
    
    List<TesterBacklogType> testerBacklog = List.of(
            testTesterBacklogType
    );
    when(labSvc.getTesterBacklog()).thenReturn(testerBacklog);
    GetTesterBacklogRequest request = GetTesterBacklogRequest.newBuilder().build();

    labSvcController.getTesterBacklog(request, responseGetTesterBacklogResponse);

    verify(labSvc).getTesterBacklog();
    verify(responseGetTesterBacklogResponse).onNext(any(GetTesterBacklogResponse.class));
    verify(responseGetTesterBacklogResponse).onCompleted();
  }

  @Test
  void testGetCurrentRefurbPlans() {
    RefurbPlanType testRefurbPlanType = new RefurbPlanType(1, true, true, true, true);
    testRefurbPlanType.setRefurbPlanId(2);

    List<RefurbPlanType> refurbPlans = List.of(
            testRefurbPlanType
    );
    when(labSvc.getCurrentRefurbPlans()).thenReturn(refurbPlans);
    GetRefurbPlansRequest request = GetRefurbPlansRequest.newBuilder().build();

    labSvcController.getCurrentRefurbPlans(request, responseGetRefurbPlansResponse);

    verify(labSvc).getCurrentRefurbPlans();
    verify(responseGetRefurbPlansResponse).onNext(any(GetRefurbPlansResponse.class));
    verify(responseGetRefurbPlansResponse).onCompleted();
  }

  @Test
  void testGetRefurbPlans() {
    RefurbPlanType testRefurbPlanType = new RefurbPlanType(1, true, true, true, true);
    testRefurbPlanType.setRefurbPlanId(2);
    
    List<RefurbPlanType> refurbPlans = List.of(
            testRefurbPlanType
    );
    when(labSvc.getRefurbPlans()).thenReturn(refurbPlans);
    GetRefurbPlansRequest request = GetRefurbPlansRequest.newBuilder().build();

    labSvcController.getRefurbPlans(request, responseGetRefurbPlansResponse);

    verify(labSvc).getRefurbPlans();
    verify(responseGetRefurbPlansResponse).onNext(any(GetRefurbPlansResponse.class));
    verify(responseGetRefurbPlansResponse).onCompleted();
  }

  @Test
  void testChangeBatteryTesterPriority() {
    
    when(labSvc.changeBatteryTesterPriority(any(Integer.class), any(Integer.class))).thenReturn(true);
    ChangeBatteryTesterPriorityRequest request = ChangeBatteryTesterPriorityRequest.newBuilder()
            .setBatteryId(1)
            .setPriority(2)
            .build();

    labSvcController.changeBatteryTesterPriority(request, responseChangeBatteryTesterPriorityResponse);

    verify(labSvc).changeBatteryTesterPriority(1, 2);
    verify(responseChangeBatteryTesterPriorityResponse).onNext(any(ChangeBatteryTesterPriorityResponse.class));
    verify(responseChangeBatteryTesterPriorityResponse).onCompleted();
  }

  @Test
  void testChangeBatteryRefurbPriority() {
    
    when(labSvc.changeBatteryRefurbPriority(any(Integer.class), any(Integer.class))).thenReturn(true);
    ChangeBatteryRefurbPriorityRequest request = ChangeBatteryRefurbPriorityRequest.newBuilder()
            .setBatteryId(1)
            .setPriority(2)
            .build();

    labSvcController.changeBatteryRefurbPriority(request, responseChangeBatteryRefurbPriorityResponse);

    verify(labSvc).changeBatteryRefurbPriority(1, 2);
    verify(responseChangeBatteryRefurbPriorityResponse).onNext(any(ChangeBatteryRefurbPriorityResponse.class));
    verify(responseChangeBatteryRefurbPriorityResponse).onCompleted();
  }

  @Test
  void testGetTesterStnInfo() {
    TesterStationType testTesterStationType = new TesterStationType(1, 1);
    testTesterStationType.setLastActiveDate();
    testTesterStationType.setLastCalibrationDate();
    testTesterStationType.setNextCalibrationDate(Timestamp.from(Instant.now()));

    List<TesterStationType> testerStations = List.of(
            testTesterStationType
    );
    when(labSvc.getTesterStationLogs()).thenReturn(testerStations);
    GetTesterStnInfoRequest request = GetTesterStnInfoRequest.newBuilder().build();

    labSvcController.getTesterStnInfo(request, responseGetTesterStnInfoResponse);
    
    verify(labSvc).getTesterStationLogs();
    verify(responseGetTesterStnInfoResponse).onNext(any(GetTesterStnInfoResponse.class));
    verify(responseGetTesterStnInfoResponse).onCompleted();
  }

  @Test
  void testGetRefurbStnInfo() {
    RefurbStationType testRefurbStationType = new RefurbStationType(1, 1);
    testRefurbStationType.setLastActiveDate();
    testRefurbStationType.setLastCalibrationDate();
    testRefurbStationType.setNextCalibrationDate(Timestamp.from(Instant.now()));

    List<RefurbStationType> refurbStations = List.of(
            testRefurbStationType
    );
    when(labSvc.getRefurbStationLogs()).thenReturn(refurbStations);
    GetRefurbStnInfoRequest request = GetRefurbStnInfoRequest.newBuilder().build();

    labSvcController.getRefurbStnInfo(request, responseGetRefurbStnInfoResponse);
    
    verify(labSvc).getRefurbStationLogs();
    verify(responseGetRefurbStnInfoResponse).onNext(any(GetRefurbStnInfoResponse.class));
    verify(responseGetRefurbStnInfoResponse).onCompleted();
  }

  @Test
  void testRemoveBattery() {
    
    when(labSvc.removeBattery(any(Integer.class))).thenReturn(true);
    RemoveLabBatteryRequest request = RemoveLabBatteryRequest.newBuilder()
            .setBatteryId(1)
            .build();

    labSvcController.removeLabBattery(request, responseRemoveLabBatteryResponse);

    verify(labSvc).removeBattery(1);
    verify(responseRemoveLabBatteryResponse).onNext(any(RemoveLabBatteryResponse.class));
    verify(responseRemoveLabBatteryResponse).onCompleted();
  }

  @Test
  void testGetLabPlanStatusCounts() {
    
    List<LabPlanStatusCount> labPlanStatusCounts =
        List.of(
            LabPlanStatusCount.newBuilder().setLabPlanStatus(LabPlanStatus.TESTER_BACKLOG_NEW).setCount(3).build());
    when(labSvc.getLabPlanStatusCounts()).thenReturn(labPlanStatusCounts);
    GetLabPlanStatusCountsRequest request = GetLabPlanStatusCountsRequest.newBuilder().build();

    labSvcController.getLabPlanStatusCounts(request, responseGetLabPlanStatusCountsResponse);

    verify(labSvc).getLabPlanStatusCounts();
    verify(responseGetLabPlanStatusCountsResponse).onNext(any(GetLabPlanStatusCountsResponse.class));
    verify(responseGetLabPlanStatusCountsResponse).onCompleted();
  }

}

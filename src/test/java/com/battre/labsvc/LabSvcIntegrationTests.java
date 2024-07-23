package com.battre.labsvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.battre.grpcifc.GrpcMethodInvoker;
import com.battre.grpcifc.GrpcTestMethodInvoker;
import com.battre.labsvc.controller.LabSvcController;
import com.battre.labsvc.model.LabPlanType;
import com.battre.labsvc.model.RefurbPlanType;
import com.battre.labsvc.model.RefurbStationType;
import com.battre.labsvc.model.TesterBacklogType;
import com.battre.labsvc.model.TesterStationType;
import com.battre.labsvc.repository.LabPlansRepository;
import com.battre.labsvc.repository.RefurbPlanRepository;
import com.battre.labsvc.repository.RefurbStationRepository;
import com.battre.labsvc.repository.TesterBacklogRepository;
import com.battre.labsvc.repository.TesterStationRepository;
import com.battre.stubs.services.BatteryIdType;
import com.battre.stubs.services.BatteryTypeTerminalPair;
import com.battre.stubs.services.ChangeBatteryRefurbPriorityRequest;
import com.battre.stubs.services.ChangeBatteryRefurbPriorityResponse;
import com.battre.stubs.services.ChangeBatteryTesterPriorityRequest;
import com.battre.stubs.services.ChangeBatteryTesterPriorityResponse;
import com.battre.stubs.services.GetBatteryTerminalLayoutsRequest;
import com.battre.stubs.services.GetBatteryTerminalLayoutsResponse;
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
import com.battre.stubs.services.ProcessLabBatteriesRequest;
import com.battre.stubs.services.ProcessLabBatteriesResponse;
import com.battre.stubs.services.RemoveLabBatteryRequest;
import com.battre.stubs.services.RemoveLabBatteryResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = "grpc.server.port=9043")
@ExtendWith(MockitoExtension.class)
public class LabSvcIntegrationTests {
  private static final Logger logger = Logger.getLogger(LabSvcIntegrationTests.class.getName());

  @MockBean private LabPlansRepository labPlansRepo;
  @MockBean private TesterBacklogRepository testerBacklogRepo;
  @MockBean private TesterStationRepository testerStnRepo;
  @MockBean private RefurbPlanRepository refurbPlanRepo;
  @MockBean private RefurbStationRepository refurbStnRepo;
  @MockBean private GrpcMethodInvoker grpcMethodInvoker;
  @Autowired private LabSvcController labSvcController;
  private final GrpcTestMethodInvoker grpcTestMethodInvoker = new GrpcTestMethodInvoker();

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testProcessLabBatteries_Success() throws NoSuchMethodException {
    // Test
    when(labPlansRepo.save(any(LabPlanType.class))).thenReturn(new LabPlanType());
    when(testerBacklogRepo.save(any(TesterBacklogType.class))).thenReturn(new TesterBacklogType());

    GetBatteryTerminalLayoutsResponse tryGetBatteryTerminalLayoutsResponse =
        GetBatteryTerminalLayoutsResponse.newBuilder()
            .addBatteries(
                BatteryTypeTerminalPair.newBuilder()
                    .setBatteryTypeId(1)
                    .setBatteryTerminalLayoutId(2)
                    .build())
            .build();
    when(grpcMethodInvoker.invokeNonblock(
            eq("specsvc"),
            eq("getBatteryTerminalLayouts"),
            any(GetBatteryTerminalLayoutsRequest.class)))
        .thenReturn(tryGetBatteryTerminalLayoutsResponse);

    // Request
    ProcessLabBatteriesRequest request =
        ProcessLabBatteriesRequest.newBuilder()
            .addBatteryIdTypes(
                BatteryIdType.newBuilder().setBatteryId(1).setBatteryTypeId(1).build())
            .build();
    ProcessLabBatteriesResponse response =
        grpcTestMethodInvoker.invokeNonblock(labSvcController, "processLabBatteries", request);
    assertTrue(response.getSuccess());

    // Verify
    verify(labPlansRepo).save(any(LabPlanType.class));
    verify(testerBacklogRepo).save(any(TesterBacklogType.class));
    verify(grpcMethodInvoker)
        .invokeNonblock(
            eq("specsvc"),
            eq("getBatteryTerminalLayouts"),
            any(GetBatteryTerminalLayoutsRequest.class));
  }

  @Test
  public void testGetLabPlans_Success() throws NoSuchMethodException {
    // Test
    List<LabPlanType> labPlans = List.of(new LabPlanType(5));
    when(labPlansRepo.getLabPlans()).thenReturn(labPlans);

    // Request
    GetLabPlansRequest request = GetLabPlansRequest.newBuilder().build();
    GetLabPlansResponse response =
        grpcTestMethodInvoker.invokeNonblock(labSvcController, "getLabPlans", request);
    assertEquals(response.getLabPlanListCount(), 1);
    assertEquals(response.getLabPlanList(0).getBatteryId(), 5);

    // Verify
    verify(labPlansRepo).getLabPlans();
  }

  @Test
  public void testGetCurrentLabPlans_Success() throws NoSuchMethodException {
    // Test
    List<LabPlanType> labPlans = List.of(new LabPlanType(6));
    when(labPlansRepo.getCurrentLabPlans()).thenReturn(labPlans);

    // Request
    GetLabPlansRequest request = GetLabPlansRequest.newBuilder().build();
    GetLabPlansResponse response =
        grpcTestMethodInvoker.invokeNonblock(labSvcController, "getCurrentLabPlans", request);
    assertEquals(response.getLabPlanListCount(), 1);
    assertEquals(response.getLabPlanList(0).getBatteryId(), 6);

    // Verify
    verify(labPlansRepo).getCurrentLabPlans();
  }

  @Test
  public void testGetCurrentTesterBacklog_Success() throws NoSuchMethodException {
    // Test
    TesterBacklogType testerBacklogType = new TesterBacklogType(3, 4, 5);
    testerBacklogType.setTesterBacklogId(2);
    List<TesterBacklogType> backlogList = List.of(testerBacklogType);
    when(testerBacklogRepo.getCurrentTesterBacklog()).thenReturn(backlogList);

    // Request
    GetTesterBacklogRequest request = GetTesterBacklogRequest.newBuilder().build();
    GetTesterBacklogResponse response =
        grpcTestMethodInvoker.invokeNonblock(labSvcController, "getCurrentTesterBacklog", request);
    assertEquals(response.getTesterBacklogListCount(), 1);
    assertEquals(response.getTesterBacklogList(0).getBatteryId(), 3);

    // Verify
    verify(testerBacklogRepo).getCurrentTesterBacklog();
  }

  @Test
  public void testGetTesterBacklog_Success() throws NoSuchMethodException {
    // Test
    TesterBacklogType testerBacklogType = new TesterBacklogType(5, 6, 7);
    testerBacklogType.setTesterBacklogId(4);
    List<TesterBacklogType> backlogList = List.of(testerBacklogType);
    when(testerBacklogRepo.getTesterBacklog()).thenReturn(backlogList);

    // Request
    GetTesterBacklogRequest request = GetTesterBacklogRequest.newBuilder().build();
    GetTesterBacklogResponse response =
        grpcTestMethodInvoker.invokeNonblock(labSvcController, "getTesterBacklog", request);
    assertEquals(response.getTesterBacklogListCount(), 1);
    assertEquals(response.getTesterBacklogList(0).getBatteryId(), 5);

    // Verify
    verify(testerBacklogRepo).getTesterBacklog();
  }

  @Test
  public void testGetCurrentRefurbPlans_Success() throws NoSuchMethodException {
    // Test
    RefurbPlanType refurbPlanType = new RefurbPlanType(8, true, false, true, false);
    refurbPlanType.setRefurbPlanId(7);
    List<RefurbPlanType> refurbPlanList = List.of(refurbPlanType);
    when(refurbPlanRepo.getCurrentRefurbPlans()).thenReturn(refurbPlanList);

    // Request
    GetRefurbPlansRequest request = GetRefurbPlansRequest.newBuilder().build();
    GetRefurbPlansResponse response =
        grpcTestMethodInvoker.invokeNonblock(labSvcController, "getCurrentRefurbPlans", request);
    assertEquals(response.getRefurbPlanListCount(), 1);
    assertEquals(response.getRefurbPlanList(0).getBatteryId(), 8);
    assertTrue(response.getRefurbPlanList(0).getResolder());
    assertFalse(response.getRefurbPlanList(0).getRepack());
    assertTrue(response.getRefurbPlanList(0).getProcessorSwap());
    assertFalse(response.getRefurbPlanList(0).getCapacitorSwap());

    // Verify
    verify(refurbPlanRepo).getCurrentRefurbPlans();
  }

  @Test
  public void testGetRefurbPlans_Success() throws NoSuchMethodException {
    // Test
    RefurbPlanType refurbPlanType = new RefurbPlanType(9, false, true, false, true);
    refurbPlanType.setRefurbPlanId(8);
    List<RefurbPlanType> refurbPlanList = List.of(refurbPlanType);
    when(refurbPlanRepo.getRefurbPlans()).thenReturn(refurbPlanList);

    // Request
    GetRefurbPlansRequest request = GetRefurbPlansRequest.newBuilder().build();
    GetRefurbPlansResponse response =
        grpcTestMethodInvoker.invokeNonblock(labSvcController, "getRefurbPlans", request);
    assertEquals(response.getRefurbPlanListCount(), 1);
    assertEquals(response.getRefurbPlanList(0).getBatteryId(), 9);
    assertFalse(response.getRefurbPlanList(0).getResolder());
    assertTrue(response.getRefurbPlanList(0).getRepack());
    assertFalse(response.getRefurbPlanList(0).getProcessorSwap());
    assertTrue(response.getRefurbPlanList(0).getCapacitorSwap());

    // Verify
    verify(refurbPlanRepo).getRefurbPlans();
  }

  @Test
  public void testChangeBatteryTesterPriority_Success() throws NoSuchMethodException {
    // Test
    doNothing().when(testerBacklogRepo).setBatteryTesterPriority(2, 40);

    // Request
    ChangeBatteryTesterPriorityRequest request =
        ChangeBatteryTesterPriorityRequest.newBuilder().setBatteryId(2).setPriority(40).build();
    ChangeBatteryTesterPriorityResponse response =
        grpcTestMethodInvoker.invokeNonblock(
            labSvcController, "changeBatteryTesterPriority", request);
    assertTrue(response.getSuccess());

    // Verify
    verify(testerBacklogRepo)
        .setBatteryTesterPriority(request.getBatteryId(), request.getPriority());
  }

  @Test
  public void testChangeBatteryRefurbPriority_Success() throws NoSuchMethodException {
    // Test
    doNothing().when(refurbPlanRepo).setBatteryRefurbPriority(3, 60);

    // Request
    ChangeBatteryRefurbPriorityRequest request =
        ChangeBatteryRefurbPriorityRequest.newBuilder().setBatteryId(3).setPriority(60).build();
    ChangeBatteryRefurbPriorityResponse response =
        grpcTestMethodInvoker.invokeNonblock(
            labSvcController, "changeBatteryRefurbPriority", request);
    assertTrue(response.getSuccess());

    // Verify
    verify(refurbPlanRepo).setBatteryRefurbPriority(request.getBatteryId(), request.getPriority());
  }

  @Test
  public void testGetTesterStnInfo_Success() throws NoSuchMethodException {
    // Test
    TesterStationType testerStationType = new TesterStationType(1, 2);
    testerStationType.setLastActiveDate();
    testerStationType.setLastCalibrationDate();
    testerStationType.setNextCalibrationDate(Timestamp.from(Instant.now()));
    List<TesterStationType> testerStationList = List.of(testerStationType);
    when(testerStnRepo.getTesterStationLogs()).thenReturn(testerStationList);

    // Request
    GetTesterStnInfoRequest request = GetTesterStnInfoRequest.newBuilder().build();
    GetTesterStnInfoResponse response =
        grpcTestMethodInvoker.invokeNonblock(labSvcController, "getTesterStnInfo", request);
    assertEquals(response.getTesterStationListCount(), 1);
    assertEquals(response.getTesterStationList(0).getTesterStnId(), 1);
    assertEquals(response.getTesterStationList(0).getTerminalLayoutId(), 2);

    // Verify
    verify(testerStnRepo).getTesterStationLogs();
  }

  @Test
  public void testGetRefurbStnInfo_Success() throws NoSuchMethodException {
    // Test
    RefurbStationType refurbStationType = new RefurbStationType(2, 4);
    refurbStationType.setLastActiveDate();
    refurbStationType.setLastCalibrationDate();
    refurbStationType.setNextCalibrationDate(Timestamp.from(Instant.now()));
    List<RefurbStationType> refurbStationList = List.of(refurbStationType);
    when(refurbStnRepo.getRefurbStationLogs()).thenReturn(refurbStationList);

    // Request
    GetRefurbStnInfoRequest request = GetRefurbStnInfoRequest.newBuilder().build();
    GetRefurbStnInfoResponse response =
        grpcTestMethodInvoker.invokeNonblock(labSvcController, "getRefurbStnInfo", request);
    assertEquals(response.getRefurbStationListCount(), 1);
    assertEquals(response.getRefurbStationList(0).getRefurbStnId(), 2);
    assertEquals(response.getRefurbStationList(0).getRefurbStationClassValue(), 4);

    // Verify
    verify(refurbStnRepo).getRefurbStationLogs();
  }

  @Test
  public void testRemoveLabBattery_Success() throws NoSuchMethodException {
    // Test
    int labPlanId = 1;
    int batteryId = 4;
    LabPlanType labPlanType = new LabPlanType(batteryId);
    labPlanType.setLabPlanId(labPlanId);
    when(labPlansRepo.findByBatteryId(batteryId)).thenReturn(labPlanType);
    doNothing().when(labPlansRepo).endLabPlan(eq(labPlanId), any(Timestamp.class));
    doNothing().when(labPlansRepo).setPlanStatusesForPlanId(eq(labPlanId), eq("DESTROYED"));
    when(labPlansRepo.getLabPlanIdsForBatteryId(batteryId)).thenReturn(List.of());
    when(testerBacklogRepo.getCurrentTesterBacklogForBatteryId(batteryId))
        .thenReturn(Optional.of(5))
        .thenReturn(Optional.empty());
    doNothing().when(testerBacklogRepo).endTesterBacklogEntry(anyInt(), any(Timestamp.class));

    when(refurbPlanRepo.getPendingRefurbPlanForBatteryId(batteryId)).thenReturn(Optional.empty());

    // Request
    RemoveLabBatteryRequest request =
        RemoveLabBatteryRequest.newBuilder().setBatteryId(batteryId).build();
    RemoveLabBatteryResponse response =
        grpcTestMethodInvoker.invokeNonblock(labSvcController, "removeLabBattery", request);
    assertTrue(response.getSuccess());

    // Verify
    verify(labPlansRepo).findByBatteryId(eq(batteryId));
    verify(labPlansRepo).endLabPlan(eq(labPlanId), any(Timestamp.class));
    verify(labPlansRepo).setPlanStatusesForPlanId(eq(labPlanId), eq("DESTROYED"));
    verify(labPlansRepo).getLabPlanIdsForBatteryId(eq(batteryId));
    verify(testerBacklogRepo, times(2)).getCurrentTesterBacklogForBatteryId(eq(batteryId));
    verify(testerBacklogRepo).endTesterBacklogEntry(anyInt(), any(Timestamp.class));
    verify(refurbPlanRepo).getPendingRefurbPlanForBatteryId(eq(batteryId));
  }

  @Test
  public void testGetLabPlanStatusCounts_Success() throws NoSuchMethodException {
    // Test
    when(labPlansRepo.getLabPlanStatusCounts())
        .thenReturn(List.of(new Object[] {"PASS", 10L}, new Object[] {"DESTROYED", 20L}));

    // Request
    GetLabPlanStatusCountsRequest request = GetLabPlanStatusCountsRequest.newBuilder().build();
    GetLabPlanStatusCountsResponse response =
        grpcTestMethodInvoker.invokeNonblock(labSvcController, "getLabPlanStatusCounts", request);
    assertEquals(response.getLabPlanStatusCountListCount(), 2);
    assertEquals(response.getLabPlanStatusCountList(0).getLabPlanStatus(), LabPlanStatus.PASS);
    assertEquals(response.getLabPlanStatusCountList(0).getCount(), 10);

    // Verify
    verify(labPlansRepo).getLabPlanStatusCounts();
  }
}

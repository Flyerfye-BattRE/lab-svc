package com.battre.labsvc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.battre.grpcifc.GrpcMethodInvoker;
import com.battre.labsvc.enums.LabPlanStatusEnum;
import com.battre.labsvc.enums.RefurbStationClass;
import com.battre.labsvc.model.LabPlanType;
import com.battre.labsvc.model.RefurbPlanType;
import com.battre.labsvc.model.RefurbResultRecord;
import com.battre.labsvc.model.RefurbStationType;
import com.battre.labsvc.model.TesterBacklogType;
import com.battre.labsvc.model.TesterResultRecord;
import com.battre.labsvc.model.TesterStationType;
import com.battre.labsvc.repository.LabPlansRepository;
import com.battre.labsvc.repository.RefurbPlanRepository;
import com.battre.labsvc.repository.RefurbStationRepository;
import com.battre.labsvc.repository.TesterBacklogRepository;
import com.battre.labsvc.repository.TesterStationRepository;
import com.battre.stubs.services.BatteryIdType;
import com.battre.stubs.services.BatteryTypeTerminalPair;
import com.battre.stubs.services.GetBatteryTerminalLayoutsRequest;
import com.battre.stubs.services.GetBatteryTerminalLayoutsResponse;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LabSvcTests {
  @Mock private LabPlansRepository labPlansRepo;
  @Mock private TesterBacklogRepository testerBacklogRepo;
  @Mock private TesterStationRepository testerStnRepo;
  @Mock private RefurbPlanRepository refurbPlanRepo;
  @Mock private RefurbStationRepository refurbStnRepo;
  @Mock private GrpcMethodInvoker grpcMethodInvoker;
  @Mock private BlockingQueue<TesterResultRecord> testResultQueue;
  @Mock private BlockingQueue<RefurbResultRecord> refurbResultQueue;
  private LabSvc labSvc;
  private AutoCloseable closeable;

  public void mockGetBatteryTerminalLayouts(GetBatteryTerminalLayoutsResponse response) {
    when(grpcMethodInvoker.invokeNonblock(
            eq("specsvc"),
            eq("getBatteryTerminalLayouts"),
            any(GetBatteryTerminalLayoutsRequest.class)))
        .thenReturn(response);
  }

  @BeforeEach
  public void openMocks() {
    closeable = MockitoAnnotations.openMocks(this);
    testResultQueue = new LinkedBlockingQueue<>();
    refurbResultQueue = new LinkedBlockingQueue<>();

    labSvc =
        new LabSvc(
            labPlansRepo,
            testerBacklogRepo,
            testerStnRepo,
            refurbPlanRepo,
            refurbStnRepo,
            grpcMethodInvoker,
            testResultQueue,
            refurbResultQueue);
  }

  @AfterEach
  public void releaseMocks() throws Exception {
    closeable.close();
  }

  @Test
  public void testAddBatteriesToLabPlans() {
    List<BatteryIdType> batteryIdsTypes =
        List.of(
            BatteryIdType.newBuilder().setBatteryId(1).setBatteryTypeId(3).build(),
            BatteryIdType.newBuilder().setBatteryId(2).setBatteryTypeId(4).build(),
            BatteryIdType.newBuilder().setBatteryId(3).setBatteryTypeId(3).build());

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
    List<BatteryIdType> batteryIdsTypes =
        List.of(
            BatteryIdType.newBuilder().setBatteryId(4).setBatteryTypeId(2).build(),
            BatteryIdType.newBuilder().setBatteryId(5).setBatteryTypeId(1).build(),
            BatteryIdType.newBuilder().setBatteryId(6).setBatteryTypeId(3).build(),
            BatteryIdType.newBuilder().setBatteryId(7).setBatteryTypeId(4).build());

    List<BatteryTypeTerminalPair> batteryTypeTerminalList =
        List.of(
            BatteryTypeTerminalPair.newBuilder()
                .setBatteryTypeId(1)
                .setBatteryTerminalLayoutId(4)
                .build(),
            BatteryTypeTerminalPair.newBuilder()
                .setBatteryTypeId(2)
                .setBatteryTerminalLayoutId(3)
                .build(),
            BatteryTypeTerminalPair.newBuilder()
                .setBatteryTypeId(3)
                .setBatteryTerminalLayoutId(5)
                .build(),
            BatteryTypeTerminalPair.newBuilder()
                .setBatteryTypeId(4)
                .setBatteryTerminalLayoutId(2)
                .build());
    GetBatteryTerminalLayoutsResponse response =
        GetBatteryTerminalLayoutsResponse.newBuilder()
            .addAllBatteries(batteryTypeTerminalList)
            .build();
    mockGetBatteryTerminalLayouts(response);

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

  @Test
  void testGetLabPlans() {
    List<LabPlanType> labPlans = List.of(
            new LabPlanType(1),
            new LabPlanType(2)
    );
    when(labPlansRepo.getLabPlans()).thenReturn(labPlans);

    List<LabPlanType> result = labSvc.getLabPlans();

    assertEquals(2, result.size());
    assertEquals(1, result.get(0).getBatteryId());
    assertEquals(2, result.get(1).getBatteryId());
    verify(labPlansRepo).getLabPlans();
  }

  @Test
  void testGetCurrentLabPlans() {
    List<LabPlanType> currentLabPlans = List.of(
            new LabPlanType(3),
            new LabPlanType(4)
    );
    when(labPlansRepo.getCurrentLabPlans()).thenReturn(currentLabPlans);

    List<LabPlanType> result = labSvc.getCurrentLabPlans();

    assertEquals(2, result.size());
    assertEquals(3, result.get(0).getBatteryId());
    assertEquals(4, result.get(1).getBatteryId());
    verify(labPlansRepo).getCurrentLabPlans();
  }

  @Test
  void testGetCurrentTesterBacklog() {
    List<TesterBacklogType> currentTesterBacklog = List.of(
            new TesterBacklogType(5, 1, 2),
            new TesterBacklogType(6, 2, 3)
    );
    when(testerBacklogRepo.getCurrentTesterBacklog()).thenReturn(currentTesterBacklog);

    List<TesterBacklogType> result = labSvc.getCurrentTesterBacklog();

    assertEquals(2, result.size());
    assertEquals(5, result.get(0).getBatteryId());
    assertEquals(6, result.get(1).getBatteryId());
    verify(testerBacklogRepo).getCurrentTesterBacklog();
  }

  @Test
  void testGetTesterBacklog() {
    List<TesterBacklogType> testerBacklog = List.of(
            new TesterBacklogType(7, 3, 4),
            new TesterBacklogType(8, 4, 5)
    );
    when(testerBacklogRepo.getTesterBacklog()).thenReturn(testerBacklog);

    List<TesterBacklogType> result = labSvc.getTesterBacklog();

    assertEquals(2, result.size());
    assertEquals(7, result.get(0).getBatteryId());
    assertEquals(8, result.get(1).getBatteryId());
    verify(testerBacklogRepo).getTesterBacklog();
  }

  @Test
  void testGetCurrentRefurbPlans() {
    List<RefurbPlanType> currentRefurbPlans = List.of(
            new RefurbPlanType(9, true, true, true, true),
            new RefurbPlanType(10, true, true, true, true)
    );
    when(refurbPlanRepo.getCurrentRefurbPlans()).thenReturn(currentRefurbPlans);

    List<RefurbPlanType> result = labSvc.getCurrentRefurbPlans();

    assertEquals(2, result.size());
    assertEquals(9, result.get(0).getBatteryId());
    assertEquals(10, result.get(1).getBatteryId());
    verify(refurbPlanRepo).getCurrentRefurbPlans();
  }

  @Test
  void testGetRefurbPlans() {
    List<RefurbPlanType> refurbPlans = List.of(
            new RefurbPlanType(11, true, true, true, true)
    );
    when(refurbPlanRepo.getRefurbPlans()).thenReturn(refurbPlans);

    List<RefurbPlanType> result = labSvc.getRefurbPlans();

    assertEquals(1, result.size());
    assertEquals(11, result.get(0).getBatteryId());
    verify(refurbPlanRepo).getRefurbPlans();
  }

  @Test
  void testChangeBatteryTesterPriority() {
    boolean result = labSvc.changeBatteryTesterPriority(13, 2);

    assertTrue(result);
    verify(testerBacklogRepo).setBatteryTesterPriority(13, 2);
  }

  @Test
  void testChangeBatteryRefurbPriority() {
    boolean result = labSvc.changeBatteryRefurbPriority(14, 3);

    assertTrue(result);
    verify(refurbPlanRepo).setBatteryRefurbPriority(14, 3);
  }


  @Test
  void testGetTesterStnInfo() {
    TesterStationType testTesterStationType = new TesterStationType(15, 1);
    List<TesterStationType> testerStationLogs = List.of(
            testTesterStationType
    );
    when(testerStnRepo.getTesterStationLogs()).thenReturn(testerStationLogs);

    List<TesterStationType> result = labSvc.getTesterStationLogs();

    assertEquals(1, result.size());
    assertEquals(15, result.get(0).getTesterStnId());
    verify(testerStnRepo).getTesterStationLogs();
  }

  @Test
  void testGetRefurbStnInfo() {
    List<RefurbStationType> refurbStationLogs = List.of(
            new RefurbStationType(17, 1),
            new RefurbStationType(18, 2)
    );
    when(refurbStnRepo.getRefurbStationLogs()).thenReturn(refurbStationLogs);

    List<RefurbStationType> result = labSvc.getRefurbStationLogs();

    assertEquals(2, result.size());
    assertEquals(17, result.get(0).getRefurbStnId());
    assertEquals(18, result.get(1).getRefurbStnId());
    verify(refurbStnRepo).getRefurbStationLogs();
  }

  @Test
  void testRemoveLabBattery() {
    TesterResultRecord testerRecord = new TesterResultRecord(1, 19, 1, 1, 1, 1, Timestamp.from(Instant.now()));
    RefurbResultRecord refurbRecord = new RefurbResultRecord(2, 2, RefurbStationClass.NO_REFURB, 19, 2, Timestamp.from(Instant.now()));
    testResultQueue.add(testerRecord);
    refurbResultQueue.add(refurbRecord);

    when(labPlansRepo.findByBatteryId(19)).thenReturn(new LabPlanType(19));
    when(labPlansRepo.getLabPlanIdsForBatteryId(19)).thenReturn(List.of());
    when(testerBacklogRepo.getCurrentTesterBacklogForBatteryId(19)).thenReturn(Optional.of(1));
    when(testerBacklogRepo.getCurrentTesterBacklogForBatteryId(19)).thenReturn(Optional.empty());
    when(refurbPlanRepo.getPendingRefurbPlanForBatteryId(19)).thenReturn(Optional.of(1));
    when(refurbPlanRepo.getPendingRefurbPlanForBatteryId(19)).thenReturn(Optional.empty());

    boolean result = labSvc.removeBattery(19);

    assertTrue(result);
    verify(labPlansRepo).endLabPlan(anyInt(), any(Timestamp.class));
    verify(labPlansRepo).setPlanStatusesForPlanId(anyInt(), eq(LabPlanStatusEnum.DESTROYED.toString()));
  }
}

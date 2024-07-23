package com.battre.labsvc.service;

import com.battre.grpcifc.GrpcMethodInvoker;
import com.battre.labsvc.enums.LabPlanStatusEnum;
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
import com.battre.stubs.services.LabPlanStatusCount;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LabSvc {
  private static final Logger logger = Logger.getLogger(LabSvc.class.getName());

  private final LabPlansRepository labPlansRepo;

  private final TesterBacklogRepository testerBacklogRepo;
  private final TesterStationRepository testerStnRepo;
  private final RefurbPlanRepository refurbPlanRepo;
  private final RefurbStationRepository refurbStnRepo;
  private final GrpcMethodInvoker grpcMethodInvoker;
  private final BlockingQueue<TesterResultRecord> testResultQueue;
  private final BlockingQueue<RefurbResultRecord> refurbResultQueue;

  @Autowired
  LabSvc(
      LabPlansRepository labPlansRepo,
      TesterBacklogRepository testerBacklogRepo,
      TesterStationRepository testerStnRepo,
      RefurbPlanRepository refurbPlanRepo,
      RefurbStationRepository refurbStnRepo,
      GrpcMethodInvoker grpcMethodInvoker,
      BlockingQueue<TesterResultRecord> testResultQueue,
      BlockingQueue<RefurbResultRecord> refurbResultQueue) {
    this.labPlansRepo = labPlansRepo;
    this.testerBacklogRepo = testerBacklogRepo;
    this.testerStnRepo = testerStnRepo;
    this.refurbPlanRepo = refurbPlanRepo;
    this.refurbStnRepo = refurbStnRepo;
    this.grpcMethodInvoker = grpcMethodInvoker;
    this.testResultQueue = testResultQueue;
    this.refurbResultQueue = refurbResultQueue;
  }

  public boolean addBatteriesToLabPlans(List<BatteryIdType> batteryIdsTypes) {
    // create new lab plan records for all the batteries
    for (BatteryIdType batteryInfo : batteryIdsTypes) {
      LabPlanType labPlan = new LabPlanType(batteryInfo.getBatteryId());

      labPlansRepo.save(labPlan);
    }

    return true;
  }

  public boolean addBatteriesToTesterBacklog(List<BatteryIdType> batteryIdsTypes) {
    // query spec svc for terminal_layout_ids per batteryId
    Set<Integer> batteryTypesSet = new HashSet<>();

    // de-duplicate battery types before querying for corresponding terminal ids
    for (BatteryIdType batteryInfo : batteryIdsTypes) {
      batteryTypesSet.add(batteryInfo.getBatteryTypeId());
    }

    Map<Integer, Integer> batteryTypeToTerminalIds =
        getBatteryTerminalIdMap(batteryTypesSet.stream().toList());

    if (batteryTypeToTerminalIds.size() != batteryTypesSet.size()) {
      logger.severe(
          "Could not obtain terminal type mapping ["
              + batteryTypeToTerminalIds.size()
              + "] for all battery types ["
              + batteryTypesSet.size()
              + "] specified");

      return false;
    }

    for (BatteryIdType batteryInfo : batteryIdsTypes) {
      TesterBacklogType testerBacklogEntry =
          new TesterBacklogType(
              batteryInfo.getBatteryId(),
              // TODO: Modify test scheme ID to be returned from SpecSvc.getBatteryTerminalIdMap
              // gRPC call
              // batteryInfo.getTestSchemeId(),
              1,
              batteryTypeToTerminalIds.get(batteryInfo.getBatteryTypeId()));

      testerBacklogRepo.save(testerBacklogEntry);
    }

    return true;
  }

  public List<LabPlanType> getCurrentLabPlans() {
    return labPlansRepo.getCurrentLabPlans();
  }

  public List<LabPlanType> getLabPlans() {
    return labPlansRepo.getLabPlans();
  }

  public boolean changeBatteryTesterPriority(int batteryId, int priority) {
    testerBacklogRepo.setBatteryTesterPriority(batteryId, priority);

    return true;
  }

  public List<TesterBacklogType> getCurrentTesterBacklog() {
    return testerBacklogRepo.getCurrentTesterBacklog();
  }

  public List<TesterBacklogType> getTesterBacklog() {
    return testerBacklogRepo.getTesterBacklog();
  }

  public boolean changeBatteryRefurbPriority(int batteryId, int priority) {
    refurbPlanRepo.setBatteryRefurbPriority(batteryId, priority);

    return true;
  }

  public List<RefurbPlanType> getCurrentRefurbPlans() {
    return refurbPlanRepo.getCurrentRefurbPlans();
  }

  public List<RefurbPlanType> getRefurbPlans() {
    return refurbPlanRepo.getRefurbPlans();
  }

  public List<TesterStationType> getTesterStationLogs() {
    return testerStnRepo.getTesterStationLogs();
  }

  public List<RefurbStationType> getRefurbStationLogs() {
    return refurbStnRepo.getRefurbStationLogs();
  }

  public boolean removeBattery(int batteryId) {
    /*
       A battery could be in:
           TesterBacklog
               Set testerBacklogEndDate
           TesterResultQueue
               Delete entry
           RefurbBacklog
               Set refurb plan end date
               Set available = false
           RefurbResultQueue
               Delete entry

       End lab plan
       Find whichever list it is in, remove the entry from that list
    */

    int labPlanId = labPlansRepo.findByBatteryId(batteryId).getLabPlanId();
    labPlansRepo.endLabPlan(labPlanId, Timestamp.from(Instant.now()));
    labPlansRepo.setPlanStatusesForPlanId(labPlanId, LabPlanStatusEnum.DESTROYED.toString());
    boolean labPlanStatus = labPlansRepo.getLabPlanIdsForBatteryId(batteryId).isEmpty();

    boolean testBacklogStatus = removeTesterBacklogEntryWithBatteryId(batteryId);
    boolean testResultsStatus = removeTesterResultsWithBatteryId(batteryId);
    boolean refurbBacklogStatus = removeRefurbBacklogEntryWithBatteryId(batteryId);
    boolean refurbResultsStatus = removeRefurbResultsWithBatteryId(batteryId);

    logger.info("For Battery [" + batteryId + "] labPlanStatus " + labPlanStatus);
    logger.info("testBacklogStatus " + testBacklogStatus);
    logger.info("testResultsStatus " + testResultsStatus);
    logger.info("refurbBacklogStatus " + refurbBacklogStatus);
    logger.info("refurbResultsStatus " + refurbResultsStatus);

    // The relevant lab plans should be ended and at least one of the 4 places where results are
    return labPlanStatus
        && (testBacklogStatus || testResultsStatus || refurbBacklogStatus || refurbResultsStatus);
  }

  private boolean removeTesterBacklogEntryWithBatteryId(int batteryId) {
    Optional<Integer> testerBacklogEntry =
        testerBacklogRepo.getCurrentTesterBacklogForBatteryId(batteryId);
    if (testerBacklogEntry.isPresent()) {
      logger.info("testerBacklogEntry present");
      testerBacklogRepo.endTesterBacklogEntry(
          testerBacklogEntry.get(), Timestamp.from(Instant.now()));

      // while this may look the same as the case where the batteryId isn't detected in the list,
      // this case is only hit when the repo operation fails which will result in a thrown exception
      return testerBacklogRepo.getCurrentTesterBacklogForBatteryId(batteryId).isEmpty();
    } else {
      return false;
    }
  }

  private boolean removeRefurbBacklogEntryWithBatteryId(int batteryId) {
    Optional<Integer> refurbPlanEntry = refurbPlanRepo.getPendingRefurbPlanForBatteryId(batteryId);
    if (refurbPlanEntry.isPresent()) {
      logger.info("refurbPlanEntry present");
      refurbPlanRepo.endRefurbPlanEntry(refurbPlanEntry.get(), Timestamp.from(Instant.now()));

      // while this may look the same as the case where the batteryId isn't detected in the list,
      // this case is only hit when the repo operation fails which will result in a thrown exception
      return refurbPlanRepo.getPendingRefurbPlanForBatteryId(batteryId).isEmpty();
    } else {
      return false;
    }
  }

  private boolean removeTesterResultsWithBatteryId(int batteryId) {
    boolean success = false;
    synchronized (testResultQueue) {
      Iterator<TesterResultRecord> iterator = testResultQueue.iterator();
      while (iterator.hasNext()) {
        TesterResultRecord record = iterator.next();
        if (record.batteryId() == batteryId) {
          iterator.remove();
          logger.info("Battery " + batteryId + " successfully removed from tester results queue.");
          success = true;
        }
      }
    }

    return success;
  }

  private boolean removeRefurbResultsWithBatteryId(int batteryId) {
    boolean success = false;
    synchronized (refurbResultQueue) {
      Iterator<RefurbResultRecord> iterator = refurbResultQueue.iterator();
      while (iterator.hasNext()) {
        RefurbResultRecord record = iterator.next();
        if (record.batteryId() == batteryId) {
          iterator.remove();
          logger.info("Battery " + batteryId + " successfully removed from refurb results queue.");
          success = true;
        }
      }
    }

    return success;
  }

  private Map<Integer, Integer> getBatteryTerminalIdMap(List<Integer> batteryTypeIds) {
    GetBatteryTerminalLayoutsRequest request =
        GetBatteryTerminalLayoutsRequest.newBuilder().addAllBatteryTypeIds(batteryTypeIds).build();

    GetBatteryTerminalLayoutsResponse response =
        grpcMethodInvoker.invokeNonblock("specsvc", "getBatteryTerminalLayouts", request);

    Map<Integer, Integer> batteryTypeToTerminalIds =
        convertToBatteryTypeTerminalMap(response.getBatteriesList());

    return batteryTypeToTerminalIds;
  }

  private Map<Integer, Integer> convertToBatteryTypeTerminalMap(
      List<BatteryTypeTerminalPair> batteryTypeTerminalList) {
    return batteryTypeTerminalList.stream()
        .collect(
            Collectors.toMap(
                batteryTypeTerminal -> batteryTypeTerminal.getBatteryTypeId(),
                batteryTypeTerminal -> batteryTypeTerminal.getBatteryTerminalLayoutId()));
  }

  public List<LabPlanStatusCount> getLabPlanStatusCounts() {
    List<Object[]> labPlanStatusCountsList = labPlansRepo.getLabPlanStatusCounts();

    return labPlanStatusCountsList.stream()
        .map(
            labPlanStatusCount ->
                LabPlanStatusCount.newBuilder()
                    .setLabPlanStatus(
                        LabPlanStatusEnum.fromStatusDescription((String) labPlanStatusCount[0])
                            .getGrpcStatus())
                    .setCount(((Long) labPlanStatusCount[1]).intValue())
                    .build())
        .collect(Collectors.toList());
  }
}

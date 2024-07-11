package com.battre.labsvc.service;

import com.battre.labsvc.enums.LabResult;
import com.battre.labsvc.enums.RefurbStationClass;
import com.battre.labsvc.model.RefurbResultRecord;
import com.battre.labsvc.repository.RefurbPlanRepository;
import com.battre.labsvc.repository.RefurbStationRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class RefurbBackgrounder implements Runnable {
  private static final Logger logger = Logger.getLogger(RefurbBackgrounder.class.getName());

  private final ApplicationContext context;
  private final RefurbPlanRepository refurbPlanRepo;
  private final RefurbStationRepository refurbStationsRepo;
  // Check every 5 seconds
  private final long checkInterval = 5000;
  private final Object lock = new Object();
  private final ExecutorService refurbThreadPool;
  private final BlockingQueue<RefurbResultRecord> refurbResultQueue;
  private volatile boolean active = true;

  @Autowired
  public RefurbBackgrounder(
      ApplicationContext context,
      RefurbPlanRepository refurbPlanRepo,
      RefurbStationRepository refurbStationsRepo,
      BlockingQueue<RefurbResultRecord> refurbResultQueue) {
    this.context = context;
    this.refurbPlanRepo = refurbPlanRepo;
    this.refurbStationsRepo = refurbStationsRepo;
    this.refurbResultQueue = refurbResultQueue;

    refurbThreadPool = Executors.newCachedThreadPool();
  }

  @Override
  public void run() {
    try {
      while (active) {
        synchronized (lock) {
          checkAndAllocateRefurb();
          lock.wait(checkInterval);
        }
      }
    } catch (InterruptedException e) {
      stop();
      Thread.currentThread().interrupt();
      System.err.println("Refurb backgrounder interrupted");
    } catch (Exception e) {
      System.err.println("Error in refurb backgrounder operation: " + e.getMessage());
    }
  }

  public void triggerBacklogCheck() {
    logger.info("Triggering refurb backlog check");
    synchronized (lock) {
      // Wake the waiting thread
      lock.notify();
    }
  }

  public void stop() {
    logger.info("Stopping refurb backgrounder thread");
    active = false;
    // Ensure the loop exits if it is waiting
    triggerBacklogCheck();

    refurbThreadPool.shutdown();
    try {
      if (!refurbThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
        refurbThreadPool.shutdownNow();
      }
    } catch (InterruptedException ie) {
      refurbThreadPool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  void checkAndAllocateRefurb() throws InterruptedException {
    //        logger.info("Running checkAndAllocateRefurb");
    List<Object[]> refurbPlans = refurbPlanRepo.getCurrentRefurbSchemeStatuses();
    Map<String, List<Integer>> availRefurbStns = getAvailableRefurbStnsGroupedByClass();

    for (Object[] refurbPlanEntry : refurbPlans) {
      int refurbPlanId = (Integer) refurbPlanEntry[0];
      int batteryId = (Integer) refurbPlanEntry[1];
      boolean needResolder = (Boolean) refurbPlanEntry[2];
      boolean needRepack = (Boolean) refurbPlanEntry[3];
      boolean needProcessorSwap = (Boolean) refurbPlanEntry[4];
      boolean needCapacitorSwap = (Boolean) refurbPlanEntry[5];

      logger.info(
          "Refurb plan ["
              + refurbPlanId
              + "] for battery "
              + batteryId
              + " looking for refurb stn");

      if (needResolder || needRepack || needProcessorSwap || needCapacitorSwap) {
        // check if necessary/handle each refurb action
        handleRefurbAction(
            refurbPlanId, batteryId, needResolder, RefurbStationClass.RESOLDER, availRefurbStns);
        handleRefurbAction(
            refurbPlanId, batteryId, needRepack, RefurbStationClass.REPACK, availRefurbStns);
        handleRefurbAction(
            refurbPlanId,
            batteryId,
            needProcessorSwap,
            RefurbStationClass.PROCESSOR_SWAP,
            availRefurbStns);
        handleRefurbAction(
            refurbPlanId,
            batteryId,
            needCapacitorSwap,
            RefurbStationClass.CAPACITOR_SWAP,
            availRefurbStns);
      } else {
        logger.info(
            "Refurb plan [" + refurbPlanId + "] for battery " + batteryId + " marked NO_REFURB");
        // if no refurb actions are necessary
        RefurbResultRecord result =
            new RefurbResultRecord(
                refurbPlanId,
                -1,
                RefurbStationClass.NO_REFURB,
                batteryId,
                LabResult.PASS.getStatusCode(),
                Timestamp.from(Instant.now()));

        refurbResultQueue.put(result);
      }
    }
  }

  private boolean handleRefurbAction(
      int refurbPlanId,
      int batteryId,
      boolean condition,
      RefurbStationClass stationClass,
      Map<String, List<Integer>> availRefurbStns) {
    if (condition && checkRefurbStnAvail(stationClass, availRefurbStns)) {
      int selectedRefurbStn = availRefurbStns.get(stationClass.toString()).get(0);
      logger.info("Backlog [" + refurbPlanId + "] tester " + selectedRefurbStn + " found");
      boolean sendSuccess =
          sendBatteryToRefurbStn(refurbPlanId, selectedRefurbStn, stationClass, batteryId);

      if (sendSuccess) {
        logger.info("Backlog [" + refurbPlanId + "] added to tester " + selectedRefurbStn);
        availRefurbStns.get(stationClass.toString()).remove(0);
      }
      return sendSuccess;
    }
    return false;
  }

  @Transactional
  private boolean sendBatteryToRefurbStn(
      int refurbPlanId, int refurbStnId, RefurbStationClass refurbStnClass, int batteryId) {
    refurbPlanRepo.markRefurbPlanBusy(refurbPlanId);
    //  Update status, Active Battery Id, Last used date
    refurbStationsRepo.markRefurbStnInUse(refurbStnId, batteryId, Timestamp.from(Instant.now()));

    //  Start a cached thread to perform the testing
    RefurbStationRepository rsrBean = context.getBean(RefurbStationRepository.class);
    RefurbRunnable tr =
        new RefurbRunnable(
            rsrBean, refurbResultQueue, refurbPlanId, refurbStnId, refurbStnClass, batteryId);
    refurbThreadPool.submit(tr);

    return true;
  }

  private Map<String, List<Integer>> getAvailableRefurbStnsGroupedByClass() {
    List<Object[]> results = refurbStationsRepo.getAvailableRefurbStns();
    return results.stream()
        .collect(
            Collectors.groupingBy(
                result -> (String) result[1], // terminal_layout_id
                Collectors.mapping(
                    result -> (Integer) result[0], // tester_stn_id
                    Collectors.toList())));
  }

  private boolean checkRefurbStnAvail(
      RefurbStationClass refurbStnClass, Map<String, List<Integer>> availRefurbStns) {
    return availRefurbStns.containsKey(refurbStnClass.toString())
        && !availRefurbStns.get(refurbStnClass.toString()).isEmpty();
  }
}

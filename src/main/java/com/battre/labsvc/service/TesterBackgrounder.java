package com.battre.labsvc.service;

import com.battre.labsvc.repository.TesterBacklogRepository;
import com.battre.labsvc.repository.TesterStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@Scope("prototype")
public class TesterBackgrounder implements Runnable {
    private static final Logger logger = Logger.getLogger(TesterBackgrounder.class.getName());

    private final ApplicationContext context;
    private final TesterBacklogRepository testerBacklogRepo;
    private final TesterStationRepository testerStationsRepo;
    // Check every 5 seconds
    private final long checkInterval = 5000;
    private final Object lock = new Object();
    private final ExecutorService testerThreadPool;
    private final ExecutorService resultProcessorThread;
    private final BlockingQueue<TesterResultRecord> testerResultQueue;
    private volatile boolean active = true;

    @Autowired
    public TesterBackgrounder(
            ApplicationContext context,
            TesterBacklogRepository testerBacklogRepo,
            TesterStationRepository testerStationsRepo,
            BlockingQueue<TesterResultRecord> testerResultQueue
    ) {
        this.context = context;
        this.testerBacklogRepo = testerBacklogRepo;
        this.testerStationsRepo = testerStationsRepo;
        this.testerResultQueue = testerResultQueue;

        testerThreadPool = Executors.newCachedThreadPool();
        resultProcessorThread = Executors.newSingleThreadExecutor();

        resultProcessorThread.submit(context.getBean(TesterResultProcessor.class));
    }

    @Override
    public void run() {
        try {
            while (active) {
                synchronized (lock) {
                    checkAndAllocateTesters();
                    lock.wait(checkInterval);
                }
            }
        } catch (InterruptedException e) {
            stop();
            Thread.currentThread().interrupt();
            System.err.println("Tester backgrounder interrupted");
        } catch (Exception e) {
            System.err.println("Error in tester backgrounder operation: " + e.getMessage());
        }
    }

    public void triggerBacklogCheck() {
        logger.info("Triggering tester backlog check");
        synchronized (lock) {
            // Wake the waiting thread
            lock.notify();
        }
    }

    public void stop() {
        logger.info("Stopping tester backgrounder thread");
        active = false;
        // Ensure the loop exits if it is waiting
        triggerBacklogCheck();

        testerThreadPool.shutdown();
        try {
            if (!testerThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                testerThreadPool.shutdownNow();
            }
        } catch (InterruptedException ie) {
            testerThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void checkAndAllocateTesters() {
        logger.info("Running checkAndAllocateTesters");
        List<Object[]> testerBacklog = testerBacklogRepo.getPendingTesterBacklog();
        Map<Integer, List<Integer>> availTesters = getAvailableTesterStationsGroupedByLayout();

        for (Object[] backlogEntry : testerBacklog) {
            int testerBacklogId = (Integer) backlogEntry[0];
            int terminalLayoutId = (Integer) backlogEntry[1];
            int testSchemeId = (Integer) backlogEntry[2];
            int batteryId = (Integer) backlogEntry[3];

            logger.info("Tester backlog [" + testerBacklogId + "] for battery " + batteryId + " looking for tester");
            // if the desired terminal layout is present in the avail testers mapping
            if (availTesters.containsKey(terminalLayoutId) && !availTesters.get(terminalLayoutId).isEmpty()) {
                int selectedTester = availTesters.get(terminalLayoutId).get(0);
                boolean sendSuccess =
                        sendBatteryToTester(testerBacklogId, selectedTester, batteryId, testSchemeId, terminalLayoutId);
                logger.info("Tester backlog [" + testerBacklogId + "] tester " + selectedTester + " found");

                // remove the tester from the list of available testers if battery sent successfully
                if (sendSuccess) {
                    logger.info("Tester backlog [" + testerBacklogId + "] added to tester " + selectedTester);
                    availTesters.get(terminalLayoutId).remove(0);
                }
            }
        }
    }

    @Transactional
    private boolean sendBatteryToTester(int testerBacklogId, int testerId, int batteryId, int testSchemeId, int terminalLayoutId) {
        //  TesterBacklog: Set end date
        testerBacklogRepo.endTesterBacklogEntry(testerBacklogId, Timestamp.from(Instant.now()));
        //  TesterStations: Update status, Active Battery Id, Last used date
        testerStationsRepo.markTesterInUse(testerId, batteryId, Timestamp.from(Instant.now()));

        //  Start a cached thread to perform the testing
        TesterStationRepository tsrBean = context.getBean(TesterStationRepository.class);
        TesterRunnable tr =
                new TesterRunnable(tsrBean, testerResultQueue, testerId, testSchemeId, batteryId, terminalLayoutId);
        testerThreadPool.submit(tr);

        return true;
    }

    private Map<Integer, List<Integer>> getAvailableTesterStationsGroupedByLayout() {
        List<Object[]> results = testerStationsRepo.getAvailableTesterStations();
        return results.stream()
                .collect(Collectors.groupingBy(
                        result -> (Integer) result[1], // terminal_layout_id
                        Collectors.mapping(result -> (Integer) result[0], // tester_stn_id
                                Collectors.toList())
                ));
    }
}

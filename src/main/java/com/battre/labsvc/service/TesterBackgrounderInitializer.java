package com.battre.labsvc.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class TesterBackgrounderInitializer {
    private static final Logger logger = Logger.getLogger(TesterBackgrounderInitializer.class.getName());
    private final ApplicationContext context;
    private ExecutorService backgrounderThread;
    private ExecutorService resultProcessorThread;

    @Autowired
    TesterBackgrounderInitializer(ApplicationContext context) {
        this.context = context;
    }

    @PostConstruct
    public void startBackgroundThreads() {
        logger.info("startBackgroundThreads started");
        TesterBackgrounder testerBG = context.getBean(TesterBackgrounder.class);
        backgrounderThread = Executors.newSingleThreadExecutor();
        backgrounderThread.submit(testerBG);
        logger.info("backgrounderThread started");

        TesterResultProcessor testerRP = context.getBean(TesterResultProcessor.class);
        resultProcessorThread = Executors.newSingleThreadExecutor();
        resultProcessorThread.submit(testerRP);
        logger.info("resultProcessorThread started");
    }

    @PreDestroy
    public void cleanUp() {
        backgrounderThread.shutdown();
        try {
            if (!backgrounderThread.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                backgrounderThread.shutdownNow();
            }
        } catch (InterruptedException e) {
            backgrounderThread.shutdownNow();
        }
        resultProcessorThread.shutdown();
        try {
            if (!resultProcessorThread.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                resultProcessorThread.shutdownNow();
            }
        } catch (InterruptedException e) {
            resultProcessorThread.shutdownNow();
        }
    }

}

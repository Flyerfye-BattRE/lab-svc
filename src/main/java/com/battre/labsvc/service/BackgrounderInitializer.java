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
public class BackgrounderInitializer {
    private static final Logger logger = Logger.getLogger(BackgrounderInitializer.class.getName());
    private final ApplicationContext context;
    private ExecutorService testerBackgrounderThread;
    private ExecutorService testerResultProcessorThread;
    private ExecutorService refurbBackgrounderThread;
    private ExecutorService refurbResultProcessorThread;

    @Autowired
    BackgrounderInitializer(ApplicationContext context) {
        this.context = context;
    }

    @PostConstruct
    public void startBackgroundThreads() {
        logger.info("startBackgroundThreads started");
        TesterBackgrounder testerBG = context.getBean(TesterBackgrounder.class);
        testerBackgrounderThread = Executors.newSingleThreadExecutor();
        testerBackgrounderThread.submit(testerBG);
        logger.info("testerBackgrounderThread started");

        TesterResultProcessor testerRP = context.getBean(TesterResultProcessor.class);
        testerResultProcessorThread = Executors.newSingleThreadExecutor();
        testerResultProcessorThread.submit(testerRP);
        logger.info("testerResultProcessorThread started");

        RefurbBackgrounder refurbBG = context.getBean(RefurbBackgrounder.class);
        refurbBackgrounderThread = Executors.newSingleThreadExecutor();
        refurbBackgrounderThread.submit(refurbBG);
        logger.info("refurbBackgrounderThread started");

        RefurbResultProcessor refurbRP = context.getBean(RefurbResultProcessor.class);
        refurbResultProcessorThread = Executors.newSingleThreadExecutor();
        refurbResultProcessorThread.submit(refurbRP);
        logger.info("refurbResultProcessorThread started");
    }

    @PreDestroy
    public void cleanUp() {
        testerBackgrounderThread.shutdown();
        try {
            if (!testerBackgrounderThread.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                testerBackgrounderThread.shutdownNow();
            }
        } catch (InterruptedException e) {
            testerBackgrounderThread.shutdownNow();
        }
        testerResultProcessorThread.shutdown();
        try {
            if (!testerResultProcessorThread.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                testerResultProcessorThread.shutdownNow();
            }
        } catch (InterruptedException e) {
            testerResultProcessorThread.shutdownNow();
        }
        refurbBackgrounderThread.shutdown();
        try {
            if (!refurbBackgrounderThread.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                refurbBackgrounderThread.shutdownNow();
            }
        } catch (InterruptedException e) {
            refurbBackgrounderThread.shutdownNow();
        }
        refurbResultProcessorThread.shutdown();
        try {
            if (!refurbResultProcessorThread.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                refurbResultProcessorThread.shutdownNow();
            }
        } catch (InterruptedException e) {
            refurbResultProcessorThread.shutdownNow();
        }
    }

}

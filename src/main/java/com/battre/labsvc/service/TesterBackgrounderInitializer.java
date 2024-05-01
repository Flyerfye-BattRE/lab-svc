package com.battre.labsvc.service;

import com.battre.labsvc.repository.TesterBacklogRepository;
import com.battre.labsvc.repository.TesterStationsRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TesterBackgrounderInitializer {
    private ExecutorService executorService;
    private final TesterBacklogRepository testerBacklogRepo;
    private final TesterStationsRepository testerStationsRepo;

    TesterBackgrounderInitializer(TesterBacklogRepository testerBacklogRepo,
                                  TesterStationsRepository testerStationsRepo){
        this.testerBacklogRepo = testerBacklogRepo;
        this.testerStationsRepo = testerStationsRepo;

    }

    @PostConstruct
    public void startBackgrounder() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new TesterBackgrounder(testerBacklogRepo, testerStationsRepo));
    }
}

package com.battre.labsvc.service;

import com.battre.labsvc.model.TesterBacklogType;
import com.battre.labsvc.model.TesterStationType;
import com.battre.labsvc.repository.TesterBacklogRepository;
import com.battre.labsvc.repository.TesterStationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TesterBackgrounderTest {
    @Mock
    private ApplicationContext context;
    @Mock
    private TesterBacklogRepository testerBacklogRepo;
    @Mock
    private TesterStationRepository testerStationsRepo;

    @InjectMocks
    private TesterBackgrounder testerBackgrounder;

    @Test
    public void testCheckAndAllocateTesters() throws Exception {
        // testerBacklogId, terminalLayoutId, testSchemeId, batteryId
        TesterBacklogType mockTesterBacklogEntry = new TesterBacklogType(2, 4, 3);
        mockTesterBacklogEntry.setTesterBacklogId(1);

        List<TesterBacklogType> mockBacklog = List.of(
                mockTesterBacklogEntry
        );
        when(testerBacklogRepo.getCurrentTesterBacklog()).thenReturn(mockBacklog);
        // testerStationId, terminalLayoutId
        List<TesterStationType> mockStations = Arrays.asList(
                new TesterStationType(1, 3),
                new TesterStationType(2, 6)
        );
        when(testerStationsRepo.getAvailableTesterStations()).thenReturn(mockStations);

        testerBackgrounder.checkAndAllocateTesters();

        // Verify
        verify(testerBacklogRepo).getCurrentTesterBacklog();
        verify(testerStationsRepo).getAvailableTesterStations();
        verify(testerBacklogRepo).endTesterBacklogEntry(eq(1), any(Timestamp.class));
        verify(testerStationsRepo).markTesterInUse(eq(1), eq(2), any(Timestamp.class));
    }
}

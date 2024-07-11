package com.battre.labsvc.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.battre.labsvc.model.RefurbResultRecord;
import com.battre.labsvc.repository.RefurbPlanRepository;
import com.battre.labsvc.repository.RefurbStationRepository;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
public class RefurbBackgrounderTest {
  @Mock private ApplicationContext context;
  @Mock private RefurbPlanRepository refurbPlanRepo;
  @Mock private RefurbStationRepository refurbStationsRepo;
  @Mock private BlockingQueue<RefurbResultRecord> refurbResultQueue;

  @InjectMocks private RefurbBackgrounder refurbBackgrounder;

  @Test
  public void testCheckAndAllocateRefurb() throws Exception {
    // refurbPlanId, batteryId, needResolder, needRepack, needProcessorSwap, needCapacitorSwap
    List<Object[]> mockPlans =
        Arrays.asList(
            new Object[] {1, 2, true, false, true, false},
            new Object[] {2, 3, false, true, false, false},
            new Object[] {3, 4, false, false, false, false});
    when(refurbPlanRepo.getCurrentRefurbSchemeStatuses()).thenReturn(mockPlans);

    // refurbStnId, stationClass
    List<Object[]> mockStns =
        Arrays.asList(
            new Object[] {1, "PROCESSOR_SWAP"},
            new Object[] {2, "REPACK"},
            new Object[] {3, "RESOLDER"});
    when(refurbStationsRepo.getAvailableRefurbStns()).thenReturn(mockStns);

    refurbBackgrounder.checkAndAllocateRefurb();

    // Verify results
    verify(refurbPlanRepo).getCurrentRefurbSchemeStatuses();
    verify(refurbPlanRepo, times(3)).markRefurbPlanBusy(anyInt());
    verify(refurbStationsRepo, times(1)).markRefurbStnInUse(eq(1), eq(2), any(Timestamp.class));
    verify(refurbStationsRepo, times(1)).markRefurbStnInUse(eq(2), eq(3), any(Timestamp.class));
    verify(refurbStationsRepo, times(1)).markRefurbStnInUse(eq(3), eq(2), any(Timestamp.class));
    // when test class run individually, 4 invocs (3 TestRunnable, 1 RefurbBG) detected
    verify(refurbResultQueue, atLeast(1)).put(any(RefurbResultRecord.class));
  }
}

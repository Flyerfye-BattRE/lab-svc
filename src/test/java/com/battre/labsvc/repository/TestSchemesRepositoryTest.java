package com.battre.labsvc.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.battre.labsvc.model.TestSchemeType;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class TestSchemesRepositoryTest {
    private static final Logger logger = Logger.getLogger(TestSchemesRepositoryTest.class.getName());

    @Autowired
    private TestSchemesRepository testSchemesRepo;

    @Test
    public void testGetDataForTestScheme() {
        Optional<TestSchemeType> result = testSchemesRepo.getDataForTestScheme(6);

        // Verify the result
        assertFalse(result.get().isCheckerboard());
        assertTrue(result.get().isNullLine());
        assertFalse(result.get().isVaporSim());
        assertTrue(result.get().isBlackout());
        assertFalse(result.get().isOvenScreen());
    }
}

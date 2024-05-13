package com.battre.labsvc.repository;

import com.battre.labsvc.model.TestSchemeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(false, result.get().isCheckerboard());
        assertEquals(true, result.get().isNullLine());
        assertEquals(false, result.get().isVaporSim());
        assertEquals(true, result.get().isBlackout());
        assertEquals(false, result.get().isOvenScreen());
    }
}

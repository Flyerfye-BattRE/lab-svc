package com.battre.labsvc.repository;

import com.battre.labsvc.model.RefurbSchemeType;
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
public class RefurbSchemesRepositoryTest {
    private static final Logger logger = Logger.getLogger(RefurbSchemesRepositoryTest.class.getName());

    @Autowired
    private RefurbSchemesRepository refurbSchemesRepo;

    @Test
    @Sql(scripts = {"/testdb/test-rscr-populateRefurbSchemes.sql"})
    public void testGetDataForRefurbScheme() {
        Optional<RefurbSchemeType> result = refurbSchemesRepo.getDataForRefurbScheme(2);

        // Verify the result
        assertEquals(false, result.get().isResolder());
        assertEquals(true, result.get().isRepack());
        assertEquals(false, result.get().isProcessorSwap());
        assertEquals(true, result.get().isCapacitorSwap());
    }
}

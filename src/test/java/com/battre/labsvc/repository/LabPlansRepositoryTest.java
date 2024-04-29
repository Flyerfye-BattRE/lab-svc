package com.battre.labsvc.repository;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.logging.Logger;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class LabPlansRepositoryTest {
    private static final Logger logger = Logger.getLogger(LabPlansRepositoryTest.class.getName());

    @Autowired
    private LabPlansRepository labPlansRepo;

    // No repository fns => No repository tests
}

package com.battre.labsvc.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.logging.Logger;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class TesterBacklogRepositoryTest {
    private static final Logger logger = Logger.getLogger(TesterBacklogRepositoryTest.class.getName());

    @Autowired
    private TesterBacklogRepository testerBacklogRepo;

    // No repository fns => No repository tests
}

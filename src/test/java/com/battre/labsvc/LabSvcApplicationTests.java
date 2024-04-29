package com.battre.labsvc;

import com.battre.stubs.services.SpecSvcGrpc;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class LabSvcApplicationTests {
    @MockBean
    private SpecSvcGrpc.SpecSvcStub specSvcClient;
    @Test
    void contextLoads() {
    }

}

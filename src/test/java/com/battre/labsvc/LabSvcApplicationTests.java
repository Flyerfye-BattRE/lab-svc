package com.battre.labsvc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "grpc.server.port=9040")
class LabSvcApplicationTests {
  @Test
  void contextLoads() {}
}

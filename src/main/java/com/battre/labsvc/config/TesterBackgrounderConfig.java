package com.battre.labsvc.config;

import com.battre.labsvc.model.TesterResultRecord;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesterBackgrounderConfig {
  @Bean
  public BlockingQueue<TesterResultRecord> testerResultQueue() {
    return new LinkedBlockingQueue<>();
  }
}

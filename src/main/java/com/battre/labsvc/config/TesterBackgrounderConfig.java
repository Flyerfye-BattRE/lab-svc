package com.battre.labsvc.config;

import com.battre.labsvc.model.TesterResultRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class TesterBackgrounderConfig {
  @Bean
  public BlockingQueue<TesterResultRecord> testerResultQueue() {
    return new LinkedBlockingQueue<>();
  }
}

package com.battre.labsvc.config;

import com.battre.labsvc.model.RefurbResultRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class RefurbBackgrounderConfig {
  @Bean
  public BlockingQueue<RefurbResultRecord> refurbResultQueue() {
    return new LinkedBlockingQueue<>();
  }
}

package com.battre.labsvc.service;

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

package com.banking.banking_api.config;

import com.Job_Queue.JQ_Service.sdk.JobQueueClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobQueueConfig {

    @Bean
    public JobQueueClient jobQueueClient() {
        return new JobQueueClient("http://localhost:8083");
    }
}
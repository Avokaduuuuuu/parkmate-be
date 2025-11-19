package com.parkmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Core threads
        executor.setMaxPoolSize(20); // Max threads for email sending
        executor.setQueueCapacity(100); // Queue for pending tasks
        executor.setThreadNamePrefix("email-sender-");
        executor.initialize();
        return executor;
    }
}

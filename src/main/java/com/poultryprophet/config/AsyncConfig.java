package com.poultryprophet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Thread pool backing the analytics worker (SDD 2.1 IndicatorJobWorker). Replaces the
 * Node.js BullMQ queue with an in-process async executor: record-created events are
 * handled off the request thread after the originating transaction commits.
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "analyticsExecutor")
    public TaskExecutor analyticsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("analytics-");
        executor.initialize();
        return executor;
    }
}

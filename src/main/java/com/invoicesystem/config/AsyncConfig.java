package com.invoicesystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring Boot auto-configures the async executor via spring.task.execution properties
}

package com.vpm.Accounts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import com.vpm.Accounts.security.TenantContext;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("accountExecutor")
    public Executor accountExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("account-exec-");
        executor.setTaskDecorator(task -> {
            Long submittedTenantId = TenantContext.get();
            return () -> {
                Long previousTenantId = TenantContext.get();
                try {
                    if (submittedTenantId == null) {
                        TenantContext.clear();
                    } else {
                        TenantContext.set(submittedTenantId);
                    }
                    task.run();
                } finally {
                    if (previousTenantId == null) {
                        TenantContext.clear();
                    } else {
                        TenantContext.set(previousTenantId);
                    }
                }
            };
        });
        executor.initialize();
        return executor;
    }
}

package org.pnop.waf.sample.retry.sb;

import org.pnop.waf.sample.retry.sb.services.RetryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableRetry
@Component
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    @Bean("retryConfig")
    public RetryConfig getRetryConfig() {
        return new RetryConfig();
    }
}

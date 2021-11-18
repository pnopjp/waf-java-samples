package org.pnop.sample.waf.cb.sb;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.springretry.SpringRetryCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.CircuitBreakerRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.client.HttpServerErrorException;

@Configuration
public class SampleConfiguration {

    private static Logger logger = LoggerFactory.getLogger(SampleConfiguration.class);

    @Bean
    public Customizer<SpringRetryCircuitBreakerFactory> defaultCustomizer() {

        logger.info("defaultCustomizer");

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(HttpServerErrorException.class, true);
        RetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
        CircuitBreakerRetryPolicy policy = new CircuitBreakerRetryPolicy(retryPolicy);
        policy.setOpenTimeout(5000);
        policy.setResetTimeout(10000);

        return factory -> factory
            .configure(builder -> builder.retryPolicy(policy).build(), "myconfig1");
    }
}

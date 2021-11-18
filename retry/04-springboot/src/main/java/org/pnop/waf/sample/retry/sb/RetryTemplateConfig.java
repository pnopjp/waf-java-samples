package org.pnop.waf.sample.retry.sb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryTemplateConfig {

    @Bean
    RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy exponentialBackkOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackkOffPolicy.setInitialInterval(2000L);
        exponentialBackkOffPolicy.setMultiplier(2.0);
        retryTemplate.setBackOffPolicy(exponentialBackkOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        retryTemplate.registerListener(new CustomRetryListener());
        return retryTemplate;
    }
    
    public static class CustomRetryListener implements RetryListener {
        private Logger logger = LoggerFactory.getLogger(CustomRetryListener.class);

        @Override
        public <T, E extends Throwable> boolean open(RetryContext context,
            RetryCallback<T, E> callback) {
            logger.info("open");
            return true;
        }

        @Override
        public <T, E extends Throwable> void close(RetryContext context,
            RetryCallback<T, E> callback, Throwable throwable) {
            logger.info("close");
        }

        @Override
        public <T, E extends Throwable> void onError(RetryContext context,
            RetryCallback<T, E> callback, Throwable throwable) {
            logger.info("onError, retry count : {}", context.getRetryCount());
        }
    }
}

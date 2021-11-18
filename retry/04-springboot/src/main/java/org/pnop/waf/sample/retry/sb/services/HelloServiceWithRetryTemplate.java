package org.pnop.waf.sample.retry.sb.services;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
public class HelloServiceWithRetryTemplate {

    private Logger logger = LoggerFactory.getLogger(HelloServiceWithRetryTemplate.class);
    private RetryTemplate retryTemplate;

    public HelloServiceWithRetryTemplate(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    public String sayHello(String name) throws IOException {
        logger.info("sayHello");
        retryTemplate.execute(new RetryCallback<Void, IOException>() {
            @Override
            public Void doWithRetry(RetryContext context) throws IOException {
                logger.info("retry count : " + context.getRetryCount());
                someFunction();
                return null;
            }
        });
        return String.format("Hello %s !!", name);
    }

    private static void someFunction() throws IOException {
        // ランダムに IOExceptionをスローする
        double r = Math.random();
        if (r < 0.7) {
            throw new IOException("IO Error");
        }
    }
}

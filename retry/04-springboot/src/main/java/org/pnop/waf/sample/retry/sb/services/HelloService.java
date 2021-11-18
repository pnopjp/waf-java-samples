package org.pnop.waf.sample.retry.sb.services;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class HelloService {

    private Logger logger = LoggerFactory.getLogger(HelloService.class);

    public HelloService() {
    }

    // 最大試行回数4回
    // 段階的間隔 3秒,6秒,12秒
    @Retryable(
        value = { IOException.class }, 
        maxAttempts = 5, 
        backoff = @Backoff(delay = 3000, multiplier = 2))
    public String sayHello(String name) throws IOException {
        logger.info("sayHello");
        someFunction();
        return String.format("Hello %s !!", name);
    }

    // applicaiton.yml に記述した例
    @Retryable(
        value = { IOException.class },  
        maxAttemptsExpression = "${my.retry.maxAttempts}",
        backoff = @Backoff(delayExpression = "${my.retry.delay}",
                           multiplierExpression= "${my.retry.multiplier}"))
    public String sayHello2(String name) throws IOException {
        logger.info("sayHello2");
        someFunction();
        return String.format("Hello %s !!", name);
    }

    // Beanから取得する例
    @Retryable(
        value = { IOException.class }, 
        maxAttemptsExpression = "#{@retryConfig.getMaxAttempts()}",
        backoff = @Backoff(delay = 3000, multiplier = 2))
    public String sayHello3(String name) throws IOException {
        logger.info("sayHello2");
        someFunction();
        return String.format("Hello %s !!", name);
    }

    private static void someFunction() throws IOException {
        double r = Math.random();
        if (r < 1.0) {
            throw new IOException("IO Error");
        }
    }

    
}

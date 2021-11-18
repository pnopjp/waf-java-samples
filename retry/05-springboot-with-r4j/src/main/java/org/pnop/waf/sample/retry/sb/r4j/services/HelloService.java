package org.pnop.waf.sample.retry.sb.r4j.services;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class HelloService {

    private Logger logger = LoggerFactory.getLogger(HelloService.class);

    private RetryRegistry registry;

    public HelloService(RetryRegistry registry) {
        this.registry = registry;
    }

    @Retry(name = "helloService", fallbackMethod = "fallBackMethod")
    public String sayHello(String name) throws IOException {
        logger.info("sayHello");
        someFunction();
        return String.format("Hello %s !!", name);
    }

    private static void someFunction() throws IOException {
        double r = Math.random();
        if (r < 0.7) {
            throw new IOException("IO Error");
        }
    }

    private String fallBackMethod(String name, Exception e) {
        logger.info("fallback : {}, {}", name, e.getMessage());
        return "fallback";
    }

    @PostConstruct
    public void postConstruct() {
        registry
            .retry("helloService")
            .getEventPublisher()
            .onRetry(event -> logger.info("{}", event));
    }
}

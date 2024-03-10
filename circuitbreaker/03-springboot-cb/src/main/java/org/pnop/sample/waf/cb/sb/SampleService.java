package org.pnop.sample.waf.cb.sb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("rawtypes")
@Service
public class SampleService {

    private static Logger logger = LoggerFactory.getLogger(SampleService.class);
    private RestTemplate restTemplate;
    private org.springframework.cloud.client.circuitbreaker.CircuitBreaker cb;

    public SampleService(RestTemplateBuilder builder, CircuitBreakerFactory cbFactory) {
        this.restTemplate = builder.build();
        this.cb = cbFactory.create("myconfig1");
    }

    public String call1(int code) {
        return cb.run(() -> {
            String url = "http://httpbin.org/status/" + code;
            logger.info("request : {}", url);
            restTemplate.getForObject(url, String.class);
            return "success";
        }, throwable -> {
            logger.error("{} {}", throwable.getClass().getName(), throwable.getMessage());
            return "fallback";
        });
    }

    @Recover
    private String fallbackForCall() {
        logger.error("Fallback for call invoked");
        return "fallback";
    }

    @CircuitBreaker(maxAttempts = 3, openTimeout = 5000, resetTimeout = 10000, 
        include = {
            HttpServerErrorException.class,
    })
    public String call2(int code) {
        String url = "http://httpbin.org/status/" + code;
        logger.info("request : {}", url);
        restTemplate.getForObject(url, String.class);
        logger.info("success");
        return "success";
    }

}
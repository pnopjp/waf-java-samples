package org.pnop.wa.sample.cb.sb.r4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@SuppressWarnings("rawtypes")
@Service
public class SampleService {

    private static Logger logger = LoggerFactory.getLogger(SampleService.class);

    private CircuitBreakerFactory cbFactory;

    private RestTemplate restTemplate;

    public SampleService(RestTemplateBuilder restTemplateBuilder, CircuitBreakerFactory cbFactory) {
        this.restTemplate = restTemplateBuilder.build();
        this.cbFactory = cbFactory;
    }

    public String call1(int code) {
        return cbFactory.create("myconfig1").run(() -> {
            String url = "http://httpbin.org/status/" + code;
            logger.info("request : {}", url);
            restTemplate.getForObject(url, String.class);
            logger.info("success");
            return "success";
        }, throwable -> {
            logger.error("{} {}", throwable.getClass().getName(), throwable.getMessage());
            logger.info("fallbak");
            return "fallback";
        });
    }

    @CircuitBreaker(name = "myconfig2", fallbackMethod = "fallback2")
    public String call2(int code) {
        String url = "http://httpbin.org/status/" + code;
        logger.info("request : {}", url);
        restTemplate.getForObject(url, String.class);
        logger.info("success");
        return "success";
    }

    public String fallback2(int code, RuntimeException e) {
        logger.info("fallback2 : code = {} , exception = {}, message = {}",
            code,
            e.getClass().getName(),
            e.getMessage());
        return "fallback2";
    }
}

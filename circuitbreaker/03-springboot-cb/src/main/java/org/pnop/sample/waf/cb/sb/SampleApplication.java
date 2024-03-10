package org.pnop.sample.waf.cb.sb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class SampleApplication {

    private static Logger logger = LoggerFactory.getLogger(SampleApplication.class);

    public static void main(String[] args) {
        logger.info("start application");
        SpringApplication.run(SampleApplication.class, args);
    }
}

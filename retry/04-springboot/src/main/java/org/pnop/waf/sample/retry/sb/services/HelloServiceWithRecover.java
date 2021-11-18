package org.pnop.waf.sample.retry.sb.services;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class HelloServiceWithRecover {

    private Logger logger = LoggerFactory.getLogger(HelloServiceWithRecover.class);

    // 試行回数 3回
    // ２秒毎の等間隔リトライ
    @Retryable(
        value = { IOException.class }, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 2000))
    public String sayHello(String name) throws IOException {
        logger.info("sayHello");
        someFunction();
        return String.format("Hello %s !!", name);
    }

    @Recover
    public String recover(IOException e, String name) {
        logger.info("recover : {}",name);
        return "";
    }

    private static void someFunction() throws IOException {
        double r = Math.random();
        if (r < 0.7) {
            throw new IOException("IO Error");
        }
    }
}

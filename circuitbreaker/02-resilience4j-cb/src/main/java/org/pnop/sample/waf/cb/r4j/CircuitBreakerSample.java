package org.pnop.sample.waf.cb.r4j;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;

public class CircuitBreakerSample {

    private static Logger logger = LoggerFactory.getLogger(CircuitBreakerSample.class);
    private CircuitBreaker circutBreaker;

    public CircuitBreakerSample() {

        CircuitBreakerConfig config = CircuitBreakerConfig
            .custom()
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10)
            .failureRateThreshold(30)
            .permittedNumberOfCallsInHalfOpenState(5)
            .recordExceptions(IOException.class, RuntimeException.class)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .waitDurationInOpenState(Duration.ofSeconds(5))
            .build();

        CircuitBreakerRegistry registory = CircuitBreakerRegistry.of(config);
        circutBreaker = registory.circuitBreaker("mycircuitbreaker");
    }

    public void run() {

        // コールバック関数を定義
        Function<Boolean, String> decorateFunction = CircuitBreaker
            .decorateFunction(circutBreaker, new Function<Boolean, String>() {
                @Override
                public String apply(Boolean isThrow) {
                    if (isThrow) {
                        throw new RuntimeException("呼び出し失敗");
                    } else {
                        return "呼び出し成功";
                    }
                }
            });

        // 10回中3回失敗すると OPEN 状態になり、以降 OPEN 状態なのでActionは呼ばれない
        for (int i = 0; i < 20; i++) {
            // 3回に一回例外を発生させる
            boolean throwException = i % 3 == 1;
            invoke(decorateFunction, throwException);
        }

        // HALF OPEN になるまで 5秒以上待つ
        logger.info("---------------- waiting ----------------------");
        sleep(Duration.ofSeconds(6));

        // HALF OPEN 状態で、3回失敗すると再度 OPEN 状態となる
        for (int i = 0; i < 10; i++) {
            invoke(decorateFunction, true);
        }

        // HALF OPEN になるまで 5秒以上待つ
        logger.info("---------------- waiting ----------------------");
        sleep(Duration.ofSeconds(6));

        // HALF OPEN 状態で、成功の閾値を上回ると CLOSED 状態となる
        for (int i = 0; i < 10; i++) {
            invoke(decorateFunction, i % 5 == 0);
        }

    }

    private void invoke(Function<Boolean, String> decorated, boolean arg) {

        State before = circutBreaker.getState();
        Try<String> result = Try.of(() -> decorated.apply(arg));
        State after = circutBreaker.getState();

        if (result.isSuccess()) {
            logger.info("state = {} -> {}, success = {}, result = {}",
                before, after, result.isSuccess(), result.getOrNull());
        } else {
            logger.info("state = {} -> {}, success = {}, cause = {}",
                before, after, result.isSuccess(), result.getCause().getMessage());
        }
    }

    private static void sleep(Duration d) {
        try {
            Thread.sleep(d.toMillis());
        } catch (InterruptedException e) {
        }
    }
}

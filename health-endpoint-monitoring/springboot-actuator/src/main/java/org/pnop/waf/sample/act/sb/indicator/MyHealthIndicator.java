package org.pnop.waf.sample.act.sb.indicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("MyApplication")
public class MyHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        int errorCode = check();
        if (errorCode != 0) {
            return Health
                .down()
                .withDetail("Error Code", errorCode)
                .withDetail("PropertyA", 99999)
                .withDetail("PropertyB", false)
                .build();
        }

        return Health
            .up()
            .withDetail("Property1", 1234)
            .withDetail("Property2", "ABCD")
            .withDetail("Property3", true)
            .build();
    }

    private int check() {
        // 実際にはアプリケーションのエラーチェック等を行う
        if (Math.random() < 0.5) {
            log.warn("An error has occurred");
            return 1;
        }
        log.info("check successful.");
        return 0;
    }
}

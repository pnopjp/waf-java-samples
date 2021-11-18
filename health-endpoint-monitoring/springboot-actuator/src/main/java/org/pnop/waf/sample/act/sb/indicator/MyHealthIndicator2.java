package org.pnop.waf.sample.act.sb.indicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("MyApplication2")
public class MyHealthIndicator2 implements HealthIndicator {

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
            .withDetail("PropertyX", "XYZ")
            .withDetail("PropertyY", 84803)
            .withDetail("PropertyZ", 10.012)
            .build();
    }

    private int check() {
        if (Math.random() < 0.5) {
            log.warn("An error has occurred");
            return 1;
        }
        log.info("check successful.");
        return 0;
    }
}

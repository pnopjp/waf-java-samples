package org.pnop.sample.waf.retry.r4j;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        logger.info("Resilience4j retry sample start");

        // 成功
        new RetrySample().run(URI.create("https://httpbin.org/status/200"));

        // 404 失敗 リトライせず
        new RetrySample().run(URI.create("https://httpbin.org/status/404"));

        // 500 失敗 リトライ
        new RetrySample().run(URI.create("https://httpbin.org/status/500"));

        // 強制的にタイムアウトになるように調整
        new RetrySample().run(URI.create("https://httpbin.org/delay/10"));

        logger.info("end");
    }
}

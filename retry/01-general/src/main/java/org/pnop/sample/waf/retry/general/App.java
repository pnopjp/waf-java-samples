package org.pnop.sample.waf.retry.general;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        logger.info("main start");

        // 成功
        logger.info("*** success ***");
        new RetrySample().run(URI.create("https://httpbin.org/status/200"));

        // 500 失敗
        logger.info("*** internal server errror ***");
        new RetrySample().run(URI.create("https://httpbin.org/status/500"));

        // 429 失敗
        logger.info("*** too many request ***");
        new RetrySample().run(URI.create("https://httpbin.org/status/429"));

        // 強制的にタイムアウトになるように調整
        logger.info("*** timeout ***");
        new RetrySample().run(URI.create("https://httpbin.org/delay/10"));

        logger.info("main end");
    }
}

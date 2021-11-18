package org.pnop.sample.waf.retry.httpcomp;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    
    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {

        logger.info("httpcomponets sample start");

        // 正常パターン
        new RetrySample().run(URI.create("https://httpbin.org/status/200"));
        
        // 500 を返すパターン
        new RetrySample().run(URI.create("https://httpbin.org/status/500"));

        logger.info("end");
    }
}

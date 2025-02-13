package org.pnop.sample.waf.retry.httpcomp;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrySample {

    private static Logger logger = LoggerFactory.getLogger(RetrySample.class);

    public boolean run(URI uri) throws Exception {

        // ReuqestConfig で各種タイムアウトの設定ができる
        RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofSeconds(10))
            .build();

        // HttpClient のインスタンス化
        try (final CloseableHttpClient httpclient = HttpClientBuilder
            .create()
            .setDefaultRequestConfig(config)
            .setRetryStrategy(new MyRetryStrategy())
            .build()) {

            final HttpGet httpget = new HttpGet(uri);
            logger.info("Executing request " + httpget.getMethod() + " " + httpget.getUri());

            // リクエスト
            httpclient.execute(httpget, response -> {
                String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                System.out.println(content);
                logger.info("response code : {}", response.getCode());
                return null;
            });
        }
        return true;

    }

    public static class MyRetryStrategy implements HttpRequestRetryStrategy {

        private static int MAX_RETRY_COUNT = 5; // リトライ回数
        private static int RETRY_INTERVAL = 3; // リトライ間隔

        /**
         * リトライすべき条件を判定します。
         */
        @Override
        public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
            int code = response.getCode();

            if (code >= 200 && code <= 299) {
                logger.info("success");
                return false;
            }

            if (execCount <= MAX_RETRY_COUNT) {
                if (code == HttpStatus.SC_INTERNAL_SERVER_ERROR ||
                    code == HttpStatus.SC_SERVICE_UNAVAILABLE ||
                    code == HttpStatus.SC_TOO_MANY_REQUESTS) {

                    logger.info("should be retry. execute count : {} , statu code : {}", execCount, code);
                    return true;
                }
            }

            logger.info("Number of retries exceeded or response code does not match retry status code :  {}",
                execCount);
            return false;
        }

        /**
         * リトライすべき例外を判定します。
         */
        @Override
        public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
            logger.info("retry with exception");
            if (execCount <= MAX_RETRY_COUNT) {
                return exception instanceof SocketTimeoutException;
            }
            return false;
        }

        /**
         * リトライ間隔を計算します。
         */
        @Override
        public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
            // 段階的間隔
            return TimeValue.ofSeconds(RETRY_INTERVAL * execCount);
        }
    }
}

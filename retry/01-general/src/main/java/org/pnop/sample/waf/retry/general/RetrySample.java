package org.pnop.sample.waf.retry.general;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrySample {

    private static Logger logger = LoggerFactory.getLogger(RetrySample.class);

    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_INTERVAL = 3;

    public boolean run(URI uri) {

        var retryCount = 0;

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();

        while (true) {

            logger.info("retry count : {}", retryCount);

            try {
                HttpResponse<String> response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
                int code = response.statusCode();
                logger.info("response : {} {}", code, response.body());
                if (code >= 200 && code <= 299) {
                    return true;
                }
                // リトライすべきステータスコードかチェック
                if (code != 500 && code != 503 && code != 429) {
                    return false;
                }

            } catch (HttpTimeoutException e) {
                // タイムアウトの例外はリトライ対象
                logger.warn("HttpTimeoutException");
            } catch (IOException | InterruptedException e) {
                // それ以外の例外は失敗
                logger.error("Exception", e);
                return false;
            }

            retryCount++;

            // リトライ回数のチェック
            if (retryCount > MAX_RETRY_COUNT) {
                logger.error("Number of retries exceeded");
                return false;
            }

            // 待ち時間 （等間隔）
            try {
                logger.info("waiting....");
                TimeUnit.SECONDS.sleep(RETRY_INTERVAL);
            } catch (InterruptedException e) {
                return false;
            }
        }
    }
}
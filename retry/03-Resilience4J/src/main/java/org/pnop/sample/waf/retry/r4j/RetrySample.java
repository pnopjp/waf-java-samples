package org.pnop.sample.waf.retry.r4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

public class RetrySample {

    private static Logger logger = LoggerFactory.getLogger(RetrySample.class);

    private static int MAX_ATTEMPT_COUNT = 4; // リトライ回数は3となる
    private static int RETRY_INTERVAL = 3;

    public boolean run(URI uri) {

        // リトライの構成を設定する
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(MAX_ATTEMPT_COUNT)
            .retryOnResult(response -> ((HttpResponse<?>) response).statusCode() == 500)
            .retryExceptions(IOException.class, TimeoutException.class)
            .failAfterMaxAttempts(true)
            .intervalFunction(
                IntervalFunction
                    .ofExponentialBackoff(Duration.ofSeconds(RETRY_INTERVAL), 2d))
            .build();

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();

        // Retry インスタンスを取得
        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("retry");

        // イベントをハンドルする
        retry.getEventPublisher()
            .onRetry(event -> logger.info("onRetry : {}", event.toString()))
            .onError(event -> logger.info("onError : {}", event.toString()))
            .onSuccess(event -> logger.info("onSuccess : {}", event.toString()));

        try {
            // Retry#executeCallable のコールバックとしてリトライすべき処理を記述する
            HttpResponse<String> response = retry.executeCallable(new Callable<HttpResponse<String>>() {
                public java.net.http.HttpResponse<String> call() throws Exception {
                    logger.info("Executing request : {} " ,request.uri());
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    return response;
                }
            });
            System.out.println(response.body());
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
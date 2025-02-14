# Retry Sample with Resilience4j

This is a retry sample using Resilience4j.

## Overview

This sample uses the retry component of the Resilience4j library. It uses a web service that returns arbitrary status codes as the external source being called.

1. The external service returns `200`. It finishes without retrying.
2. The external service returns `404`. It finishes without retrying.
3. The external service returns `500`. Since `500` is a status code that triggers a retry, it attempts to retry.
4. The external service returns `429`. It behaves the same as above.
5. The external service does not return a response within the specified time, resulting in an `HttpTimeoutException`. Since this is an exception that triggers a retry, it attempts to retry.

## Prerequisites

- Java 17 or later
- Maven 3.6 or later

## Dependencies

This sample uses the retry library of Resilience4j.

```xml
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-retry</artifactId>
            <version>2.0.2</version>
        </dependency>
```

You can also build and run from IDEs such as Visual Studio Code or Eclipse.

## Execution Results

The execution results of this sample are summarized by pattern.

### Pattern where the external service returns 200

Since a normal status is returned, it finishes without retrying.

```log
2021-09-27 13:54:47:506 INFO App - Resilience4j retry sample start
2021-09-27 13:54:47:904 INFO RetrySample - Executing request : https://httpbin.org/status/200
```

### Pattern where the external service returns 500

Since a status (`500`) that should be retried is returned, it retries up to the maximum number of attempts and eventually fails.

```log
2021-09-27 13:54:49:694 INFO RetrySample - Executing request : https://httpbin.org/status/500
2021-09-27 13:54:50:285 INFO RetrySample - onRetry : 2021-09-27T13:54:50.285057+09:00[Asia/Tokyo]: Retry 'retry', waiting PT3S until attempt '1'. Last attempt failed with exception 'null'.
2021-09-27 13:54:53:297 INFO RetrySample - Executing request : https://httpbin.org/status/500
2021-09-27 13:54:53:486 INFO RetrySample - onRetry : 2021-09-27T13:54:53.486279+09:00[Asia/Tokyo]: Retry 'retry', waiting PT6S until attempt '2'. Last attempt failed with exception 'null'.
2021-09-27 13:54:59:488 INFO RetrySample - Executing request : https://httpbin.org/status/500
2021-09-27 13:54:59:679 INFO RetrySample - onRetry : 2021-09-27T13:54:59.678930+09:00[Asia/Tokyo]: Retry 'retry', waiting PT12S until attempt '3'. Last attempt failed with exception 'null'.
2021-09-27 13:55:11:682 INFO RetrySample - Executing request : https://httpbin.org/status/500
2021-09-27 13:55:11:900 INFO RetrySample - onError : 2021-09-27T13:55:11.900590+09:00[Asia/Tokyo]: Retry 'retry' recorded a failed retry attempt. Number of retry attempts: '4'. Giving up. Last exception was: 'io.github.resilience4j.retry.MaxRetriesExceeded: max retries is reached out for the result predicate check'.
```
### Pattern where `HttpTimeoutException` is thrown

Although `HttpTimeoutException` is thrown, it is a retry target, so it retries and eventually fails.

```log
2021-09-27 13:55:11:907 INFO RetrySample - Executing request : https://httpbin.org/delay/10
2021-09-27 13:55:16:918 INFO RetrySample - onRetry : 2021-09-27T13:55:16.916947+09:00[Asia/Tokyo]: Retry 'retry', waiting PT3S until attempt '1'. Last attempt failed with exception 'java.net.http.HttpTimeoutException: request timed out'.
2021-09-27 13:55:19:923 INFO RetrySample - Executing request : https://httpbin.org/delay/10
2021-09-27 13:55:24:934 INFO RetrySample - onRetry : 2021-09-27T13:55:24.933226+09:00[Asia/Tokyo]: Retry 'retry', waiting PT6S until attempt '2'. Last attempt failed with exception 'java.net.http.HttpTimeoutException: request timed out'.
2021-09-27 13:55:30:939 INFO RetrySample - Executing request : https://httpbin.org/delay/10
2021-09-27 13:55:35:946 INFO RetrySample - onRetry : 2021-09-27T13:55:35.946108+09:00[Asia/Tokyo]: Retry 'retry', waiting PT12S until attempt '3'. Last attempt failed with exception 'java.net.http.HttpTimeoutException: request timed out'.
2021-09-27 13:55:47:949 INFO RetrySample - Executing request : https://httpbin.org/delay/10
2021-09-27 13:55:52:962 INFO RetrySample - onError : 2021-09-27T13:55:52.962079+09:00[Asia/Tokyo]: Retry 'retry' recorded a failed retry attempt. Number of retry attempts: '4'. Giving up. Last exception was: 'java.net.http.HttpTimeoutException: request timed out'.
2021-09-27 13:55:52:966 INFO App - end
```

## Key Points

The retry configuration is done using the `RetryConfig` class. The configuration is set using the builder pattern. For details, please refer to the reference from the links provided. In this sample, the number of retries and other settings are directly written in the program. Avoid such implementations in production code (e.g., read from environment variables or configuration files).

```java
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(MAX_ATTEMPT_COUNT)
            .retryOnResult(response -> ((HttpResponse<?>) response).statusCode() == 500)
            .retryExceptions(IOException.class, TimeoutException.class)
            .failAfterMaxAttempts(true)
            .intervalFunction(
                IntervalFunction
                    .ofExponentialBackoff(Duration.ofSeconds(RETRY_INTERVAL), 2d))
            .build();
```
You can receive events from the event publisher for retries, exceptions, and successes.

```java
        retry.getEventPublisher()
            .onRetry(event -> logger.info("onRetry : {}", event.toString()))
            .onError(event -> logger.info("onError : {}", event.toString()))
            .onSuccess(event -> logger.info("onSuccess : {}", event.toString()));
```

The logic you want to retry should be written as a callback using interfaces such as `Callable`, `Supplier`, or `Runnable`.


```java
            HttpResponse<String> response = retry.executeCallable(new Callable<HttpResponse<String>>() {
                public java.net.http.HttpResponse<String> call() throws Exception {
                    logger.info("Executing request : {} " ,request.uri());
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    return response;
                }
            }
```

## References

* [resilience4j/resilience4j: Resilience4j is a fault tolerance library designed for Java8 and functional programming](https://github.com/resilience4j/resilience4j)
* [Resilience4j Retry](https://resilience4j.readme.io/docs/retry)

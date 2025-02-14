
This is a retry sample using Apache HttpComponents.

## Overview

This sample demonstrates how to retry HTTP requests using the features of Apache HttpComponents. It uses a web service that returns arbitrary status codes as the external service to be called.

You can check two patterns:

1. The external service returns `200`. It finishes without retrying.
2. The external service returns `500`. Since `500` is a status code that should be retried, it attempts to retry.

## Prerequisites

- Java 17 or later
- Maven 3.6 or later

## Dependencies

It uses Apache HttpComponents 5.

```
        <dependency>
            <groupId>org.apache.httpcomponents.client5</groupId>
            <artifactId>httpclient5</artifactId>
            <version>5.1</version>
        </dependency>
```

## Build and Execution

Build with the following command:

```sh
mvn clean package
```

Run with the following command:

```sh
mvn exec:java 
```

You can also build and run from IDEs like Visual Studio Code or Eclipse.

## Execution Results

### Pattern where the external service returns 200

Since a normal status is returned, it finishes without retrying.

```log
2021-09-27 11:58:03:199 INFO App - httpcomponets sample start
2021-09-27 11:58:03:719 INFO RetrySample - Executing request GET https://httpbin.org/status/200
2021-09-27 11:58:04:908 INFO RetrySample - success
```

### Pattern where the external service returns 500

Since a status (`500`) that should be retried is returned, it retries up to the maximum number of attempts and eventually fails.

```log
2021-09-27 11:58:04:946 INFO RetrySample - Executing request GET https://httpbin.org/status/500
2021-09-27 11:58:05:728 INFO RetrySample - should be retry. execute count : 1 , statu code : 500
2021-09-27 11:58:08:916 INFO RetrySample - should be retry. execute count : 2 , statu code : 500
2021-09-27 11:58:15:101 INFO RetrySample - should be retry. execute count : 3 , statu code : 500
2021-09-27 11:58:24:301 INFO RetrySample - should be retry. execute count : 4 , statu code : 500
2021-09-27 11:58:36:487 INFO RetrySample - should be retry. execute count : 5 , statu code : 500
2021-09-27 11:58:51:671 INFO RetrySample - Number of retries exceeded or response code does not match retry status code :  6
```

## Points

The behavior during retries is implemented in `MyRetryStrategy`.

The number of retries and other settings are directly written in the program. Avoid such implementations in production code (e.g., read from environment variables or configuration files).

```java
        private static int MAX_RETRY_COUNT = 5; // Number of retries
        private static int RETRY_INTERVAL = 3; // Retry interval
```

The conditions for retrying are implemented in the `retryRequest` method. Status codes in the `200` range are considered successful, while `500`, `503`, and `429` are considered for retrying. Other status codes are considered failures.

```java
        /**
         * Determines the conditions for retrying.
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
```

The `retryRequest` method that receives exceptions determines the exceptions that should be retried.

```java
        /**
         * Determines the exceptions that should be retried.
         */
        @Override
        public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
            logger.info("retry with exception");
            if (execCount <= MAX_RETRY_COUNT) {
                return exception instanceof SocketTimeoutException;
            }
            return false;
        }
```
Calculates the retry interval. In the following example, it increases linearly with the number of executions.

```java
        
        /**
         * Calculates the retry interval.
         */
        @Override
        public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
            // Incremental interval
            return TimeValue.ofSeconds(RETRY_INTERVAL * execCount);
        }
```

## References

* [Apache HttpComponents – Apache HttpComponents](https://hc.apache.org/)


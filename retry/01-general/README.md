# Retry Sample Implemented Independently

This is a sample implemented to understand retry logic. In practice, consider using libraries or frameworks.

## Overview

This sample is an example of implementing retry without using libraries or frameworks to make it easier to understand the internal logic of retry. It uses a web service that returns arbitrary status codes as the external source to be called.

You can check four patterns:

1. The external service returns `200`. It ends without retrying.
2. The external service returns `500`. Since `500` is a status code that should be retried, it attempts to retry.
3. The external service returns `429`. Same as above.
4. The external service does not return a response within the specified time, resulting in an `HttpTimeoutException`. Since it is an exception that should be retried, it attempts to retry.

## Prerequisites

- Java 17 or later
- Maven 3.6 or later

## Build and Execution

Build with the following command:

```sh
mvn clean package
```

Execute with the following command:

```sh
mvn exec:java
```

You can also build and execute from IDEs like Visual Studio Code or Eclipse.
## Execution Results

The execution results of this sample are summarized by pattern.

### Pattern where the external service returns 200

Since a normal status is returned, it ends without retrying.

```log
2021-09-22 11:25:54:815 INFO App - main start
2021-09-22 11:25:54:816 INFO App - *** success ***
2021-09-22 11:25:55:603 INFO RetrySample - retry count : 0
2021-09-22 11:25:56:814 INFO RetrySample - response : 200
```

### Pattern where the external service returns 500

Since a status (`500`) that should be retried is returned, it retries up to the maximum number of attempts and ultimately fails.


```log
2021-09-22 11:25:56:814 INFO App - *** internal server errror ***
2021-09-22 11:25:56:816 INFO RetrySample - retry count : 0
2021-09-22 11:25:57:370 INFO RetrySample - response : 500
2021-09-22 11:25:57:371 INFO RetrySample - waiting....
2021-09-22 11:26:00:383 INFO RetrySample - retry count : 1
2021-09-22 11:26:00:564 INFO RetrySample - response : 500
2021-09-22 11:26:00:564 INFO RetrySample - waiting....
2021-09-22 11:26:03:576 INFO RetrySample - retry count : 2
2021-09-22 11:26:03:754 INFO RetrySample - response : 500 
2021-09-22 11:26:03:755 INFO RetrySample - waiting....
2021-09-22 11:26:06:763 INFO RetrySample - retry count : 3
2021-09-22 11:26:06:942 INFO RetrySample - response : 500 
2021-09-22 11:26:06:943 ERROR RetrySample - Number of retries exceeded
```

### Pattern where the external service returns 429. Same as 500

It is the same as `500`.

```log
2021-09-22 11:26:06:943 INFO App - *** too many request ***
2021-09-22 11:26:06:946 INFO RetrySample - retry count : 0
2021-09-22 11:26:07:500 INFO RetrySample - response : 429 
2021-09-22 11:26:07:501 INFO RetrySample - waiting....
2021-09-22 11:26:10:505 INFO RetrySample - retry count : 1
2021-09-22 11:26:10:688 INFO RetrySample - response : 429 
2021-09-22 11:26:10:688 INFO RetrySample - waiting....
2021-09-22 11:26:13:701 INFO RetrySample - retry count : 2
2021-09-22 11:26:13:881 INFO RetrySample - response : 429 
2021-09-22 11:26:13:882 INFO RetrySample - waiting....
2021-09-22 11:26:16:894 INFO RetrySample - retry count : 3
2021-09-22 11:26:17:076 INFO RetrySample - response : 429 
2021-09-22 11:26:17:076 ERROR RetrySample - Number of retries exceeded
```

### Pattern where `HttpTimeoutException` is thrown

Although `HttpTimeoutException` is thrown, it is subject to retry, so it retries and ultimately fails.

```log
2021-09-22 11:26:17:077 INFO App - *** timeout ***
2021-09-22 11:26:17:079 INFO RetrySample - retry count : 0
2021-09-22 11:26:22:082 WARN RetrySample - HttpTimeoutException
2021-09-22 11:26:22:083 INFO RetrySample - waiting....
2021-09-22 11:26:25:098 INFO RetrySample - retry count : 1
2021-09-22 11:26:30:100 WARN RetrySample - HttpTimeoutException
2021-09-22 11:26:30:101 INFO RetrySample - waiting....
2021-09-22 11:26:33:114 INFO RetrySample - retry count : 2
2021-09-22 11:26:38:124 WARN RetrySample - HttpTimeoutException
2021-09-22 11:26:38:125 INFO RetrySample - waiting....
2021-09-22 11:26:41:131 INFO RetrySample - retry count : 3
2021-09-22 11:26:46:146 WARN RetrySample - HttpTimeoutException
2021-09-22 11:26:46:146 ERROR RetrySample - Number of retries exceeded
2021-09-22 11:26:46:147 INFO App - main end
```
## Points

The number of retries is declared as a constant. Changing it will alter the number of retries.

```java
    private static final int MAX_RETRY_COUNT = 3;
```

Determination of retries based on status codes. Status codes in the `200` range are considered successful, while `500`, `503`, and `429` are subject to retries. Other status codes are considered failures.

```java
                if (code >= 200 && code <= 299) {
                    return true;
                }
                // check if the status code is 500, 503, or 429
                if ((code == 500 || code == 503 || code == 429) == false) {
                    return false;
                }
```
Determination of exceptions to retry. `HttpTimeoutException` is subject to retry. Other exceptions are considered failures.

```java
            } catch (HttpTimeoutException e) {
                // Timeout exceptions are subject to retry
                logger.warn("HttpTimeoutException");
            } catch (IOException | InterruptedException e) {
                // Other exceptions are considered failures
                logger.error("Exception", e);
                return false;
            }
```

The retry interval is fixed. Changing it to `retryCount * RETRY_INTERVAL` will gradually extend the interval.

```java
    private static final int RETRY_INTERVAL  = 3;

    ...

            try {
                logger.info("waiting....");
                TimeUnit.SECONDS.sleep(RETRY_INTERVAL);
            } catch (InterruptedException e) {
                return false;
            }
```

# Circuit Breaker Sample with Resilience4j

This is a sample of a circuit breaker using Resilience4j.

## Overview

This sample uses the retry component of the Resilience4j library. Each parameter is defined as follows:

| Item  | Value  |
|---|---|
| Evaluation Method  | Count-based |
| Sliding Window | 10 |
| Failure Rate | 30% |
| Number of allowed calls during HALF OPEN | 10 |
| Recorded Exceptions | IOException, RuntimeException |
| Transition from OPEN to HALF OPEN | Automatic |
| Waiting time from OPEN to HALF OPEN | 5 seconds |

<br>

> :warning: **This sample sets smaller values to understand the behavior of the circuit breaker. For default values, please refer to the Resilience4j reference.**

## Prerequisites

- Java 17 or later
- Maven 3.6 or later

## Dependencies

Using the Resilience4j circuit breaker library.

```xml
    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-circuitbreaker</artifactId>
      <version>1.7.1</version>
    </dependency>
```

## Build and Run

Following command is used to build.

```sh
mvn clean pakcage
```

Execute with the following command.

```sh
mvn exec:java 
```

You can also build and run from IDEs such as Visual Studio Code or Eclipse.

## Execution Results

### CLOSED -> OPEN

This is an example of transitioning from the initial state (CLOSED) to OPEN. If the failure rate of the 10 sliding windows exceeds 30% (3 times in this example), it transitions to OPEN. However, it will not be evaluated unless there are at least the number of calls in the sliding window. The external service call is set to fail once every three times, and after the 10th call, the failure rate is evaluated, and it transitions to OPEN. After transitioning to OPEN, external service calls are blocked.

```log
2021-10-11 11:30:56:556 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = Success
2021-10-11 11:30:56:567 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = false, cause = failure
2021-10-11 11:30:56:568 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = Success
2021-10-11 11:30:56:570 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = Success
2021-10-11 11:30:56:572 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = false, cause = failure
2021-10-11 11:30:56:574 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = Success
2021-10-11 11:30:56:576 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = Success
2021-10-11 11:30:56:577 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = false, cause = failure
2021-10-11 11:30:56:578 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = Success
2021-10-11 11:30:56:605 INFO CircuitBreakerSample - state = CLOSED -> OPEN, success = true, result = Success
2021-10-11 11:30:56:610 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:612 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:613 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:615 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:617 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:619 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:622 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:624 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:626 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:627 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
```

### OPEN -> HALF OPEN と HALF OPEN -> OPEN

Wait more than 5 seconds to transition to HALF OPEN state. The previous final state was OPEN, but after more than 5 seconds, it transitions to HALF OPEN. During HALF OPEN, if 5 external service calls fail, it transitions back to OPEN.
```log
2021-10-11 11:30:56:629 INFO CircuitBreakerSample - ---------------- waiting ----------------------
2021-10-11 11:31:02:631 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = false, cause = failure
2021-10-11 11:31:02:632 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = false, cause = failure
2021-10-11 11:31:02:633 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = false, cause = failure
2021-10-11 11:31:02:634 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = false, cause = failure
2021-10-11 11:31:02:636 INFO CircuitBreakerSample - state = HALF_OPEN -> OPEN, success = false, cause = failure
2021-10-11 11:31:02:637 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:31:02:639 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:31:02:643 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:31:02:646 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:31:02:651 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
```

Wait more than 5 seconds again to transition to HALF OPEN state. If the failure rate during HALF OPEN is below the threshold, it transitions to CLOSED.

```log
2021-10-11 11:31:02:655 INFO CircuitBreakerSample - ---------------- waiting ----------------------
2021-10-11 11:31:08:657 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = false, cause = failure
2021-10-11 11:31:08:658 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = true, result = Success
2021-10-11 11:31:08:661 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = true, result = Success
2021-10-11 11:31:08:662 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = true, result = Success
2021-10-11 11:31:08:664 INFO CircuitBreakerSample - state = HALF_OPEN -> CLOSED, success = true, result = Success
2021-10-11 11:31:08:666 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = false, cause = failure
2021-10-11 11:31:08:668 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = Success
2021-10-11 11:31:08:669 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = Success
2021-10-11 11:31:08:672 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = Success
2021-10-11 11:31:08:674 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = Success
```

## Points

The circuit breaker configuration is done in the `CircuitBreakerConfig` class. For details and default values, please refer to the reference from the link.

```java
        CircuitBreakerConfig config = CircuitBreakerConfig
            .custom()
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10)
            .failureRateThreshold(30)
            .permittedNumberOfCallsInHalfOpenState(5)
            .recordExceptions(IOException.class, RuntimeException.class)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .waitDurationInOpenState(Duration.ofSeconds(5))
            .build();
```

The adjustment of the failure rate is done using the modulo operation of the counter. Please try adjusting the configuration values and failure rate.

```java
        // If it fails 3 times out of 10, it transitions to OPEN state, and the Action will not be called thereafter
        for (int i = 0; i < 20; i++) {
            // Throw an exception every 3 times
            boolean throwException = i % 3 == 1;
            invoke(decorateFunction, throwException);
        }

        // Wait more than 5 seconds until it becomes HALF OPEN
        logger.info("---------------- waiting ----------------------");
        sleep(Duration.ofSeconds(6));
```

## Reference Links

* [CircuitBreaker](https://resilience4j.readme.io/docs/circuitbreaker)

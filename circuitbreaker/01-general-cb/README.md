# Circuit Breaker Sample Custom Implementation
 
This is a sample implementation of a circuit breaker logic for understanding purposes. Please consider using libraries or frameworks in actual use.

## Overview
 
This sample is an example of implementing a circuit breaker without using libraries or frameworks to easily understand the behavior of a circuit breaker.

The following state transitions can be confirmed:

1. CLOSED -> OPEN
1. OPEN -> HALF OPEN
1. HALF OPEN -> OPEN
1. HALF OPEN -> CLOSED

In this sample, each state transitions under the following conditions.

**CLOSE -> OPEN**

Based on count, it transitions to OPEN if it fails the specified number of times (5 times in this example).

**OPEN -> HALF OPEN**

Based on time elapsed, it transitions to HALF OPEN after the specified number of seconds (10 seconds in this example).

**HALF OPEN -> OPEN**

During HALF OPEN, if it fails once, it transitions back to OPEN. In this sample implementation, this cannot be changed.

**HALF OPEN -> CLOSED**

Based on count, it transitions to CLOSED if it succeeds the specified number of times (3 times in this example).

:warning: This sample is an implementation for understanding the behavior of the circuit breaker. For example, considerations such as multithreading are not taken into account, so please refrain from applying this sample to production code.

## Prerequisites
 
- Java 17 or later
- Maven 3.6 or later
- Build and Run
 
Build with the following command.

```sh
mvn clean package  
 ```

Run with the following command.

```sh
mvn exec:java
```
 
It can also be built and run from an IDE such as Visual Studio Code or Eclipse.

## Execution Result

...

## Points
 
The threshold for state transition is defined in the constructor. By changing it, you can change the behavior of the circuit breaker.

```java
        // Create a circuit breaker. It becomes OPEN if it fails 5 times, and transitions from OPEN to HALF_OPEN after 5 seconds.  
        int failureThreshold = 5;  
        int halfOpenSuccessThreshold = 3;  
        int openToHalfOpenWaitSecond = 5;  
        CircuitBreaker circuitBreaker = new CircuitBreaker("test",  
            failureThreshold,  
            halfOpenSuccessThreshold,  
            openToHalfOpenWaitSecond);  
```

The action, which is a stand-in for an external service, is implemented to forcibly throw an exception (fail) according to the given argument.

```java
public class MyAction implements Action<Boolean> {  
  
    private static Logger logger = LoggerFactory.getLogger(MyAction.class);  
  
    @Override  
    public void run(Boolean throwException) throws Exception {  
        if (throwException) {  
            logger.info("Invocation failure");  
            throw new IOException("An I/O error has occurred");  
        }  
        logger.info("Invocation successful");  
        return;  
    }  
}  
```


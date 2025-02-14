# Spring Boot + Spring Cloud CircuitBreaker Resilience4j

This is a sample of a circuit breaker combining Spring Boot and Spring Cloud Circuit Breaker with Resilience4j.

## Overview

We use the circuit breaker functionality provided by Spring Cloud Circuit Breaker. While several implementations can be chosen, this sample uses Resilience4j.

## Prerequisites

- Java 17 or later
- Maven 3.6 or later
- `curl` is used to call HTTP endpoints.

## Dependencies

In addition to `spring-boot-starter-web`, the following libraries are required. For details, please refer to `pom.xml`.

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
        </dependency>
        <dependency>
```

## Build and Run

Build with the following command:

```sh
mvn clean package 
```
Start the Spring Boot application with the following command:

```sh
mvn spring-boot:run
```

The application will start with logs similar to the following:

```log
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.5)

2021-10-11 14:03:12.082  INFO 22471 --- [           main] o.p.w.s.cb.sb.r4j.SampleApplication      : Starting SampleApplication using Java 17.0.11 on NICKEL with PID 22471 (/work/waf-java-samples/circuitbreaker/04-springboot-cb-r4j/target/classes started by moris in /work/waf-java-samples/circuitbreaker/04-springboot-cb-r4j)
2021-10-11 14:03:12.090  INFO 22471 --- [           main] o.p.w.s.cb.sb.r4j.SampleApplication      : No active profile set, falling back to default profiles: default
2021-10-11 14:03:14.859  INFO 22471 --- [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=dc13a1f0-731b-3a8e-9bc3-ef28ee6e6c54
2021-10-11 14:03:16.618  INFO 22471 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2021-10-11 14:03:16.647  INFO 22471 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-10-11 14:03:16.647  INFO 22471 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.53]
```

**If you want to change the port**, run the following command with the additional argument:


```
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8888
```
## Sample Description

The endpoints `/test1` and `/test2` are defined, and although the implementation methods are different, they behave the same. `/test1` is an example where the circuit breaker is configured programmatically, while `/test2` is configured using the `@CircuitBreaker` annotation.

The service called from the `/test1` endpoint is an example where the circuit breaker is configured programmatically. It is defined in `SampleConfiguration`.


```java
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10)
            .failureRateThreshold(30)
            .permittedNumberOfCallsInHalfOpenState(5)
            .recordExceptions(HttpServerErrorException.class, 
                HttpClientErrorException.TooManyRequests.class)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .waitDurationInOpenState(Duration.ofSeconds(5))
            .build();

        return factory -> factory
            .configure(builder -> builder
                .circuitBreakerConfig(config).build(), "myconfig1");
    }
```

The service called from the `/test2` endpoint is configured using the `@CircuitBreaker` annotation. The configuration values are defined in `application.yml`.

```yml
resilience4j.circuitbreaker:
    instances:
        myconfig2:
            slidingWindowSize: 10
            permittedNumberOfCallsInHalfOpenState: 5
            waitDurationInOpenState: 5000
            failureRateThreshold: 30
            registerHealthIndicator: true
            automatic-transition-from-open-to-half-open-enabled: true
            record-exceptions:
            - org.springframework.web.client.HttpServerErrorException
            - org.springframework.web.client.HttpClientErrorException.TooManyRequests

```
`/test1` and `/test2` are configured to behave almost the same.

## Operating the Application

Run `test.sh`.

When the circuit breaker becomes OPEN, an exception `io.github.resilience4j.circuitbreaker.CallNotPermittedException CircuitBreaker 'myconfig1' is OPEN and does not permit further calls` is thrown, and external service calls are blocked.

### CLOSED -> OPEN

This is an example of transitioning from the initial state (CLOSED) to OPEN. If the failure rate of the 10 sliding windows exceeds 30% (3 times in this example), it transitions to OPEN, but it will not be evaluated unless there are at least the number of calls in the sliding window. The external service call is set to fail once every three times, and after the 10th call, the failure rate is evaluated, and it transitions to OPEN. After transitioning to OPEN, external service calls are blocked.


```log
2021-10-11 14:29:17.224  INFO 25662 --- [nio-8080-exec-1] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:29:17.269  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/500
2021-10-11 14:29:17.849 ERROR 25662 --- [nio-8080-exec-1] o.p.wa.sample.cb.sb.r4j.SampleService    : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 14:29:17.849  INFO 25662 --- [nio-8080-exec-1] o.p.wa.sample.cb.sb.r4j.SampleService    : fallbak
2021-10-11 14:29:17.916  INFO 25662 --- [nio-8080-exec-2] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:29:17.917  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/500
2021-10-11 14:29:18.112 ERROR 25662 --- [nio-8080-exec-2] o.p.wa.sample.cb.sb.r4j.SampleService    : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 14:29:18.112  INFO 25662 --- [nio-8080-exec-2] o.p.wa.sample.cb.sb.r4j.SampleService    : fallbak
2021-10-11 14:29:18.125  INFO 25662 --- [nio-8080-exec-3] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:29:18.125  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/500
2021-10-11 14:29:18.323 ERROR 25662 --- [nio-8080-exec-3] o.p.wa.sample.cb.sb.r4j.SampleService    : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 14:29:18.324  INFO 25662 --- [nio-8080-exec-3] o.p.wa.sample.cb.sb.r4j.SampleService    : fallbak
2021-10-11 14:29:18.337  INFO 25662 --- [nio-8080-exec-4] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:29:18.338  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:29:18.623  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:29:18.652  INFO 25662 --- [nio-8080-exec-5] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:29:18.652  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:29:18.846  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:29:18.857  INFO 25662 --- [nio-8080-exec-6] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:29:18.858  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:29:19.052  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:29:19.066  INFO 25662 --- [nio-8080-exec-7] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:29:19.067  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:29:19.265  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:29:19.281  INFO 25662 --- [nio-8080-exec-8] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:29:19.281  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:29:19.473  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:29:19.489  INFO 25662 --- [nio-8080-exec-9] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:29:19.490  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:29:19.690  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:29:19.700  INFO 25662 --- [io-8080-exec-10] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:29:19.700  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:29:19.895  INFO 25662 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:29:19.921  INFO 25662 --- [nio-8080-exec-1] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:29:19.927 ERROR 25662 --- [nio-8080-exec-1] o.p.wa.sample.cb.sb.r4j.SampleService    : io.github.resilience4j.circuitbreaker.CallNotPermittedException CircuitBreaker 'myconfig1' is OPEN and does not permit further calls
2021-10-11 14:29:19.927  INFO 25662 --- [nio-8080-exec-1] o.p.wa.sample.cb.sb.r4j.SampleService    : fallbak
```

5秒以上待機させると、HALF OPEN 状態になり、外部サービス呼び出しができるようになります。HALF OPEN 中に外部サービス呼び出しを失敗させ、再度 OPEN に遷移します。


```log
2021-10-11 14:37:48.044  INFO 26855 --- [nio-8080-exec-2] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:37:48.046  INFO 26855 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/500
2021-10-11 14:37:48.428 ERROR 26855 --- [nio-8080-exec-2] o.p.wa.sample.cb.sb.r4j.SampleService    : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 14:37:48.429  INFO 26855 --- [nio-8080-exec-2] o.p.wa.sample.cb.sb.r4j.SampleService    : fallbak
2021-10-11 14:37:48.441  INFO 26855 --- [nio-8080-exec-3] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:37:48.441  INFO 26855 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/500
2021-10-11 14:37:48.637 ERROR 26855 --- [nio-8080-exec-3] o.p.wa.sample.cb.sb.r4j.SampleService    : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 14:37:48.638  INFO 26855 --- [nio-8080-exec-3] o.p.wa.sample.cb.sb.r4j.SampleService    : fallbak
2021-10-11 14:37:48.651  INFO 26855 --- [nio-8080-exec-4] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:37:48.651  INFO 26855 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:37:48.843  INFO 26855 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:37:48.852  INFO 26855 --- [nio-8080-exec-5] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:37:48.853  INFO 26855 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:37:49.043  INFO 26855 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:37:49.057  INFO 26855 --- [nio-8080-exec-6] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:37:49.058  INFO 26855 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/500
2021-10-11 14:37:49.255 ERROR 26855 --- [nio-8080-exec-6] o.p.wa.sample.cb.sb.r4j.SampleService    : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 14:37:49.255  INFO 26855 --- [nio-8080-exec-6] o.p.wa.sample.cb.sb.r4j.SampleService    : fallbak
2021-10-11 14:37:49.269  INFO 26855 --- [nio-8080-exec-7] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:37:49.270 ERROR 26855 --- [nio-8080-exec-7] o.p.wa.sample.cb.sb.r4j.SampleService    : io.github.resilience4j.circuitbreaker.CallNotPermittedException CircuitBreaker 'myconfig1' is OPEN and does not permit further calls
2021-10-11 14:37:49.270  INFO 26855 --- [nio-8080-exec-7] o.p.wa.sample.cb.sb.r4j.SampleService    : fallbak
```

再度5秒以上待機させ、HALF OPEN 状態にした後、HALF OPEN 中の失敗率が閾値以下ならば CLOSED に遷移します。

```log
2021-10-11 14:44:19.765  INFO 27061 --- [nio-8080-exec-9] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:44:19.766  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/500
2021-10-11 14:44:20.159 ERROR 27061 --- [nio-8080-exec-9] o.p.wa.sample.cb.sb.r4j.SampleService    : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 14:44:20.159  INFO 27061 --- [nio-8080-exec-9] o.p.wa.sample.cb.sb.r4j.SampleService    : fallbak
2021-10-11 14:44:20.168  INFO 27061 --- [io-8080-exec-10] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:44:20.169  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:44:20.350  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:44:20.364  INFO 27061 --- [nio-8080-exec-1] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:44:20.365  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:44:20.555  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:44:20.571  INFO 27061 --- [nio-8080-exec-2] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:44:20.571  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:44:20.760  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:44:20.770  INFO 27061 --- [nio-8080-exec-3] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:44:20.770  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:44:20.953  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:44:20.964  INFO 27061 --- [nio-8080-exec-4] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:44:20.964  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:44:21.145  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:44:21.156  INFO 27061 --- [nio-8080-exec-5] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:44:21.156  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:44:21.340  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
2021-10-11 14:44:21.353  INFO 27061 --- [nio-8080-exec-6] o.p.w.sample.cb.sb.r4j.SampleController  : test1
2021-10-11 14:44:21.353  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : request : http://httpbin.org/status/200
2021-10-11 14:44:21.535  INFO 27061 --- [pool-1-thread-1] o.p.wa.sample.cb.sb.r4j.SampleService    : success
```

In this sample, since the internal state cannot be checked, it is judged from the behavior.

## References

* [CircuitBreaker](https://resilience4j.readme.io/docs/circuitbreaker)
* [Spring Cloud Circuit Breaker](https://spring.io/projects/spring-cloud-circuitbreaker)


# Spring Boot + Spring Cloud CircuitBreaker Resilience4j

This is a circuit breaker sample combining Spring Boot and Spring Cloud Circuit Breaker's Spring Retry.

## Overview

It utilizes the circuit breaker functionality provided by Spring Cloud Circuit Breaker. Several implementations can be selected, but this sample uses Resilience4j.

## Prerequisites

## Requirements

- Java 17 or later
- Maven 3.6 or later
- `curl` is used to call HTTP endpoints.

## Dependencies

Besides `spring-boot-starter-web`, the following libraries are required. For details, please refer to `pom.xml`.

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-spring-retry</artifactId>
            <version>2.0.2</version>
        <dependency>
```
## Build and Run

Build with the following command:

```
mvn clean package 
```
Start the Spring Boot application with the following command:

```
mvn spring-boot:run
```

The application will start and the following log will be displayed.


```log
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.5)

2021-10-11 14:03:12.082  INFO 22471 --- [           main] o.p.w.s.cb.sb.r4j.SampleApplication      : Starting SampleApplication using Java 17.0.11 on NICKEL with PID 22471 (/work/waf-java-samples/circuitbreaker/03-springboot-cb/target/classes started by moris in /work/waf-java-samples/circuitbreaker/03-springboot-cb)
2021-10-11 14:03:12.090  INFO 22471 --- [           main] o.p.w.s.cb.sb.r4j.SampleApplication      : No active profile set, falling back to default profiles: default
2021-10-11 14:03:14.859  INFO 22471 --- [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=dc13a1f0-731b-3a8e-9bc3-ef28ee6e6c54
2021-10-11 14:03:16.618  INFO 22471 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2021-10-11 14:03:16.647  INFO 22471 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-10-11 14:03:16.647  INFO 22471 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.53]
```
**If you want to change the port**, run the following command with the argument:

```
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8888
```

## Sample Explanation

The endpoints `/test1` and `/test2` are defined, and although the implementation methods are different, they behave the same. `/test1` is an example where the circuit breaker is configured programmatically, while `/test2` is configured with the `@CircuitBreaker` annotation.

```java
    @Bean
    public Customizer<SpringRetryCircuitBreakerFactory> defaultCustomizer() {

        logger.info("defaultCustomizer");

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(HttpServerErrorException.class, true);
        RetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
        CircuitBreakerRetryPolicy policy = new CircuitBreakerRetryPolicy(retryPolicy);
        policy.setOpenTimeout(5000);
        policy.setResetTimeout(10000);

        return factory -> factory
            .configure(builder -> builder.retryPolicy(policy).build(), "myconfig1");
    }
```
## Application Operation

Run `test1.sh`. To make the application's behavior easier to understand, the debug level of `org.springframework.retry` is set to `TRACE`.

This is an example of transitioning from the initial state (CLOSED) to OPEN. If there are more than 3 failures within 5 seconds as configured, it will transition to OPEN. In the log of the third call, you can confirm that `Opening circuit` is output. Subsequent calls will be blocked by the circuit breaker.


```log
2021-10-11 21:14:38.433  INFO 1462 --- [nio-8080-exec-1] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:38.492 TRACE 1462 --- [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=0, lastException=null, exhausted=false]
2021-10-11 21:14:38.493 TRACE 1462 --- [nio-8080-exec-1] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:38.493 DEBUG 1462 --- [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=0
2021-10-11 21:14:38.498  INFO 1462 --- [nio-8080-exec-1] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:14:39.076 TRACE 1462 --- [nio-8080-exec-1] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:39.076 DEBUG 1462 --- [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1
2021-10-11 21:14:39.076 ERROR 1462 --- [nio-8080-exec-1] org.pnop.sample.waf.cb.sb.SampleService  : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 21:14:39.146  INFO 1462 --- [nio-8080-exec-2] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:39.147 TRACE 1462 --- [nio-8080-exec-2] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=1, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:14:39.148 TRACE 1462 --- [nio-8080-exec-2] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:39.148 DEBUG 1462 --- [nio-8080-exec-2] o.s.retry.support.RetryTemplate          : Retry: count=1
2021-10-11 21:14:39.148  INFO 1462 --- [nio-8080-exec-2] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:14:39.363 TRACE 1462 --- [nio-8080-exec-2] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:39.363 DEBUG 1462 --- [nio-8080-exec-2] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2
2021-10-11 21:14:39.363 ERROR 1462 --- [nio-8080-exec-2] org.pnop.sample.waf.cb.sb.SampleService  : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 21:14:39.375  INFO 1462 --- [nio-8080-exec-3] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:39.376 TRACE 1462 --- [nio-8080-exec-3] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=2, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:14:39.376 TRACE 1462 --- [nio-8080-exec-3] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:39.376 DEBUG 1462 --- [nio-8080-exec-3] o.s.retry.support.RetryTemplate          : Retry: count=2
2021-10-11 21:14:39.376  INFO 1462 --- [nio-8080-exec-3] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:14:39.570 TRACE 1462 --- [nio-8080-exec-3] o.s.r.policy.CircuitBreakerRetryPolicy   : Opening circuit
2021-10-11 21:14:39.570 TRACE 1462 --- [nio-8080-exec-3] o.s.r.policy.CircuitBreakerRetryPolicy   : Opening circuit
2021-10-11 21:14:39.570 DEBUG 1462 --- [nio-8080-exec-3] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=3
2021-10-11 21:14:39.571 ERROR 1462 --- [nio-8080-exec-3] org.pnop.sample.waf.cb.sb.SampleService  : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 21:14:39.583  INFO 1462 --- [nio-8080-exec-4] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:39.584 TRACE 1462 --- [nio-8080-exec-4] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=3, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:14:39.584 ERROR 1462 --- [nio-8080-exec-4] org.pnop.sample.waf.cb.sb.SampleService  : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 21:14:39.595  INFO 1462 --- [nio-8080-exec-5] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:39.595 TRACE 1462 --- [nio-8080-exec-5] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=3, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:14:39.595 ERROR 1462 --- [nio-8080-exec-5] org.pnop.sample.waf.cb.sb.SampleService  : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
```

After a certain period (10 seconds in the configuration), the settings will be reset and transition to CLOSED, but if the threshold is exceeded, it will transition to OPEN again.

```log
2021-10-11 21:14:51.606  INFO 1462 --- [nio-8080-exec-6] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:51.607 TRACE 1462 --- [nio-8080-exec-6] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=3, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:14:51.607 TRACE 1462 --- [nio-8080-exec-6] o.s.r.policy.CircuitBreakerRetryPolicy   : Closing
2021-10-11 21:14:51.607 TRACE 1462 --- [nio-8080-exec-6] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:51.607 TRACE 1462 --- [nio-8080-exec-6] o.s.r.policy.CircuitBreakerRetryPolicy   : Resetting context
2021-10-11 21:14:51.607 TRACE 1462 --- [nio-8080-exec-6] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:51.607 DEBUG 1462 --- [nio-8080-exec-6] o.s.retry.support.RetryTemplate          : Retry: count=0
2021-10-11 21:14:51.607  INFO 1462 --- [nio-8080-exec-6] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/200
2021-10-11 21:14:52.811  INFO 1462 --- [nio-8080-exec-1] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:52.811 TRACE 1462 --- [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=0, lastException=null, exhausted=false]
2021-10-11 21:14:52.812 TRACE 1462 --- [nio-8080-exec-1] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:52.812 DEBUG 1462 --- [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Retry: count=0
2021-10-11 21:14:52.812  INFO 1462 --- [nio-8080-exec-1] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:14:53.004 TRACE 1462 --- [nio-8080-exec-1] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:53.005 DEBUG 1462 --- [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1
2021-10-11 21:14:53.005 ERROR 1462 --- [nio-8080-exec-1] org.pnop.sample.waf.cb.sb.SampleService  : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 21:14:53.017  INFO 1462 --- [nio-8080-exec-2] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:53.018 TRACE 1462 --- [nio-8080-exec-2] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=1, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:14:53.018 TRACE 1462 --- [nio-8080-exec-2] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:53.018 DEBUG 1462 --- [nio-8080-exec-2] o.s.retry.support.RetryTemplate          : Retry: count=1
2021-10-11 21:14:53.018  INFO 1462 --- [nio-8080-exec-2] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:14:53.213 TRACE 1462 --- [nio-8080-exec-2] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:53.214 DEBUG 1462 --- [nio-8080-exec-2] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2
2021-10-11 21:14:53.214 ERROR 1462 --- [nio-8080-exec-2] org.pnop.sample.waf.cb.sb.SampleService  : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 21:14:53.225  INFO 1462 --- [nio-8080-exec-3] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:53.226 TRACE 1462 --- [nio-8080-exec-3] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=2, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:14:53.226 TRACE 1462 --- [nio-8080-exec-3] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:14:53.226 DEBUG 1462 --- [nio-8080-exec-3] o.s.retry.support.RetryTemplate          : Retry: count=2
2021-10-11 21:14:53.226  INFO 1462 --- [nio-8080-exec-3] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:14:53.420 TRACE 1462 --- [nio-8080-exec-3] o.s.r.policy.CircuitBreakerRetryPolicy   : Opening circuit
2021-10-11 21:14:53.421 TRACE 1462 --- [nio-8080-exec-3] o.s.r.policy.CircuitBreakerRetryPolicy   : Opening circuit
2021-10-11 21:14:53.421 DEBUG 1462 --- [nio-8080-exec-3] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=3
2021-10-11 21:14:53.421 ERROR 1462 --- [nio-8080-exec-3] org.pnop.sample.waf.cb.sb.SampleService  : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 21:14:53.433  INFO 1462 --- [nio-8080-exec-4] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:53.434 TRACE 1462 --- [nio-8080-exec-4] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=3, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:14:53.434 ERROR 1462 --- [nio-8080-exec-4] org.pnop.sample.waf.cb.sb.SampleService  : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 21:14:53.445  INFO 1462 --- [nio-8080-exec-5] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:53.445 TRACE 1462 --- [nio-8080-exec-5] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=3, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:14:53.445 ERROR 1462 --- [nio-8080-exec-5] org.pnop.sample.waf.cb.sb.SampleService  : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
2021-10-11 21:14:53.456  INFO 1462 --- [nio-8080-exec-6] o.p.sample.waf.cb.sb.SampleController    : test1
2021-10-11 21:14:53.457 TRACE 1462 --- [nio-8080-exec-6] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=3, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:14:53.457 ERROR 1462 --- [nio-8080-exec-6] org.pnop.sample.waf.cb.sb.SampleService  : org.springframework.web.client.HttpServerErrorException$InternalServerError 500 INTERNAL SERVER ERROR: [no body]
```
Some redundant parts of the log output have been omitted.

`test2` behaves similarly, but the logs are slightly different.

```log
2021-10-11 21:20:43.246  INFO 1462 --- [nio-8080-exec-7] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:43.281 DEBUG 1462 --- [nio-8080-exec-7] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(2c091021)
2021-10-11 21:20:43.291 TRACE 1462 --- [nio-8080-exec-7] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=0, lastException=null, exhausted=false]
2021-10-11 21:20:43.291 TRACE 1462 --- [nio-8080-exec-7] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:43.291 DEBUG 1462 --- [nio-8080-exec-7] o.s.retry.support.RetryTemplate          : Retry: count=0
2021-10-11 21:20:43.292  INFO 1462 --- [nio-8080-exec-7] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:20:43.738 TRACE 1462 --- [nio-8080-exec-7] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:43.738 DEBUG 1462 --- [nio-8080-exec-7] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1
2021-10-11 21:20:43.738 ERROR 1462 --- [nio-8080-exec-7] org.pnop.sample.waf.cb.sb.SampleService  : Fallback for call invoked
2021-10-11 21:20:43.738 DEBUG 1462 --- [nio-8080-exec-7] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (fallback)
2021-10-11 21:20:43.749  INFO 1462 --- [nio-8080-exec-8] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:43.749 DEBUG 1462 --- [nio-8080-exec-8] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(19f56f81)
2021-10-11 21:20:43.751 TRACE 1462 --- [nio-8080-exec-8] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=1, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:20:43.751 TRACE 1462 --- [nio-8080-exec-8] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:43.751 DEBUG 1462 --- [nio-8080-exec-8] o.s.retry.support.RetryTemplate          : Retry: count=1
2021-10-11 21:20:43.751  INFO 1462 --- [nio-8080-exec-8] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:20:43.976 TRACE 1462 --- [nio-8080-exec-8] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:43.976 DEBUG 1462 --- [nio-8080-exec-8] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2
2021-10-11 21:20:43.976 ERROR 1462 --- [nio-8080-exec-8] org.pnop.sample.waf.cb.sb.SampleService  : Fallback for call invoked
2021-10-11 21:20:43.976 DEBUG 1462 --- [nio-8080-exec-8] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (fallback)
2021-10-11 21:20:43.987  INFO 1462 --- [nio-8080-exec-9] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:43.987 DEBUG 1462 --- [nio-8080-exec-9] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(41638994)
2021-10-11 21:20:43.987 TRACE 1462 --- [nio-8080-exec-9] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=2, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:20:43.988 TRACE 1462 --- [nio-8080-exec-9] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:43.988 DEBUG 1462 --- [nio-8080-exec-9] o.s.retry.support.RetryTemplate          : Retry: count=2
2021-10-11 21:20:43.988  INFO 1462 --- [nio-8080-exec-9] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:20:44.392 TRACE 1462 --- [nio-8080-exec-9] o.s.r.policy.CircuitBreakerRetryPolicy   : Opening circuit
2021-10-11 21:20:44.392 DEBUG 1462 --- [nio-8080-exec-9] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=3
2021-10-11 21:20:44.392 ERROR 1462 --- [nio-8080-exec-9] org.pnop.sample.waf.cb.sb.SampleService  : Fallback for call invoked
2021-10-11 21:20:44.392 DEBUG 1462 --- [nio-8080-exec-9] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (fallback)
2021-10-11 21:20:44.403  INFO 1462 --- [nio-8080-exec-1] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:44.403 DEBUG 1462 --- [nio-8080-exec-1] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(7a9576ef)
2021-10-11 21:20:44.404 TRACE 1462 --- [nio-8080-exec-1] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=3, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:20:44.404 ERROR 1462 --- [nio-8080-exec-1] org.pnop.sample.waf.cb.sb.SampleService  : Fallback for call invoked
2021-10-11 21:20:44.404 DEBUG 1462 --- [nio-8080-exec-1] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (fallback)
2021-10-11 21:20:44.412  INFO 1462 --- [nio-8080-exec-2] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:44.412 DEBUG 1462 --- [nio-8080-exec-2] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(2b69880c)
2021-10-11 21:20:44.412 TRACE 1462 --- [nio-8080-exec-2] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=3, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:20:44.413 ERROR 1462 --- [nio-8080-exec-2] org.pnop.sample.waf.cb.sb.SampleService  : Fallback for call invoked
2021-10-11 21:20:44.413 DEBUG 1462 --- [nio-8080-exec-2] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (fallback)
2021-10-11 21:20:56.422  INFO 1462 --- [nio-8080-exec-3] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:56.423 DEBUG 1462 --- [nio-8080-exec-3] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(630b66fb)
2021-10-11 21:20:56.423 TRACE 1462 --- [nio-8080-exec-3] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=3, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:20:56.423 TRACE 1462 --- [nio-8080-exec-3] o.s.r.policy.CircuitBreakerRetryPolicy   : Closing
2021-10-11 21:20:56.423 TRACE 1462 --- [nio-8080-exec-3] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:56.423 DEBUG 1462 --- [nio-8080-exec-3] o.s.retry.support.RetryTemplate          : Retry: count=0
2021-10-11 21:20:56.423  INFO 1462 --- [nio-8080-exec-3] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/200
2021-10-11 21:20:56.802  INFO 1462 --- [nio-8080-exec-3] org.pnop.sample.waf.cb.sb.SampleService  : success
2021-10-11 21:20:56.802 DEBUG 1462 --- [nio-8080-exec-3] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (success)
2021-10-11 21:20:56.814  INFO 1462 --- [nio-8080-exec-4] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:56.814 DEBUG 1462 --- [nio-8080-exec-4] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(5c2f4359)
2021-10-11 21:20:56.814 TRACE 1462 --- [nio-8080-exec-4] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=0, lastException=null, exhausted=false]
2021-10-11 21:20:56.814 TRACE 1462 --- [nio-8080-exec-4] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:56.815 DEBUG 1462 --- [nio-8080-exec-4] o.s.retry.support.RetryTemplate          : Retry: count=0
2021-10-11 21:20:56.815  INFO 1462 --- [nio-8080-exec-4] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/200
2021-10-11 21:20:57.006  INFO 1462 --- [nio-8080-exec-4] org.pnop.sample.waf.cb.sb.SampleService  : success
2021-10-11 21:20:57.006 DEBUG 1462 --- [nio-8080-exec-4] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (success)
2021-10-11 21:20:57.018  INFO 1462 --- [nio-8080-exec-5] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:57.018 DEBUG 1462 --- [nio-8080-exec-5] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(12801f15)
2021-10-11 21:20:57.018 TRACE 1462 --- [nio-8080-exec-5] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=0, lastException=null, exhausted=false]
2021-10-11 21:20:57.018 TRACE 1462 --- [nio-8080-exec-5] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:57.018 DEBUG 1462 --- [nio-8080-exec-5] o.s.retry.support.RetryTemplate          : Retry: count=0
2021-10-11 21:20:57.018  INFO 1462 --- [nio-8080-exec-5] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:20:57.208 TRACE 1462 --- [nio-8080-exec-5] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:57.209 DEBUG 1462 --- [nio-8080-exec-5] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=1
2021-10-11 21:20:57.209 ERROR 1462 --- [nio-8080-exec-5] org.pnop.sample.waf.cb.sb.SampleService  : Fallback for call invoked
2021-10-11 21:20:57.209 DEBUG 1462 --- [nio-8080-exec-5] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (fallback)
2021-10-11 21:20:57.220  INFO 1462 --- [nio-8080-exec-6] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:57.220 DEBUG 1462 --- [nio-8080-exec-6] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(2a6747bb)
2021-10-11 21:20:57.221 TRACE 1462 --- [nio-8080-exec-6] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=1, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:20:57.221 TRACE 1462 --- [nio-8080-exec-6] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:57.221 DEBUG 1462 --- [nio-8080-exec-6] o.s.retry.support.RetryTemplate          : Retry: count=1
2021-10-11 21:20:57.221  INFO 1462 --- [nio-8080-exec-6] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:20:57.411 TRACE 1462 --- [nio-8080-exec-6] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:57.411 DEBUG 1462 --- [nio-8080-exec-6] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=2
2021-10-11 21:20:57.411 ERROR 1462 --- [nio-8080-exec-6] org.pnop.sample.waf.cb.sb.SampleService  : Fallback for call invoked
2021-10-11 21:20:57.411 DEBUG 1462 --- [nio-8080-exec-6] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (fallback)
2021-10-11 21:20:57.421  INFO 1462 --- [nio-8080-exec-7] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:57.421 DEBUG 1462 --- [nio-8080-exec-7] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(369e1a42)
2021-10-11 21:20:57.422 TRACE 1462 --- [nio-8080-exec-7] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=2, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:20:57.422 TRACE 1462 --- [nio-8080-exec-7] o.s.r.policy.CircuitBreakerRetryPolicy   : Open: false
2021-10-11 21:20:57.422 DEBUG 1462 --- [nio-8080-exec-7] o.s.retry.support.RetryTemplate          : Retry: count=2
2021-10-11 21:20:57.422  INFO 1462 --- [nio-8080-exec-7] org.pnop.sample.waf.cb.sb.SampleService  : request : http://httpbin.org/status/500
2021-10-11 21:20:57.612 TRACE 1462 --- [nio-8080-exec-7] o.s.r.policy.CircuitBreakerRetryPolicy   : Opening circuit
2021-10-11 21:20:57.613 DEBUG 1462 --- [nio-8080-exec-7] o.s.retry.support.RetryTemplate          : Checking for rethrow: count=3
2021-10-11 21:20:57.613 ERROR 1462 --- [nio-8080-exec-7] org.pnop.sample.waf.cb.sb.SampleService  : Fallback for call invoked
2021-10-11 21:20:57.613 DEBUG 1462 --- [nio-8080-exec-7] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (fallback)
2021-10-11 21:20:57.625  INFO 1462 --- [nio-8080-exec-8] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:57.626 DEBUG 1462 --- [nio-8080-exec-8] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(173292cf)
2021-10-11 21:20:57.626 TRACE 1462 --- [nio-8080-exec-8] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=3, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:20:57.627 ERROR 1462 --- [nio-8080-exec-8] org.pnop.sample.waf.cb.sb.SampleService  : Fallback for call invoked
2021-10-11 21:20:57.627 DEBUG 1462 --- [nio-8080-exec-8] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (fallback)
2021-10-11 21:20:57.639  INFO 1462 --- [nio-8080-exec-9] o.p.sample.waf.cb.sb.SampleController    : test2
2021-10-11 21:20:57.639 DEBUG 1462 --- [nio-8080-exec-9] s.r.i.StatefulRetryOperationsInterceptor : Executing proxied method in stateful retry: public java.lang.String org.pnop.sample.waf.cb.sb.SampleService.call2(int)(4fa47fc8)
2021-10-11 21:20:57.639 TRACE 1462 --- [nio-8080-exec-9] o.s.retry.support.RetryTemplate          : RetryContext retrieved: [RetryContext: count=3, lastException=org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 INTERNAL SERVER ERROR: [no body], exhausted=false]
2021-10-11 21:20:57.640 ERROR 1462 --- [nio-8080-exec-9] org.pnop.sample.waf.cb.sb.SampleService  : Fallback for call invoked
2021-10-11 21:20:57.640 DEBUG 1462 --- [nio-8080-exec-9] s.r.i.StatefulRetryOperationsInterceptor : Exiting proxied method in stateful retry with result: (fallback)
```

## Reference

* [Spring Cloud Circuit Breaker の概要とサポート期間 - リファレンスドキュメント](https://spring.pleiades.io/projects/spring-cloud-circuitbreaker)

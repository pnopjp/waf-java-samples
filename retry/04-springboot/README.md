# Spring Boot + Spring Retry Retry Sample

[日本語|Japanese](./README_ja.md)

This is a retry sample combining Spring Boot and Spring Retry.

## Overview

This is a retry sample combining Spring Boot and Spring Retry. We introduce two types of samples: a declarative usage method with retry annotations and an operation from a program using `RetryTemplate`.

## Prerequisites

- Java 17 or later
- Maven 3.6 or later
- Use `curl` to call the HTTP endpoint.

## Dependent Libraries

In addition to `spring-boot-starter-web`, the following libraries are required. For details, please refer to `pom.xml`.

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.retry</groupId>
            <artifactId>spring-retry</artifactId>
        </dependency>
```

## Building and Running Method

Build with the following command.

```
mvn clean package
```
Start the Spring Boot application with the following command.

```
mvn spring-boot:run
```

The following logs will be displayed and the application will start.

```log
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.4)

2021-09-15 17:54:34.609  INFO 560 --- [           main] c.e.r.springboot.SpringbootApplication   : Starting SpringbootApplication using Java 17.0.11 on NICKEL with PID 560 (/work/waf-java-samples/retry/04-springboot/target/classes started by moris in /work/waf-java-samples/retry/04-springboot)
2021-09-15 17:54:34.614  INFO 560 --- [           main] c.e.r.springboot.SpringbootApplication   : No active profile set, falling back to default profiles: default
2021-09-15 17:54:38.178  INFO 560 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2021-09-15 17:54:38.216  INFO 560 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-09-15 17:54:38.216  INFO 560 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.52]
2021-09-15 17:54:38.431  INFO 560 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2021-09-15 17:54:38.431  INFO 560 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 3731 ms
2021-09-15 17:54:39.749  INFO 560 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-09-15 17:54:39.772  INFO 560 --- [           main] c.e.r.springboot.SpringbootApplication   : Started SpringbootApplication in 6.577 seconds (JVM running for 7.364)
```

**If you want to change the port**, please execute it by adding the following argument to the command line.

```
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8888
```

## Operation to the Application

Call the endpoint and check the retry behavior in the application log.

### Simple Retry

Call `/hello1`.

```sh
curl http://localhost:8080/hello1?name=spring-boot
```

The instructions for the annotation are as follows:
- Maximum of 4 attempts (3 retries)
- `IOException` as the target for retry
- Retry interval is 3 seconds × 2^(retry count)

```java
    @Retryable(
        value = { IOException.class },
        maxAttempts = 4,
        backoff = @Backoff(delay = 3000, multiplier = 2))
```

The log when it fails to the end is as follows, and it is executed 4 times, and the retry intervals are 3 seconds, 6 seconds, 12 seconds, and 24 seconds, respectively.

```log
2021-09-15 20:28:30.132  INFO 1228 --- [nio-8080-exec-3] c.e.retry.springboot.HelloController     : hello1
2021-09-15 20:28:30.132  INFO 1228 --- [nio-8080-exec-3] c.e.r.springboot.services.HelloService   : sayHello
2021-09-15 20:28:33.133  INFO 1228 --- [nio-8080-exec-3] c.e.r.springboot.services.HelloService   : sayHello
2021-09-15 20:28:39.133  INFO 1228 --- [nio-8080-exec-3] c.e.r.springboot.services.HelloService   : sayHello
2021-09-15 20:28:51.134  INFO 1228 --- [nio-8080-exec-3] c.e.r.springboot.services.HelloService   : sayHello
```

There is a method that throws an exception in `HelloService.java`. It throws an exception randomly, but if you want to change the behavior, please change it as appropriate. If you set it to `1.0`, an exception will always be thrown.

```java
    private static void someFunction() throws IOException {
        double r = Math.random();
        if (r < 0.7) {
            throw new IOException("IO Error");
        }
    }
```

### Recovery

Call `/hello2`.

```sh
curl http://localhost:8080/hello2?name=spring-boot
```
As specified in the annotation, the retry interval is executed at equal intervals every 2 seconds, with a maximum of 3 attempts.

```java
    @Retryable(
        value = { IOException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000))
```

You can see from the execution time in the log that it is executed every 2 seconds. Also, if it fails to the end, the method specified with the `@Recover` annotation will be called.

```log
2021-09-15 20:48:39.212  INFO 1393 --- [nio-8080-exec-8] c.e.r.s.s.HelloServiceWithRecover        : sayHello
2021-09-15 20:48:41.212  INFO 1393 --- [nio-8080-exec-8] c.e.r.s.s.HelloServiceWithRecover        : sayHello
2021-09-15 20:48:43.213  INFO 1393 --- [nio-8080-exec-8] c.e.r.s.s.HelloServiceWithRecover        : sayHello
2021-09-15 20:48:43.213  INFO 1393 --- [nio-8080-exec-8] c.e.r.s.s.HelloServiceWithRecover        : recover : spring-boot
```

### Retry Template

Call `/hello3`.

```sh
curl http://localhost:8080/hello3?name=spring-boot
```

Retry using the retry template configured in `RetryTemplateConfig.java`. In addition, we register a custom listener and record logs.
As described in the program, the maximum number of attempts is 3, the initial interval is 2 seconds, and the retry interval is 2×2^(retry count).

```log
2021-09-15 20:53:43.837  INFO 1504 --- [nio-8080-exec-1] c.e.retry.springboot.HelloController     : hello3
2021-09-15 20:53:43.837  INFO 1504 --- [nio-8080-exec-1] c.e.r.s.s.HelloServiceWithRetryTemplate  : sayHello
2021-09-15 20:53:43.858  INFO 1504 --- [nio-8080-exec-1] .RetryTemplateConfig$CustomRetryListener : open
2021-09-15 20:53:43.862  INFO 1504 --- [nio-8080-exec-1] c.e.r.s.s.HelloServiceWithRetryTemplate  : retry count : 0
2021-09-15 20:53:43.863  INFO 1504 --- [nio-8080-exec-1] .RetryTemplateConfig$CustomRetryListener : onError, retry count : 1
2021-09-15 20:53:45.868  INFO 1504 --- [nio-8080-exec-1] c.e.r.s.s.HelloServiceWithRetryTemplate  : retry count : 1
2021-09-15 20:53:45.868  INFO 1504 --- [nio-8080-exec-1] .RetryTemplateConfig$CustomRetryListener : onError, retry count : 2
2021-09-15 20:53:49.869  INFO 1504 --- [nio-8080-exec-1] c.e.r.s.s.HelloServiceWithRetryTemplate  : retry count : 2
2021-09-15 20:53:49.869  INFO 1504 --- [nio-8080-exec-1] .RetryTemplateConfig$CustomRetryListener : onError, retry count : 3
2021-09-15 20:53:49.869  INFO 1504 --- [nio-8080-exec-1] .RetryTemplateConfig$CustomRetryListener : close
```

## About Externalization of Retry Configuration

In the sample explanation, the parameters related to retry are hard-coded, but please avoid such implementation in production code.

Although it is not used in `HelloService`, a sample that takes parameters from outside is implemented.

In the following example, it refers to the value from the property set externally (for example, `application.yml`, environment variables, etc.). In this sample, it is defined in `resources/application.yml`, but this location can also be changed. Since there are various setting methods, please refer to the reference for details.

```java
    @Retryable(
        value = { IOException.class },
        maxAttemptsExpression = "${my.retry.maxAttempts}",
        backoff = @Backoff(delayExpression = "${my.retry.delay}",
                           multiplierExpression= "${my.retry.multiplier}"))
    public String sayHello2(String name) throws IOException {
        logger.info("sayHello2");
        someFunction();
        return String.format("Hello %s !!", name);
    }
```

## References

- [Retry](https://docs.spring.io/spring-batch/docs/current/reference/html/retry.html)
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config)
- [spring-projects/spring-retry](https://github.com/spring-projects/spring-retry)

EOF
# Spring Boot + Spring Cloud CircutBreaker Resilience4j

Spring Boot と Spring Cloud Circuit Breaker の Rejilience4j を組み合わせたサーキットブレーカーサンプルです。

## 概要

Spring Cloud Circuit Breaker によって提供されたサーキットブレーカー機能を利用します。いくつか実装を選択することができますが、本サンプルでは Rejilience4j を利用します。

## 前提条件

- Java 11 以降
- Maven 3.6 以降
- HTTP エンドポイントを呼び出すために、`curl` を利用します。

## 依存ライブラリ

`spring-boot-starter-web` 以外に、以下のライブラリが必要です。詳細は、`pom.xml` を参照してください。

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

## ビルドおよび実行方法

以下のコマンドでビルドします。

```
mvn clean package 
```
以下のコマンドでSpring Boot アプリケーションを起動します。 

```
mvn spring-boot:run
```

以下のようなログが表示され、アプリケーションが起動します。

```log
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.5)

2021-10-11 14:03:12.082  INFO 22471 --- [           main] o.p.w.s.cb.sb.r4j.SampleApplication      : Starting SampleApplication using Java 11.0.11 on NICKEL with PID 22471 (/work/waf-java-samples/circuitbreaker/04-springboot-cb-r4j/target/classes started by moris in /work/waf-java-samples/circuitbreaker/04-springboot-cb-r4j)
2021-10-11 14:03:12.090  INFO 22471 --- [           main] o.p.w.s.cb.sb.r4j.SampleApplication      : No active profile set, falling back to default profiles: default
2021-10-11 14:03:14.859  INFO 22471 --- [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=dc13a1f0-731b-3a8e-9bc3-ef28ee6e6c54
2021-10-11 14:03:16.618  INFO 22471 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2021-10-11 14:03:16.647  INFO 22471 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-10-11 14:03:16.647  INFO 22471 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.53]
```

**ポートを変更したい場合は**、コマンドラインに以下の引数を付加して実行してください。

```
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8888
```

## サンプルの説明

`/test1` と `/test2` のエンドポイントが定義しており、実装方法は異なりました同じ振る舞いをします。`/test1` では、プログラムによってサーキットブレーカーを構成した例、`/test2` は `@CircutBreaker` アノテーションで構成されています。


`/test1` のエンドポンイトから呼び出されるサービスは、プログラムでサーキットブレーカーを構成した例です。 `SampleConfiguration` で定義されています。

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

`/test2` のエンドポイントから呼び出されるサービスは、`@CircuitBreaker` アノテーションで構成されています。構成値は、`application.yml` で定義されています。

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

`/test1` も `/test2` もほぼ同じ振る舞いになるように、構成設定してあします。

## アプリケーションへの操作

`test.sh` を実行します。 

サーキットブレーカー が OPENになると、`io.github.resilience4j.circuitbreaker.CallNotPermittedException CircuitBreaker 'myconfig1' is OPEN and does not permit further calls` の例外がスローされ、外部サービス呼び出しはブロックされます。

### CLOSED -> OPEN 

初期状態（CLOSED） ->　OPEN へ移行する例です。10回のスライディングウィンドウの失敗率が30%を超える（本例だと3回）と OPEN へ遷移しますが、最低でもスライディングウィンドウ数の呼び出しがないと評価されません。外部サービス呼び出しを3回に1回失敗するようにしてあり、10回目の呼び出し後に失敗率が評価され、OPEN に遷移しています。OPEN に遷移後は、外部サービス呼び出しはブロックされます。


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

本サンプルでは、内部の状態を確認できないので、挙動から判断しています。

## 参考リンク

* [CircuitBreaker](https://resilience4j.readme.io/docs/circuitbreaker)
* [Spring Cloud Circuit Breaker とは？ - リファレンスドキュメント](https://spring.pleiades.io/projects/spring-cloud-circuitbreaker)

以上
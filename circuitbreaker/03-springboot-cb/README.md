# Spring Boot + Spring Cloud CircutBreaker Resilience4j

Spring Boot と Spring Cloud Circuit Breaker の Spring Retry を組み合わせたサーキットブレーカーサンプルです。

## 概要

Spring Cloud Circuit Breaker によって提供されたサーキットブレーカー機能を利用します。いくつか実装を選択することができますが、本サンプルでは Rejilience4j を利用します。

## 前提

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
            <artifactId>spring-cloud-starter-circuitbreaker-spring-retry</artifactId>
            <version>2.0.2</version>
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

以下のログが表示され、アプリケーションが起動します。

```log
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.5)

2021-10-11 14:03:12.082  INFO 22471 --- [           main] o.p.w.s.cb.sb.r4j.SampleApplication      : Starting SampleApplication using Java 11.0.11 on NICKEL with PID 22471 (/work/waf-java-samples/circuitbreaker/03-springboot-cb/target/classes started by moris in /work/waf-java-samples/circuitbreaker/03-springboot-cb)
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

## サンプル説明

`/test1` と `/test2` のエンドポイントが定義しており、実装方法は異なりました同じ振る舞いをします。`/test1` では、プログラムによってサーキットブレーカーを構成した例、`/test2` は `@CircutBreaker` アノテーションで構成されています。

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

## アプリケーションへの操作

`test1.sh` を実行します。アプリケーションの動作を分りやすくするために、`org.springframework.retry` のデバッグレベルを `TRACE`にしてあります。

初期状態（CLOSED）-> OPEN へ遷移する例です。設定通り5秒以内に3回以上失敗すると、 OPEN への遷移します。3回目の呼び出しログで、最終的に `Opening circuit` と出力されるのが確認できます。それ移行の呼び出しは、サーキットブレーカーによってブロックされます。

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

一定期間経過すると（設定では10秒）、設定はリセットされ CLOSED に遷移しますが、閾値を超えると再び OPEN へと遷移します。

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

ログ出力の冗長な部分を一部割愛しています。

`test2` も同様の振る舞いをしますが、ログが多少異なります。

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

## 参考リンク

* [Spring Cloud Circuit Breaker とは？ - リファレンスドキュメント](https://spring.pleiades.io/projects/spring-cloud-circuitbreaker)

以上
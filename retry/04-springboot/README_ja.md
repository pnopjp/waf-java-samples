# Spring Boot + Spring Retry のリトライサンプル

Spring Boot と Spring Retry を組み合わせたリトライサンプルです。

## 概要

Spring Boot と Spring Retry を組み合わせたリトライサンプルです。リトライアノテーションで宣言的に利用する方法と、`RetryTemplate` を利用しプログラムから操作する2種類のサンプルを紹介します。

## 前提条件

- Java 17 以降
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
            <groupId>org.springframework.retry</groupId>
            <artifactId>spring-retry</artifactId>
        </dependency>
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

**ポートを変更したい場合は**、コマンドラインに以下の引数を付加して実行してください。

```
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8888
```

## アプリケーションへの操作

エンドポイントを呼び出し、アプリケーションログを見てリトライの挙動を確認します。

### 単純なリトライ

`/hello1` を呼び出します。

```sh
curl http://localhost:8080/hello1?name=spring-boot
```

アノテーションの指示は、以下の通りです。
- 最大試行回数4回（リトライ回数3回）
- `IOException` をリトライ対象
- リトライ間隔 3秒 × 2^（リトライ回数）

```java
    @Retryable(
        value = { IOException.class }, 
        maxAttempts = 4, 
        backoff = @Backoff(delay = 3000, multiplier = 2))
```

最後まで失敗した場合のログは以下の通りで、4回実行され、それそれのリトライ間隔が3秒、6秒、12秒、24秒となります。

```log
2021-09-15 20:28:30.132  INFO 1228 --- [nio-8080-exec-3] c.e.retry.springboot.HelloController     : hello1
2021-09-15 20:28:30.132  INFO 1228 --- [nio-8080-exec-3] c.e.r.springboot.services.HelloService   : sayHello
2021-09-15 20:28:33.133  INFO 1228 --- [nio-8080-exec-3] c.e.r.springboot.services.HelloService   : sayHello
2021-09-15 20:28:39.133  INFO 1228 --- [nio-8080-exec-3] c.e.r.springboot.services.HelloService   : sayHello
2021-09-15 20:28:51.134  INFO 1228 --- [nio-8080-exec-3] c.e.r.springboot.services.HelloService   : sayHello
```

例外をスローするメソッドが `HelloService.java` に存在します。 ランダムで例外をスローしていますが、挙動を変更したしたい場合は適宜変更してください。`1.0` にすれば常に例外がスローされます。 

```java
    private static void someFunction() throws IOException {
        double r = Math.random();
        if (r < 0.7) {
            throw new IOException("IO Error");
        }
    }
```

### リカバリー

`/hello2` を呼び出します。

```sh
curl http://localhost:8080/hello2?name=spring-boot
```
リトライ間隔はアノテーションで指定されたとおり、最大試行回数3回、2秒ごとの等間隔にリトライが実施されます。

```java
    @Retryable(
        value = { IOException.class }, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 2000))
```

ログの実行時間を確認すると2秒ごとに実行されていることが分ります。また最後まで失敗した場合、`@Recover` アノテーションが指定されているメソッドが呼び出されます。

```log
2021-09-15 20:48:39.212  INFO 1393 --- [nio-8080-exec-8] c.e.r.s.s.HelloServiceWithRecover        : sayHello
2021-09-15 20:48:41.212  INFO 1393 --- [nio-8080-exec-8] c.e.r.s.s.HelloServiceWithRecover        : sayHello
2021-09-15 20:48:43.213  INFO 1393 --- [nio-8080-exec-8] c.e.r.s.s.HelloServiceWithRecover        : sayHello
2021-09-15 20:48:43.213  INFO 1393 --- [nio-8080-exec-8] c.e.r.s.s.HelloServiceWithRecover        : recover : spring-boot
```

### リトライテンプレート

`hello3` を呼び出します。

```sh
curl http://localhost:8080/hello3?name=spring-boot
```

`RetryTemplateConfig.java` で構成されたリトライテンプレートを使用してリトライします。それに加えて、カスタムリスナーを登録してログを記録しています。
プログラムの記述通り、最大試行回数3回、初期間隔2秒、リトライ間隔 2×2^(リトライ回数) となります。

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

## リトライ構成の外部化について

サンプル説明では、リトライに関連するパラメータをハードコーディングしてありますが、プロダクションコードではこのような実装は避けてください。

`HelloService` には使用されていませんが、外部からパラメータを与えるサンプルが実装されています。

以下の例では外部に設定されたプロパティ（例えば、`application.yml` 、環境変数など） から値を参照します。本サンプルでは `resoueces/applicaiton.yml` に定義してありますが、これもロケーションの変更は可能です。様々な設定方法があるため詳細はリファレンスを参照してください。

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

# 参考リンク

[Retry](https://docs.spring.io/spring-batch/docs/current/reference/html/retry.html)
[Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config)
[spring-projects/spring-retry](https://github.com/spring-projects/spring-retry)

以上
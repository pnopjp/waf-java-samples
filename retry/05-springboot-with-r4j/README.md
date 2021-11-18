# Spring Boot + Resilience4j のリトライサンプル

Spring Boot と Resilience4j を組み合わせリトライサンプルです。

## 概要

Spring Boot と Resilience4j を組み合わせたリトライサンプルです。リトライアノテーションで宣言的に利用する方法を紹介します。

## 前提条件

- Java 11 以降
- Maven 3.6 以降
- HTTP エンドポイントを呼び出すために、`curl` を利用します。

## 依存ライブラリ

`spring-boot-starter-web` 以外に、以下のライブラリが必要です。

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot2</artifactId>
            <version>1.7.1</version>
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

2021-09-27 15:18:11.884  INFO 10288 --- [           main] o.p.w.s.r.sb.r4j.SpringbootApplication   : Starting SpringbootApplication using Java 11.0.11 on NICKEL with PID 10288 (/work/waf-java-samples/retry/05-springboot-with-r4j/target/classes started by moris in /work/waf-java-samples/retry/05-springboot-with-r4j)
2021-09-27 15:18:11.895  INFO 10288 --- [           main] o.p.w.s.r.sb.r4j.SpringbootApplication   : No active profile set, falling back to default profiles: default
2021-09-27 15:18:17.550  INFO 10288 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2021-09-27 15:18:17.605  INFO 10288 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-09-27 15:18:17.605  INFO 10288 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.52]
2021-09-27 15:18:17.881  INFO 10288 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2021-09-27 15:18:17.881  INFO 10288 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 5741 ms
2021-09-27 15:18:22.062  INFO 10288 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-09-27 15:18:22.095  INFO 10288 --- [           main] o.p.w.s.r.sb.r4j.SpringbootApplication   : Started SpringbootApplication in 12.075 seconds (JVM running for 12.951)
```

**ポートを変更したい場合は**、コマンドラインに以下の引数を付加して実行してください。

```
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8888
```


## アプリケーションへの操作

エンドポイントを呼び出つつ、アプリケーションログを見てリトライの挙動を確認します。

`hello1` を呼び出します。

```sh
curl http://localhost:8080/hello1?name=spring-boot
```

Resilience4j の `Retry` アノテーションでは直接リトライパラメータを指定できずに、プロパティファイル経由となります。

```java
    @Retry(name = "helloService", fallbackMethod = "fallBackMethod")
    public String sayHello(String name) throws IOException {
        logger.info("sayHello");
        someFunction();
        return String.format("Hello %s !!", name);
    }
```

プロパティでの定義は以下の通りです。

- 最大試行回数5回
- リトライ間隔3秒
- `HelloService` をリトライ対象

```yml
resilience4j:
  retry:
    instances:
      helloService:
        max-attempts: 5
        wait-duration: 3s
        retry-exceptions:
          - java.io.IOException

```

最後まで失敗した場合のログは以下の通りで、5回実行され、それそれのリトライ間隔は3秒毎です。

```java
2021-09-27 15:26:59.889  INFO 10288 --- [nio-8080-exec-2] o.p.w.s.r.sb.r4j.services.HelloService   : sayHello
2021-09-27 15:26:59.899  INFO 10288 --- [nio-8080-exec-2] o.p.w.s.r.sb.r4j.services.HelloService   : 2021-09-27T15:26:59.899503+09:00[Asia/Tokyo]: Retry 'helloService', waiting PT3S until attempt '1'. Last attempt failed with exception 'java.io.IOException: IO Error'.
2021-09-27 15:27:02.916  INFO 10288 --- [nio-8080-exec-2] o.p.w.s.r.sb.r4j.services.HelloService   : sayHello
2021-09-27 15:27:02.916  INFO 10288 --- [nio-8080-exec-2] o.p.w.s.r.sb.r4j.services.HelloService   : 2021-09-27T15:27:02.916453+09:00[Asia/Tokyo]: Retry 'helloService', waiting PT3S until attempt '2'. Last attempt failed with exception 'java.io.IOException: IO Error'.
2021-09-27 15:27:05.916  INFO 10288 --- [nio-8080-exec-2] o.p.w.s.r.sb.r4j.services.HelloService   : sayHello
2021-09-27 15:27:05.917  INFO 10288 --- [nio-8080-exec-2] o.p.w.s.r.sb.r4j.services.HelloService   : 2021-09-27T15:27:05.917266+09:00[Asia/Tokyo]: Retry 'helloService', waiting PT3S until attempt '3'. Last attempt failed with exception 'java.io.IOException: IO Error'.
2021-09-27 15:27:08.918  INFO 10288 --- [nio-8080-exec-2] o.p.w.s.r.sb.r4j.services.HelloService   : sayHello
2021-09-27 15:27:08.918  INFO 10288 --- [nio-8080-exec-2] o.p.w.s.r.sb.r4j.services.HelloService   : 2021-09-27T15:27:08.918345+09:00[Asia/Tokyo]: Retry 'helloService', waiting PT3S until attempt '4'. Last attempt failed with exception 'java.io.IOException: IO Error'.
2021-09-27 15:27:11.918  INFO 10288 --- [nio-8080-exec-2] o.p.w.s.r.sb.r4j.services.HelloService   : sayHello
2021-09-27 15:27:11.926  INFO 10288 --- [nio-8080-exec-2] o.p.w.s.r.sb.r4j.services.HelloService   : fallback : spring-boot, IO Error
```

## 参考リンク

* [Resilience4j Retry](https://resilience4j.readme.io/docs/retry)
* [Resilience4j Spring Boot2 Getting Started](https://resilience4j.readme.io/docs/getting-started-3)

以上

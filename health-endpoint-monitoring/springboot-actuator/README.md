# 正常性エンドポイントのサンプル

Spring Boot をベースとした正常性エンドポイントのサンプルです。本サンプルでは、単純にレスポンスを返すサンプルと Spring Boot Actuator を利用したサンプルが実装されています。

## 前提条件

- Java 17 以降
- Maven 3.8 以降
- HTTP エンドポイントを呼び出すために、`curl` を利用します。

## 依存ライブラリ

`spring-boot-starter-web` 以外に、以下のライブラリが必要です。詳細は、`pom.xml` を参照してください。

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
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
 :: Spring Boot ::                (v2.5.5)

2021-10-18 23:01:19.895  INFO 22452 --- [  restartedMain] o.p.waf.sample.act.sb.SampleApplication  : Starting SampleApplication using Java 17.0.11 on NICKEL with PID 22452 (/work/waf-java-samples/health-endpoint-monitoring/springboot-act/target/classes started by moris in /work/waf-java-samples/health-endpoint-monitoring/springboot-act)
2021-10-18 23:01:19.904  INFO 22452 --- [  restartedMain] o.p.waf.sample.act.sb.SampleApplication  : No active profile set, falling back to default profiles: default
2021-10-18 23:01:20.103  INFO 22452 --- [  restartedMain] .e.DevToolsPropertyDefaultsPostProcessor : Devtools property defaults active! Set 'spring.devtools.add-properties' to 'false' to disable
2021-10-18 23:01:20.103  INFO 22452 --- [  restartedMain] .e.DevToolsPropertyDefaultsPostProcessor : For additional web related logging consider setting the 'logging.level.web' property to 'DEBUG'
2021-10-18 23:01:26.739  INFO 22452 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2021-10-18 23:01:26.778  INFO 22452 --- [  restartedMain] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-10-18 23:01:26.778  INFO 22452 --- [  restartedMain] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.53]
2021-10-18 23:01:26.985  INFO 22452 --- [  restartedMain] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2021-10-18 23:01:26.985  INFO 22452 --- [  restartedMain] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 6879 ms
2021-10-18 23:01:29.962  INFO 22452 --- [  restartedMain] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 13 endpoint(s) beneath base path '/actuator'
2021-10-18 23:01:30.069  INFO 22452 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-10-18 23:01:30.111  INFO 22452 --- [  restartedMain] o.p.waf.sample.act.sb.SampleApplication  : Started SampleApplication in 11.969 seconds (JVM running for 12.935)
```

**ポートを変更したい場合は**、コマンドラインに以下の引数を付加して実行してください。

```
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8888
```

## 単純な正常性エンドポイントのサンプル

独自に実装した正常性エンドポイントを呼び出します。実装は `HealthController` クラスです。

`/health/ping` を呼び出します。

```sh
curl http://localhost:8080/health/ping
```

単純なレスポンスが返却されます。監視ツールでは正常に応答したかどうか（HTTPのステータスコードが200か）や、レスポンスの内容が正しいかどうかを判断します。

```json
{
  "date": "2021-10-18T23:07:19.91779+09:00",
  "message": "sucess"
}
```

`/health/4c735208-8bd6-4271-9020-1acbcc79b052` を呼び出します。URLは容易に推測されないようになっています。

```
 curl -i http://localhost:8080/health/4c735208-8bd6-4271-9020-1acbcc79b052
```

またエンドポイント保護のために、リクエストヘッダに特定の値を設定しないとエラーになります。

```log
HTTP/1.1 404
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 0
Date: Mon, 18 Oct 2021 14:12:46 GMT
```

`-H "X-HEALTH-KEY:PASS"` オプションでリクエストヘッダを追加すると、正常なレスポンスを返却します。実際には、`PASS` のような単純な文字列でなく複雑で長い値を利用します。

```
curl -i -H "X-HEALTH-KEY:PASS" http://localhost:8080/health/4c735208-8bd6-4271-9020-1acbcc79b052
```

正常にレスポンスが返却されます。

```
HTTP/1.1 200
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 18 Oct 2021 14:18:57 GMT

{"date":"2021-10-18T23:18:57.840219+09:00","message":"sucess"}%
```

## Spring Actuator を利用したサンプル

`MyApplication`と `MyApplication2` が独自に拡張したインジケーターです。それぞれ、`MyHealthIndicator` と `MyHealthIndicator2` クラスがそれに対応します。

`/actuator/health` を呼び出します。

```sh
curl http://localhost:8080/actuator/health 
```

レスポンスは以下の通りです。

```json
{
  "status": "UP",
  "components": {
    "MyApplication": {
      "status": "UP",
      "details": {
        "Property1": 1234,
        "Property2": "ABCD",
        "Property3": true
      }
    },
    "MyApplication2": {
      "status": "UP",
      "details": {
        "PropertyX": "XYZ",
        "PropertyY": 84803,
        "PropertyZ": 10.012
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 998194016256,
        "free": 163191590912,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

本サンプルではエラーをランダムに発生させています。何度か呼び出しレスポンスの内容を確認してください。エラーが返却されると、全体が`DOWN`状態となります。

```json
{
  "status": "DOWN",
  "components": {
    "MyApplication": {
      "status": "DOWN",
      "details": {
        "Error Code": 1,
        "PropertyA": 99999,
        "PropertyB": false
      }
    },
    "MyApplication2": {
      "status": "DOWN",
      "details": {
        "Error Code": 1,
        "PropertyA": 99999,
        "PropertyB": false
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 998194016256,
        "free": 163201699840,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

`/actuator/health/MyApplication` のような形でのアクセスもできます。

```
curl http://localhost:8080/actuator/health/MyApplication
```

指定されたインジケーターのレスポンスが返却されます。

```json
{
  "status": "UP",
  "details": {
    "Property1": 1234,
    "Property2": "ABCD",
    "Property3": true
  }
}
```

> :exclamation: レスポンスは見やすさのためフォーマットしてあります

以上
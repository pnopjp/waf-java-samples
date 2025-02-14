# Health Endpoint Sample

This is a sample of a health endpoint based on Spring Boot. This sample includes a simple response example and an example using Spring Boot Actuator.

## Prerequisites

- Java 17 or later
- Maven 3.8 or later
- `curl` is used to call HTTP endpoints.

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

## ビルドおよび実行方法

Build with the following command:。

```sh
mvn clean package 
```

Start the Spring Boot application with the following command:

```sh
mvn spring-boot:run
```

The application will start and display logs as shown below.

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

**If you want to change the port**, run the following command with the additional argument:

sh
```
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8888
```
## Simple Health Endpoint Sample

Call the custom implemented health endpoint. The implementation is in the `HealthController` class.

Call `/health/ping`.

```sh
curl http://localhost:8080/health/ping
```
A simple response will be returned. Monitoring tools will determine if the response is successful (HTTP status code 200) and if the content of the response is correct.

```json
{
  "date": "2021-10-18T23:07:19.91779+09:00",
  "message": "sucess"
}
```

Call `/health/4c735208-8bd6-4271-9020-1acbcc79b052`. The URL is designed to be difficult to guess.

```sh
 curl -i http://localhost:8080/health/4c735208-8bd6-4271-9020-1acbcc79b052
```

Additionally, an error will occur if a specific value is not set in the request header to protect the endpoint.

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

Adding the request header with the `-H "X-HEALTH-KEY:PASS"` option will return a normal response. In practice, use a complex and long value instead of a simple string like `PASS`.

```sh
curl -i -H "X-HEALTH-KEY:PASS" http://localhost:8080/health/4c735208-8bd6-4271-9020-1acbcc79b052
```

A normal response will be returned.

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
## Sample Using Spring Actuator

`MyApplication` and `MyApplication2` are custom extended indicators. They correspond to the `MyHealthIndicator` and `MyHealthIndicator2` classes, respectively.

Call `/actuator/health`.


```sh
curl http://localhost:8080/actuator/health 
```

The response is as follows.


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

In this sample, errors are generated randomly. Please check the response content by calling several times. If an error is returned, the overall status will be `DOWN`.

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

You can also access it in the form of `/actuator/health/MyApplication`.

```
curl http://localhost:8080/actuator/health/MyApplication
```

The response of the specified indicator will be returned.

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

> :exclamation: The response is formatted for readability.

以上
# Retry Sample with Spring Boot + Resilience4j  

[日本語|Japanese](./README_ja.md)

   
This is a retry sample combining Spring Boot and Resilience4j.  
   
## Overview  
   
This is a retry sample that combines Spring Boot and Resilience4j. We will introduce a method to use it declaratively with retry annotations.  
   
## Prerequisites  
   
- Java 17 or later  
- Maven 3.6 or later  
- Use `curl` to call the HTTP endpoint.  
   
## Dependency Libraries  
   
In addition to `spring-boot-starter-web`, the following libraries are required.  
   
```xml  
        <dependency>  
            <groupId>org.springframework.boot</groupId>  
            <artifactId>spring-boot-starter-aop</artifactId>  
        </dependency>  
        <dependency>  
            <groupId>io.github.resilience4j</groupId>  
            <artifactId>resilience4j-spring-boot2</artifactId>  
            <version>2.0.2</version>  
        </dependency>  
```

## Build and Execution Method  
   
Build with the following command.  
   
```  
mvn clean package   
```  
Launch the Spring Boot application with the following command.  
   
```  
mvn spring-boot:run  
```  
   
The application starts with logs displayed as follows.


```log
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.4)

2021-09-27 15:18:11.884  INFO 10288 --- [           main] o.p.w.s.r.sb.r4j.SpringbootApplication   : Starting SpringbootApplication using Java 17.0.11 on NICKEL with PID 10288 (/work/waf-java-samples/retry/05-springboot-with-r4j/target/classes started by moris in /work/waf-java-samples/retry/05-springboot-with-r4j)
2021-09-27 15:18:11.895  INFO 10288 --- [           main] o.p.w.s.r.sb.r4j.SpringbootApplication   : No active profile set, falling back to default profiles: default
2021-09-27 15:18:17.550  INFO 10288 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2021-09-27 15:18:17.605  INFO 10288 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-09-27 15:18:17.605  INFO 10288 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.52]
2021-09-27 15:18:17.881  INFO 10288 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2021-09-27 15:18:17.881  INFO 10288 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 5741 ms
2021-09-27 15:18:22.062  INFO 10288 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-09-27 15:18:22.095  INFO 10288 --- [           main] o.p.w.s.r.sb.r4j.SpringbootApplication   : Started SpringbootApplication in 12.075 seconds (JVM running for 12.951)
```

## Operation to the Application  
   
While calling the endpoint, check the retry behavior by looking at the application log.  
   
Call `hello1`.  
   
```sh  
curl http://localhost:8080/hello1?name=spring-boot  
```  
   
With Resilience4j's `Retry` annotation, you cannot directly specify retry parameters; it goes through a property file.  
   
```java  
    @Retry(name = "helloService", fallbackMethod = "fallBackMethod")  
    public String sayHello(String name) throws IOException {  
        logger.info("sayHello");  
        someFunction();  
        return String.format("Hello %s !!", name);  
    }  
```  
   
The definition in the property is as follows:  
   
- Maximum number of attempts: 5  
- Retry interval: 3 seconds  
- `HelloService` is the target for retry  
   
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
   
The log when it fails to the end is as follows, and it runs 5 times, with a retry interval of 3 seconds each time.


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

## References

* [Resilience4j Retry](https://resilience4j.readme.io/docs/retry)
* [Resilience4j Spring Boot2 Getting Started](https://resilience4j.readme.io/docs/getting-started-3)

EOF
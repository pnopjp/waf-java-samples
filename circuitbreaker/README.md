# Circuit Breaker Sample

[日本語|Japanese](./README_ja.md)

This is a collection of circuit breaker samples, part of the cloud design pattern implementation based on the Microsoft Azure Well-Architected Framework (Java/Spring Boot version).

## List of Samples

The executable samples are as follows.

| Sample Name                                              | Description                                                             |
| -------------------------------------------------------- | ----------------------------------------------------------------------- |
| [01-general-b](./01-general-cb/README.md)                | A sample implemented in Java independently                              |
| [02-rejilience4j](./02-resilience4j-cb/README.md)        | Circuit breaker sample using Resilience4J Retry                         |
| [03-springboot-cb](./03-springboot-cb/README.md)         | Sample using Spring Boot + Spring Cloud Circuit Breaker + Spring Retry  |
| [04-springboot-cb-r4j](./04-springboot-cb-r4j/README.md) | Sample using Spring Boot + Spring Cloud Circuit Breaker + Ressilience4j |

## Reference Links

- [Circuit Breaker pattern - Azure Architecture Center | Microsoft Learn](https://learn.microsoft.com/en-us/azure/architecture/patterns/circuit-breaker)
- [Implementing the Circuit Breaker pattern - .NET | Microsoft Learn](https://learn.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/implement-circuit-breaker-pattern)

End of document.
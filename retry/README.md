# Retry Samples  

[日本語|Japanese](./README_ja.md)
   
This is a collection of retry samples for cloud design patterns implementation based on the Microsoft Azure Well-Architected Framework (Java/Spring Boot version).  
   
## Sample List  
   
The following are the executable samples:  
   
| Sample Name                                             | Overview                                                                     |
| ------------------------------------------------------- | ---------------------------------------------------------------------------- |
| [01-general](./01-general/README.md)                    | Sample implemented in Java independently                                     |
| [02-httpcomponents](./02-httpcomponents/README.md)      | HTTP request retry sample using Apache HttpComponents                        |
| [03-Resilience4j](./03-Resilience4J/README.md)          | Retry sample using Resilience4J Retry                                        |
| [04-springboot](./04-springboot/README.md)              | Sample using annotations and retry templates by Spring Retry and Spring Boot |
| [05-springboot-r4j](./05-springboot-with-r4j/README.md) | Sample using annotations and retry templates by Spring Retry and Spring Boot |
   
## Reference Links  
   
* [Retry pattern - Azure Architecture Center | Microsoft Learn](https://learn.microsoft.com/en-us/azure/architecture/patterns/retry)
* [Transient fault handling - Best practices for cloud applications | Microsoft Learn](https://learn.microsoft.com/en-us/azure/architecture/best-practices/transient-faults)
* [Azure service retry guidance - Best practices for cloud applications | Microsoft Learn](https://learn.microsoft.com/en-us/azure/architecture/best-practices/retry-service-specific)
* [Retry Storm antipattern - Performance antipatterns for cloud apps | Microsoft Learn](https://learn.microsoft.com/en-us/azure/architecture/antipatterns/retry-storm/)

EOF

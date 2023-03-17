# Microsoft Azure Well-Architected Framework Samples for Java

[日本語|Japanese](./README_ja.md)

### Update 2023/03

- Updated dependent libraries
- Updated Java verion 11 to 17.
- Updated Spring Boot Version 2.5.x to 2.7.9
- English documentation (Other pages will be prepared in the future)

## Introduction

This document provides the following executable samples based on the content of the Implementing Cloud Design Patterns Based on Microsoft Azure Well-Architected Framework (Java/Spring Boot Edition) seminar.

| Folder  | Content |
|---|---|
| [retry](./retry/README.md) | Retry pattern |
| [circuitbreaker](./circuitbreaker/README.md) | Circut breaker pattern |
| [health-endpoint-monitoring](./health-endpoint-monitoring/README.md) | Health endpoint monitoring pattern |
| [queue-base-load-leveling](./queue-base-load-leveling/README.md) | Queue base load leveling |
| [asynchronous-request-reply](./asynchronous-request-reply/README.md) | Asynchronous request reply pattern |

## Prerequisites

The following prerequisites are required to run this sample. The sample executions are described in the individual `README.md` files.

- Java 17 or later
- [Maven 3.8](https://maven.apache.org/index.html) or later
- curl (Some samples)
- [Azure Storge Emulator](https://docs.microsoft.com/ja-jp/azure/storage/common/storage-use-emulator) or Azurite Emulator](https://docs.microsoft.com/ja-jp/azure/storage/common/storage-use-azurite?tabs=npm) (Some samples)
- [Azure Functions Core ツール](https://docs.microsoft.com/ja-jp/azure/azure-functions/functions-run-local)（一部サンプル）
- Docker / Docker Compose （一部サンプル、なくても実行はできますが環境構築が簡単になります）
 
## External Service to be used

Here is a sample that uses a Web service that returns arbitrary status codes as an "external service". This Web service can return an HTTP status code for a request or a delayed response.

* [httpbin.org](http://httpbin.org/)

## Environment

This sample was tested in the following environments

- Ubuntu20.04 on Windows 11 WSL2 environment
- OpenJDK Runtime Environment Microsoft-32931 (build 17.0.3+7-LTS)
- Apache Maven 3.8.3
- Eclipse IDE for Java Developers (2021-3)
- Visual Studio Code 1.76.0 
- Docker Desktop fo Windows v20.10.8

## Terms of Use

- The information in this sample is current as of the date this sample was prepared.
- The information is subject to change due to changes in circumstances.
- We are not responsible for any indirect, incidental, or consequential damages (including loss of business opportunities or business information) incurred by you, your affiliates, or any third party in connection with the use of this sample.


This sample is released under the [MIT License](./LICENSE.txt)


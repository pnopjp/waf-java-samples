# Asynchronous Response Pattern

## Prerequisites

- Java 17 or later
- Maven 3.8 or later
- [Azure Storage Emulator](https://docs.microsoft.com/ja-jp/azure/storage/common/storage-use-emulator) or [Azurite Emulator](https://docs.microsoft.com/ja-jp/azure/storage/common/storage-use-azurite?tabs=npm)
- [Azure Functions Core Tools](https://docs.microsoft.com/ja-jp/azure/azure-functions/functions-run-local)
- Or a Docker runtime environment (installation of Azure Storage Emulator and Azure Functions Core Tools is not required)

## Sample Structure

This sample consists of four components. However, the queue and BLOB operate on the same emulator.

1. Frontend Web Application
2. Azure Storage Queue
3. Backend Azure Functions Application
4. Azure Storage BLOB

The frontend web application is implemented with Spring Boot, generates tasks based on external requests, and sends messages to the queue.

The Azure Storage Queue uses the Azurite Emulator to handle tasks asynchronously.

The backend Azure Functions application is implemented with a queue trigger, receives messages from the queue, executes tasks sequentially, and stores the execution results in Azure Storage BLOB.

## How to Run

There are two ways to run this sample:

1. Manual execution
2. Execution using Docker Compose

The former requires the installation of a Storage emulator and Azure Functions Core Tools in your environment, and you will need to start each application individually. The latter is for environments where Docker is installed. Since each application runs on Docker, there is no need to install individual Storage emulators, etc., and everything can be started with the `docker-compose` command.

#### Starting the Storage Emulator

Check the prerequisites link and start the Azure Storage emulator or Azurite emulator. The former may be installed as part of Visual Studio, but it is recommended to use the Azurite emulator in the future.

If it is not installed, follow the documentation to install and start the Azurite emulator. There are several ways to install and start it, so use the method that suits your environment.

Below is an example of starting the Azurite emulator.


```sh
$ mkdir ~/azurite
$ azurite --location ~/azurite/
Azurite Blob service is starting at http://127.0.0.1:10000
Azurite Blob service is successfully listening at http://127.0.0.1:10000
Azurite Queue service is starting at http://127.0.0.1:10001
Azurite Queue service is successfully listening at http://127.0.0.1:10001
Azurite Table service is starting at http://127.0.0.1:10002
Azurite Table service is successfully listening at http://127.0.0.1:10002
```

#### Starting the Frontend Web Application

This is a Spring Boot Web application. Build and run it using the following commands:

```sh
cd frontent-webapp
mvn clean pacakage
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=10080
```

The port specification is set to use the same port as when starting with Docker Compose later. The following log will be displayed, and the application will start:


```log

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.5)

2021-10-22 17:04:56.268  INFO 7075 --- [           main] o.p.w.sample.async.sb.SampleApplication  : Starting SampleApplication using Java 17.0.12 on NICKEL with PID 7075 (/work/waf-java-samples/asynchronous-request-reply/webapps-queue-functions-blob/frontend-webapp/target/classes started by moris in /work/waf-java-samples/asynchronous-request-reply/webapps-queue-functions-blob/frontend-webapp)
2021-10-22 17:04:56.279  INFO 7075 --- [           main] o.p.w.sample.async.sb.SampleApplication  : No active profile set, falling back to default profiles: default
2021-10-22 17:05:00.304  INFO 7075 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 10080 (http)
2021-10-22 17:05:00.334  INFO 7075 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-10-22 17:05:00.334  INFO 7075 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.53]
2021-10-22 17:05:00.558  INFO 7075 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2021-10-22 17:05:00.558  INFO 7075 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 4110 ms
2021-10-22 17:05:00.853  INFO 7075 --- [           main] o.p.w.s.async.sb.services.QueueService   : Connection string :DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1;TableEndpoint=http://127.0.0.1:10002/devstoreaccount1;
2021-10-22 17:05:00.859  INFO 7075 --- [           main] o.p.w.s.async.sb.services.QueueService   : Queue name        :asyncreqrep
2021-10-22 17:05:01.036  INFO 7075 --- [           main] c.a.c.i.jackson.JacksonVersion           : Package versions: jackson-annotations=2.12.5, jackson-core=2.12.5, jackson-databind=2.12.5, jackson-dataformat-xml=2.12.5, jackson-datatype-jsr310=2.12.5, azure-core=1.21.0
2021-10-22 17:05:02.835  INFO 7075 --- [           main] o.p.w.s.async.sb.services.BlobService    : Connection string :DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1;TableEndpoint=http://127.0.0.1:10002/devstoreaccount1;
2021-10-22 17:05:02.835  INFO 7075 --- [           main] o.p.w.s.async.sb.services.BlobService    : Container name    :asyncreqrep
2021-10-22 17:05:04.288  INFO 7075 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 10080 (http) with context path ''
2021-10-22 17:05:04.314  INFO 7075 --- [           main] o.p.w.sample.async.sb.SampleApplication  : Started SampleApplication in 9.75 seconds (JVM running for 10.433)

```

#### Starting the Backend Functions Application

Check the prerequisite links and install the Azure Functions Core Tools. Execution is done via mvn.


```sh
cd backend-function
mvn clean package
mvn azure-functions:run
```


```log
[INFO] Scanning for projects...
[INFO]
[INFO] -------------< org.pnop.waf.sample:async-backend-function >-------------
[INFO] Building Asynchronous Request Reply Azure Function apps 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- azure-functions-maven-plugin:1.14.0:run (default-cli) @ async-backend-function ---
[INFO] Azure Function App's staging directory found at: /work/waf-java-samples/asynchronous-request-reply/webapps-queue-functions-blob/backend-function/target/azure-functions/async-backend-function-20211012200706420
3.0.3568
[INFO] Azure Functions Core Tools found.

Azure Functions Core Tools
Core Tools Version:       3.0.3568 Commit hash: e30a0ede85fd498199c28ad699ab2548593f759b  (64-bit)
Function Runtime Version: 3.0.15828.0

[2021-10-22T08:06:29.733Z] Worker process started and initialized.

Functions:

        Function: queueTrigger

For detailed output, run func with --verbose flag.
[2021-10-22T08:06:34.973Z] Host lock lease acquired by instance ID '00000000000000000000000049ED9E21'.
```

### How to run using Docker Compose

You can also run this sample all at once using Docker Compose. Since each application runs in a container, there is no need to install emulators, etc. First, run `build.sh` to build each Java application, and then create the Docker Image.

```sh
./build.sh
docker-compose build
```

Next, start the containers.

```sh
docker-compose up
```

Next, start the containers.

```sh
docker-compose up
```

```log
Creating network "webapps-queue-functions-blob_default" with the default driver
Creating webapps-queue-functions-blob_emulator_1 ... done
Creating webapps-queue-functions-blob_backend_1  ... done
Creating webapps-queue-functions-blob_frontend_1 ... done
Attaching to webapps-queue-functions-blob_emulator_1, webapps-queue-functions-blob_backend_1, webapps-queue-functions-blob_frontend_1
emulator_1  | Azurite Blob service is starting at http://0.0.0.0:10000
emulator_1  | Azurite Blob service is successfully listening at http://0.0.0.0:10000
emulator_1  | Azurite Queue service is starting at http://0.0.0.0:10001
backend_1   | Starting OpenBSD Secure Shell server: sshd.
emulator_1  | Azurite Queue service is successfully listening at http://0.0.0.0:10001
emulator_1  | Azurite Table service is starting at http://0.0.0.0:10002
emulator_1  | Azurite Table service is successfully listening at http://0.0.0.0:10002
frontend_1  |
frontend_1  |   .   ____          _            __ _ _
frontend_1  |  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
frontend_1  | ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
frontend_1  |  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
frontend_1  |   '  |____| .__|_| |_|_| |_\__, | / / / /
frontend_1  |  =========|_|==============|___/=/_/_/_/
frontend_1  |  :: Spring Boot ::                (v2.5.5)
frontend_1  |
frontend_1  | 2021-10-22 08:10:38.887  INFO 1 --- [           main] o.p.w.sample.async.sb.SampleApplication  : Starting SampleApplication v1.0.0 using Java 17.0.12 on bf1571b3a35e with PID 1 (/app.jar started by root in /)
frontend_1  | 2021-10-22 08:10:38.890  INFO 1 --- [           main] o.p.w.sample.async.sb.SampleApplication  : No active profile set, falling back to default profiles: default
frontend_1  | 2021-10-22 08:10:40.037  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
frontend_1  | 2021-10-22 08:10:40.052  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
frontend_1  | 2021-10-22 08:10:40.053  INFO 1 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.53]
frontend_1  | 2021-10-22 08:10:40.119  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
frontend_1  | 2021-10-22 08:10:40.119  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1145 ms
frontend_1  | 2021-10-22 08:10:40.199  INFO 1 --- [           main] o.p.w.s.async.sb.services.QueueService   : Connection string :DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://emulator:10000/devstoreaccount1;QueueEndpoint=http://emulator:10001/devstoreaccount1;TableEndpoint=http://emulator:10002/devstoreaccount1;
frontend_1  | 2021-10-22 08:10:40.201  INFO 1 --- [           main] o.p.w.s.async.sb.services.QueueService   : Queue name        :asyncreqrep
frontend_1  | 2021-10-22 08:10:40.280  INFO 1 --- [           main] c.a.c.i.jackson.JacksonVersion           : Package versions: jackson-annotations=2.12.5, jackson-core=2.12.5, jackson-databind=2.12.5, jackson-dataformat-xml=2.12.5, jackson-datatype-jsr310=2.12.5, azure-core=1.21.0
backend_1   | info: Host.Triggers.Warmup[0]
backend_1   |       Initializing Warmup Extension.
backend_1   | info: Host.Startup[503]
backend_1   |       Initializing Host. OperationId: '382104ec-2e0d-420a-b40c-c5fa4a23fae4'.
backend_1   | info: Host.Startup[504]
backend_1   |       Host initialization: ConsecutiveErrors=0, StartupCount=1, OperationId=382104ec-2e0d-420a-b40c-c5fa4a23fae4
backend_1   | info: Microsoft.Azure.WebJobs.Hosting.OptionsLoggingService[0]
```

The following containers will start:

- Azurite Storage Emulator
- Frontend Web Application (Spring Boot Web Application)
- Backend Azure Functions Application (Queue Trigger)

## Execution Confirmation

Run the following command. The content of this command will be executed according to the asynchronous response pattern.

```ssh
./test.sh
```

1. Post a message to the API endpoint and get the request ID.
   Check the status by passing the request ID to the status endpoint every few seconds.
2. The status code returned from the status endpoint will be either `202 Accepted` or `302 Found`.
3. When `302 Found` is returned, the result BLOB URL is added to the `Location` header, so it will be redirected by curl, and the content of the BLOB will be displayed.

The execution result is as follows.


```log
REQUEST  http://localhost:10080/api/post
REQUEST ID = 62ba05ef-6a4c-4123-abb2-34bfb7943a43


POLLING http://localhost:10080/api/state/62ba05ef-6a4c-4123-abb2-34bfb7943a43
HTTP/1.1 202
Content-Length: 0
Date: Fri, 22 Oct 2021 08:26:03 GMT

POLLING http://localhost:10080/api/state/62ba05ef-6a4c-4123-abb2-34bfb7943a43
HTTP/1.1 202
Content-Length: 0
Date: Fri, 22 Oct 2021 08:26:05 GMT

POLLING http://localhost:10080/api/state/62ba05ef-6a4c-4123-abb2-34bfb7943a43
HTTP/1.1 302
Location: http://localhost:10000/devstoreaccount1/asyncreqrep/62ba05ef-6a4c-4123-abb2-34bfb7943a43?sv=2020-06-12&st=2021-10-22T08%253A25%253A07Z&se=2021-10-22T08%253A36%253A07Z&sr=b&sp=r&sig=hLo995v5VMrlKMBEWIOtVLoBxTiBP95XVR8uJD0%252FiJo%253D
Content-Length: 0
Date: Fri, 22 Oct 2021 08:26:07 GMT

HTTP/1.1 200 OK
Server: Azurite-Blob/3.14.2
last-modified: Fri, 22 Oct 2021 08:26:06 GMT
x-ms-creation-time: Fri, 22 Oct 2021 08:26:06 GMT
content-length: 41
content-type: text/plain
etag: "0x1CE6F025A07F100"
content-md5: dhB+fLOl1g4XHfrN8ZHVsw==
x-ms-blob-type: BlockBlob
x-ms-lease-state: available
x-ms-lease-status: unlocked
x-ms-request-id: 14f40674-8323-4cd5-b855-fa43d60778d9
x-ms-version: 2020-10-02
accept-ranges: bytes
date: Fri, 22 Oct 2021 08:26:07 GMT
x-ms-server-encrypted: true
Connection: keep-alive
Keep-Alive: timeout=5

Asynchronous Request Reply Pattern Sample
```

## Backend Configuration

In this sample, the `host.json` is configured with a batch size of 1 to prevent parallel execution.

```json
    "queues": {
      "batchSize": 1,
      "maxPollingInterval": "00:00:05"
    }
```

Refer to the following to change the settings and adjust the message load on the consumer side.

* [Overview of Azure Queue storage triggers and bindings in Azure Functions | Microsoft Docs](https://docs.microsoft.com/azure/azure-functions/functions-bindings-storage-queue#hostjson-settings)

以上
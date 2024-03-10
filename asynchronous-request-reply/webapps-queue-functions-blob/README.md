# 非同期応答パターン

## 前提条件

- Java 17 以降
- Maven 3.8 以降
- [Azure Storge エミュレータ](https://docs.microsoft.com/ja-jp/azure/storage/common/storage-use-emulator) または [Azurite エミュレータ](https://docs.microsoft.com/ja-jp/azure/storage/common/storage-use-azurite?tabs=npm) 
- [Azure Functions Core ツール](https://docs.microsoft.com/ja-jp/azure/azure-functions/functions-run-local)
- もしくはDocker実行環境（Azure Storage エミュレータとAzure Functions Core ツールのインストールが不要になります）

## サンプルの構成

本サンプルは4つのコンポーネントで構成されています。ただし、キューとBLOBは同一のエミュレータ上で動作します。

1. フロントエンド Web アプリケーション
2. Azure Storage キュー 
3. バックエンド Azure Functions アプリケーション
4. Azure Storage BLOB

フロントエンド Web アプリケーションは Spring Boot で実装されており、外部からのリクエストを元にタスクを生成し、キューにメッセージを送信します。

Azure Storage キューは、Azurite エミュレータを利用しており、タスクを非同期化します。

バックエンド Azure Functions アプリケーションはキュートリガーで実装しており、キューからメッセージを受信し順次タスクを実行し、その実行結果をAzure Storage BLOB に格納します。

## 実行方法

本サンプルには、2つの実行方法がります。

1. 手動による実行
2. Docker Compose による実行

前者は、お使いの環境に Storage エミュレータや Azure Functions Core ツールのインストールが必要で、1つ1つ個別にアプリケーションを起動していきます。後者は、Docker がインストールされている環境向けです。個々のアプリケーションはDocker上で実行されるので、個別に Storage エミュレータ等のインストールは不要で、 `docker-compose` コマンドで全てを起動できます。

#### ストレージエミュレータの起動

前提条件のリンクを確認し、Azure Storage エミュレータもしくは、Azurite エミュレータを起動します。前者は、Visual Studio に付属しているので、インストールされている可能性がありますが、今後は Azurite エミュレータの利用をお勧めします。

インストールされていない場合は、ドキュメントに従ってインストールし、Azurite エミュレータを起動してください。インストール方法、起動方法はいくつかの方法がありますので、お使いの環境にあった方法で行います。

以下は、Azurite エミュレータの起動例です。

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

#### フロントエンド Web アプリケーションの起動

Spring Boot Web アプリケーションです。以下のコマンドでビルド、実行します。

```sh
cd frontent-webapp
mvn clean pacakage
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=10080
```

ポート指定は、後の Docker Compose で起動した時と同じポートを使うように指定してあります。以下のようなログが表示され、アプリケーションが起動します。

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

### バックエンド Functions アプリケーションの起動

前提条件のリンクを確認し、Azure Functions Core ツールのインストールを行います。実行は `mvn` 経由で行います。

```sh
cd backend-function
mvn clean package
mvn azure-functions:run
```

以下のようなログが表示され、Azure Functions アプリケーションが起動します。

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

### Docker Compose を利用した実行方法

Docker Compose を利用して本サンプルを一括して実行することも出来ます。各アプリケーションはコンテナーで動作するので、エミュレータ等のインストールは不要です。はじめに、`build.sh` を実行して、各Javaアプリケーションをビルドし、次に Docker Image を作成します。

```sh
./build.sh
docker-compose build
```

次にコンテナを起動します。

```sh
docker-compose up
```

以下のようなログが表示され、各コンテナが起動します。

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

以下のコンテナが起動します

- Azurite ストレージエミュレーター
- フロントエンド Webアプリケーション （Spring Boot Web アプリケーション）
- バックエンド Azure Functions アプリケーション（キュートリガー）


## 実行確認

以下のコマンドを実行します。このコマンドの内容は、以下の通り非同期応答パターンにそって実行されます。

```ssh
./test.sh
```

1. APIエンドポイントに、メッセージをPOSTして、リクエストIDを取得する
   数秒毎に状態エンドポイントにリクエストIDを渡して状態を確認する
2. 状態エンドポイントから返却されるステータスコードは、`202 Accepted` もしくは、`302 Found` が返却される。
3. `302 Found` のときは `Location` ヘッダに結果のBLOB URL が付加されるので、curlにてリダイレクトされ、BLOBの内容が表示される

実行結果は以下の通りです。

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

## バックエンド側の設定について

本サンプルの `host.json` 並列実行されないように バッチサイズを1に設定しています。

```json
    "queues": {
      "batchSize": 1,
      "maxPollingInterval": "00:00:05"
    }
```

以下を参考に設定値を変更すると、コンシューマー側のメッセージの負荷を変化させることができます。

* [Azure Functions における Azure Queue storage のトリガーとバインドの概要 | Microsoft Docs](https://docs.microsoft.com/ja-jp/azure/azure-functions/functions-bindings-storage-queue#hostjson-settings)

以上
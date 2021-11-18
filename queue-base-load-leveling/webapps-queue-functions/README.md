# キューベースの負荷平準化サンプル

Spring Boot と Azure Storage キュー、および Azure Functions を利用して実装したキューベースの負荷平準化サンプルです。

## 前提条件

- Java 11 以降
- Maven 3.8 以降
- [Azure Storge エミュレータ](https://docs.microsoft.com/ja-jp/azure/storage/common/storage-use-emulator) または [Azurite エミュレータ](https://docs.microsoft.com/ja-jp/azure/storage/common/storage-use-azurite?tabs=npm) 
- [Azure Functions Core ツール](https://docs.microsoft.com/ja-jp/azure/azure-functions/functions-run-local)
- もしくはDocker実行環境（Azure Storage エミュレータとAzure Functions Core ツールのインストールが不要になります）

## サンプルの構成

本サンプルは3つのコンポーネントで構成されています。

1. プロデューサー Web アプリケーション
2. Azure Storage キュー
3. コンシューマー Azure Functions アプリケーション

プロデューサー Web アプリケーションは Spring Boot で実装されており、外部からのリクエストを元にタスクを生成し、キューにメッセージを送信します。

Azure Storage キューは、Azurite エミュレータを利用しており、タスクを平準化します。

コンシューマー Azure Functions アプリケーションはキュートリガーで実装しており、キューからメッセージを受信し順次タスクを実行します。本サンプルではタスクの実行の代わりにち任意の秒数スリープして終了します。

## 実行方法

本サンプルには、2つの実行方法がります。

1. 手動による実行
2. Docker Compose による実行

前者は、お使いの環境に Storage エミュレータや Azure Functions Core ツールのインストールが必要で、1つ1つ個別にアプリケーションを起動していきます。後者は、Docker がインストールされている環境向けです。個々のアプリケーションはDocker上で実行されるので、個別に Storage エミュレータ等のインストールは不要で、 `docker-compose` コマンドで全てを起動できます。

### 手動による実行方法

3つのアプリケーションを手動で起動します。ターミナルを複数起動し、それぞれ異なるコマンドラインで実行してください。

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

#### プロデューサー Web アプリケーションの起動

Spring Boot Web アプリケーションです。以下のコマンドでビルド、実行します。

```sh
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

2021-10-19 21:49:34.461  INFO 15905 --- [  restartedMain] o.p.waf.sample.lv.sb.SampleApplication   : Starting SampleApplication using Java 11.0.12 on NICKEL with PID 15905 (/work/waf-java-samples/queue-base-load-leveling/webapps-queue-functions/producer-webapp/target/classes started by moris in /work/waf-java-samples/queue-base-load-leveling/webapps-queue-functions/producer-webapp)
2021-10-19 21:49:34.474  INFO 15905 --- [  restartedMain] o.p.waf.sample.lv.sb.SampleApplication   : No active profile set, falling back to default profiles: default
2021-10-19 21:49:34.751  INFO 15905 --- [  restartedMain] .e.DevToolsPropertyDefaultsPostProcessor : For additional web related logging consider setting the 'logging.level.web' property to 'DEBUG'
2021-10-19 21:49:40.161  INFO 15905 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 10080 (http)
2021-10-19 21:49:40.202  INFO 15905 --- [  restartedMain] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-10-19 21:49:40.202  INFO 15905 --- [  restartedMain] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.53]
2021-10-19 21:49:40.390  INFO 15905 --- [  restartedMain] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2021-10-19 21:49:40.390  INFO 15905 --- [  restartedMain] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 5636 ms
2021-10-19 21:49:40.593  INFO 15905 --- [  restartedMain] org.pnop.waf.sample.lv.sb.QueueService   : Connection string :DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1;TableEndpoint=http://127.0.0.1:10002/devstoreaccount1;
2021-10-19 21:49:40.599  INFO 15905 --- [  restartedMain] org.pnop.waf.sample.lv.sb.QueueService   : Queue name        :loadleveling
2021-10-19 21:49:44.412  INFO 15905 --- [  restartedMain] o.s.b.d.a.OptionalLiveReloadServer       : LiveReload server is running on port 35729
2021-10-19 21:49:44.478  INFO 15905 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 10080 (http) with context path ''
2021-10-19 21:49:44.506  INFO 15905 --- [  restartedMain] o.p.waf.sample.lv.sb.SampleApplication   : Started SampleApplication in 11.935 seconds (JVM running for 13.125)
```

#### コンシューマーアプリの起動（Azure Functions）

前提条件のリンクを確認し、Azure Functions Core ツールのインストールを行います。実行は `mvn` 経由で行います。

```sh
cd consumer-function
mvn clean package
mvn azure-functions:run
```

以下のようなログが表示され、Azure Functions アプリケーションが起動します。

```log
[INFO] Scanning for projects...
[INFO]
[INFO] ---------------< org.pnop.waf.sample:consumer-function >----------------
[INFO] Building Azure Java Functions 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- azure-functions-maven-plugin:1.14.0:run (default-cli) @ consumer-function ---
[INFO] Azure Function App's staging directory found at: /work/waf-java-samples/queue-base-load-leveling/webapps-queue-functions/consumer-function/target/azure-functions/consumer-function-20211012200706420
3.0.3568
[INFO] Azure Functions Core Tools found.

Azure Functions Core Tools
Core Tools Version:       3.0.3568 Commit hash: e30a0ede85fd498199c28ad699ab2548593f759b  (64-bit)
Function Runtime Version: 3.0.15828.0

[2021-10-19T09:01:49.136Z] Worker process started and initialized.

Functions:

        Function: queueTrigger

For detailed output, run func with --verbose flag.
```

### Docker Compose を利用した実行方法

Docker Compose を利用して本サンプルを一括して実行することも出来ます。各アプリケーションはコンテナーで動作するので、エミュレータ等のインストールは不要です。はじめに、`build.sh` を実行して、各Javaアプリケーションをビルドし、次に Docker Image を作成します。

```sh
build.sh
docker-compose build
```

次にコンテナを起動します。

```sh
docker-compose up
```

以下のようなログが表示され、各コンテナが起動します。

```log
Creating network "webapps-queue-functions_default" with the default driver
Creating webapps-queue-functions_emulator_1 ... done
Creating webapps-queue-functions_consumer_1 ... done
Creating webapps-queue-functions_producer_1 ... done
Attaching to webapps-queue-functions_emulator_1, webapps-queue-functions_consumer_1, webapps-queue-functions_producer_1
consumer_1  | Starting OpenBSD Secure Shell server: sshd.
emulator_1  | Azurite Blob service is starting at http://0.0.0.0:10000
emulator_1  | Azurite Blob service is successfully listening at http://0.0.0.0:10000
emulator_1  | Azurite Queue service is starting at http://0.0.0.0:10001
emulator_1  | Azurite Queue service is successfully listening at http://0.0.0.0:10001
emulator_1  | Azurite Table service is starting at http://0.0.0.0:10002
emulator_1  | Azurite Table service is successfully listening at http://0.0.0.0:10002
producer_1  |
producer_1  |   .   ____          _            __ _ _
producer_1  |  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
producer_1  | ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
producer_1  |  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
producer_1  |   '  |____| .__|_| |_|_| |_\__, | / / / /
producer_1  |  =========|_|==============|___/=/_/_/_/
producer_1  |  :: Spring Boot ::                (v2.5.5)
producer_1  |
producer_1  | 2021-10-19 08:45:32.423  INFO 1 --- [           main] o.p.waf.sample.lv.sb.SampleApplication   : Starting SampleApplication v0.0.1-SNAPSHOT using Java 11.0.12 on 6b8b40a59798 with PID 1 (/app.jar started by root in /)
producer_1  | 2021-10-19 08:45:32.427  INFO 1 --- [           main] o.p.waf.sample.lv.sb.SampleApplication   : The following profiles are active: docker
producer_1  | 2021-10-19 08:45:33.700  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
producer_1  | 2021-10-19 08:45:33.718  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
producer_1  | 2021-10-19 08:45:33.719  INFO 1 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.53]
producer_1  | 2021-10-19 08:45:33.804  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
producer_1  | 2021-10-19 08:45:33.805  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1288 ms
producer_1  | 2021-10-19 08:45:33.889  INFO 1 --- [           main] org.pnop.waf.sample.lv.sb.QueueService   : Connection string :DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://emulator:10000/devstoreaccount1;QueueEndpoint=http://emulator:10001/devstoreaccount1;TableEndpoint=http://emulator:10002/devstoreaccount1;
producer_1  | 2021-10-19 08:45:33.891  INFO 1 --- [           main] org.pnop.waf.sample.lv.sb.QueueService   : Queue name        :loadleveling
consumer_1  | info: Host.Triggers.Warmup[0]
consumer_1  |       Initializing Warmup Extension.
consumer_1  | info: Host.Startup[503]
consumer_1  |       Initializing Host. OperationId: 'cccca460-07b7-4917-95cc-d4d8d12805ed'.
consumer_1  | info: Host.Startup[504]
consumer_1  |       Host initialization: ConsecutiveErrors=0, StartupCount=1, OperationId=cccca460-07b7-4917-95cc-d4d8d12805ed
consumer_1  | info: Microsoft.Azure.WebJobs.Hosting.OptionsLoggingService[0]
```

以下のコンテナが起動します

- Azurite ストレージエミュレーター
- プロデューサー Webアプリケーション （Spring Boot Web アプリケーション）
- コンシューマー Azure Functions アプリケーション（キュートリガー）

## 実行確認

以下のコマンドを起動します。パラメータの `10` はコンシューマ側での待機秒を指定します。

```
curl http://localhost:10080/test/10
```

上記のコマンドは即時にレスポンスが返りますが、タスクに見立てたメッセージは、キューを通してコンシューマーである Azure Functions キュートリガーアプリケーションに伝達します。このとき、コンシュマー側では、キューのメッセージを逐次に取得し処理していくため、キューによる平準化が行われます。

```log
[2021-10-19T13:13:02.342Z] message :       : {"id":"350a14af-fa91-4031-bd4c-43f11da144bc","value":10}
[2021-10-19T13:13:02.343Z] dequeue count   : 1
[2021-10-19T13:13:02.347Z] insertion time  : Tue Oct 19 22:13:01 JST 2021
[2021-10-19T13:13:02.347Z] expiration time : Tue Oct 26 22:13:01 JST 2021
[2021-10-19T13:13:02.595Z] processing : 0
[2021-10-19T13:13:03.596Z] processing : 1
[2021-10-19T13:13:04.598Z] processing : 2
[2021-10-19T13:13:05.599Z] processing : 3
[2021-10-19T13:13:06.600Z] processing : 4
[2021-10-19T13:13:07.600Z] processing : 5
[2021-10-19T13:13:08.602Z] processing : 6
[2021-10-19T13:13:09.601Z] processing : 7
[2021-10-19T13:13:10.604Z] processing : 8
[2021-10-19T13:13:11.603Z] processing : 9
[2021-10-19T13:13:12.606Z] Function "Function" (Id: 0bd193d6-08e1-4e82-b220-96c87915130e) invoked by Java Worker
[2021-10-19T13:13:12.635Z] Executed 'Functions.Function' (Succeeded, Id=0bd193d6-08e1-4e82-b220-96c87915130e, Duration=10424ms)
[2021-10-19T13:13:12.669Z] Executing 'Functions.Function' (Reason='New queue message detected on 'loadleveling'.', Id=47e595d9-4831-44cc-b1c2-69cb4ca85610)
[2021-10-19T13:13:12.669Z] Trigger Details: MessageId: d1ead10a-ef54-4543-a7b9-7b66e1310794, DequeueCount: 1, InsertionTime: 2021-10-19T13:13:05.000+00:00
[2021-10-19T13:13:12.675Z] message :       : {"id":"3cf53b7e-dc3c-472b-a027-b188a8307f3e","value":10}
[2021-10-19T13:13:12.682Z] dequeue count   : 1
[2021-10-19T13:13:12.682Z] insertion time  : Tue Oct 19 22:13:05 JST 2021
[2021-10-19T13:13:12.683Z] expiration time : Tue Oct 26 22:13:05 JST 2021
[2021-10-19T13:13:12.683Z] processing : 0
[2021-10-19T13:13:13.680Z] processing : 1
[2021-10-19T13:13:14.681Z] processing : 2
[2021-10-19T13:13:15.682Z] processing : 3
[2021-10-19T13:13:16.682Z] processing : 4
[2021-10-19T13:13:17.682Z] processing : 5
[2021-10-19T13:13:18.683Z] processing : 6
[2021-10-19T13:13:19.686Z] processing : 7
[2021-10-19T13:13:20.685Z] processing : 8
[2021-10-19T13:13:21.686Z] processing : 9
[2021-10-19T13:13:22.687Z] Function "Function" (Id: 47e595d9-4831-44cc-b1c2-69cb4ca85610) invoked by Java Worker
[2021-10-19T13:13:22.688Z] Executed 'Functions.Function' (Succeeded, Id=47e595d9-4831-44cc-b1c2-69cb4ca85610, Duration=10019ms)
[2021-10-19T13:13:22.697Z] Executing 'Functions.Function' (Reason='New queue message detected on 'loadleveling'.', Id=8cec03b8-b044-4665-97c3-568c21fa4ad1)
[2021-10-19T13:13:22.697Z] Trigger Details: MessageId: cc5cd05d-e9c1-4916-979e-913ead3b1918, DequeueCount: 1, InsertionTime: 2021-10-19T13:13:06.000+00:00
[2021-10-19T13:13:22.704Z] message :       : {"id":"09a8c51d-9e6d-4611-8caa-86939e05a130","value":10}
[2021-10-19T13:13:22.705Z] dequeue count   : 1
[2021-10-19T13:13:22.705Z] insertion time  : Tue Oct 19 22:13:06 JST 2021
[2021-10-19T13:13:22.705Z] expiration time : Tue Oct 26 22:13:06 JST 2021
[2021-10-19T13:13:22.706Z] processing : 0
[2021-10-19T13:13:23.707Z] processing : 1
[2021-10-19T13:13:24.707Z] processing : 2
[2021-10-19T13:13:25.708Z] processing : 3
[2021-10-19T13:13:26.709Z] processing : 4
[2021-10-19T13:13:27.709Z] processing : 5
[2021-10-19T13:13:28.710Z] processing : 6
[2021-10-19T13:13:29.710Z] processing : 7
[2021-10-19T13:13:30.711Z] processing : 8
[2021-10-19T13:13:31.711Z] processing : 9
[2021-10-19T13:13:32.712Z] Function "Function" (Id: 8cec03b8-b044-4665-97c3-568c21fa4ad1) invoked by Java Worker
[2021-10-19T13:13:32.713Z] Executed 'Functions.Function' (Succeeded, Id=8cec03b8-b044-4665-97c3-568c21fa4ad1, Duration=10016ms)
```

## コンシューマー側の設定について

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


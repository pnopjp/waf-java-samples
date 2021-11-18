# Resilience4j のリトライサンプル

Resilience4j を利用したリトライサンプルです。

## 概要

本サンプルは、Resilience4j ライブラリのリトライコンポーネントを利用したサンプルです。呼び出す外部サースとして任意のステータスコードを返却する WEB サービスを利用しています。


1. 外部サービスが `200` を返すパターン。リトライせずに終了します。
2. 外部サービスが `404` を返すパターン。リトライせずに終了します。
3. 外部サービスが `500` を返すパターン。`500` はリトライするステータスコードのため、リトライを試行します。
4. 外部サービスが `429` を返すパターン。上記と同様です。
5. 外部サービスが既定時間ないにレスポンスを返さないため、`HttpTimeoutException`がスローされる例。リトライ対象の例外であるため、リトライを試行します。


## 前提

- Java 11 以降
- Maven 3.6 以降

## 依存ライブラリ

Resilience4j の リトライライブラリを利用しています。

```xml
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-retry</artifactId>
            <version>1.7.1</version>
        </dependency>
```

## ビルドおよび実行方法

以下のコマンドでビルドします。

```sh
mvn clean pakcage
```

以下のコマンドで実行します。

```sh
mvn exec:java 
```

Visaul Studio Code や Eclipse などの IDE 上からもビルド、実行できます。

## 実行結果

本サンプルの実行結果をパターン別にまとめます。

### 外部サービスが200を返すパターン

正常なステータスが返却されるのでリトライされず終了します。


```log
2021-09-27 13:54:47:506 INFO App - Resilience4j retry sample start
2021-09-27 13:54:47:904 INFO RetrySample - Executing request : https://httpbin.org/status/200
```

### 外部サービスが404を返すパターン

正常でない `404 Not Found` のステータスが返却されますが、リトライ対象でないので、リトライされずに終了します。

```log
2021-09-27 13:54:49:083 INFO RetrySample - Executing request : https://httpbin.org/status/404
```

### 外部サービスが500を返すパターン

リトライすべきステータス（`500`）が返却されるので、最大試行回数までリトライされ、最終的に失敗します。

```log
2021-09-27 13:54:49:694 INFO RetrySample - Executing request : https://httpbin.org/status/500
2021-09-27 13:54:50:285 INFO RetrySample - onRetry : 2021-09-27T13:54:50.285057+09:00[Asia/Tokyo]: Retry 'retry', waiting PT3S until attempt '1'. Last attempt failed with exception 'null'.
2021-09-27 13:54:53:297 INFO RetrySample - Executing request : https://httpbin.org/status/500
2021-09-27 13:54:53:486 INFO RetrySample - onRetry : 2021-09-27T13:54:53.486279+09:00[Asia/Tokyo]: Retry 'retry', waiting PT6S until attempt '2'. Last attempt failed with exception 'null'.
2021-09-27 13:54:59:488 INFO RetrySample - Executing request : https://httpbin.org/status/500
2021-09-27 13:54:59:679 INFO RetrySample - onRetry : 2021-09-27T13:54:59.678930+09:00[Asia/Tokyo]: Retry 'retry', waiting PT12S until attempt '3'. Last attempt failed with exception 'null'.
2021-09-27 13:55:11:682 INFO RetrySample - Executing request : https://httpbin.org/status/500
2021-09-27 13:55:11:900 INFO RetrySample - onError : 2021-09-27T13:55:11.900590+09:00[Asia/Tokyo]: Retry 'retry' recorded a failed retry attempt. Number of retry attempts: '4'. Giving up. Last exception was: 'io.github.resilience4j.retry.MaxRetriesExceeded: max retries is reached out for the result predicate check'.
```

### `HttpTimeoutException` がスローされるパターン


`HttpTimeoutException` がスローされますが、リトライ対象なのでリトライされ、最終的に失敗します。

```log
2021-09-27 13:55:11:907 INFO RetrySample - Executing request : https://httpbin.org/delay/10
2021-09-27 13:55:16:918 INFO RetrySample - onRetry : 2021-09-27T13:55:16.916947+09:00[Asia/Tokyo]: Retry 'retry', waiting PT3S until attempt '1'. Last attempt failed with exception 'java.net.http.HttpTimeoutException: request timed out'.
2021-09-27 13:55:19:923 INFO RetrySample - Executing request : https://httpbin.org/delay/10
2021-09-27 13:55:24:934 INFO RetrySample - onRetry : 2021-09-27T13:55:24.933226+09:00[Asia/Tokyo]: Retry 'retry', waiting PT6S until attempt '2'. Last attempt failed with exception 'java.net.http.HttpTimeoutException: request timed out'.
2021-09-27 13:55:30:939 INFO RetrySample - Executing request : https://httpbin.org/delay/10
2021-09-27 13:55:35:946 INFO RetrySample - onRetry : 2021-09-27T13:55:35.946108+09:00[Asia/Tokyo]: Retry 'retry', waiting PT12S until attempt '3'. Last attempt failed with exception 'java.net.http.HttpTimeoutException: request timed out'.
2021-09-27 13:55:47:949 INFO RetrySample - Executing request : https://httpbin.org/delay/10
2021-09-27 13:55:52:962 INFO RetrySample - onError : 2021-09-27T13:55:52.962079+09:00[Asia/Tokyo]: Retry 'retry' recorded a failed retry attempt. Number of retry attempts: '4'. Giving up. Last exception was: 'java.net.http.HttpTimeoutException: request timed out'.
2021-09-27 13:55:52:966 INFO App - end
```

## ポイント

リトライの構成は `RetryConfig` クラスで行います。ビルダーパターンでリトライの構成を設定していきます。詳細は参考リンクからのリファレンスを参照してください。本サンプルもリトライ回数等は、プログラムに直接記述してあります。プロダクションコードではこのような実装は避けて（例えば環境変数や構成ファイルから読み込む）ください。

```java
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(MAX_ATTEMPT_COUNT)
            .retryOnResult(response -> ((HttpResponse<?>) response).statusCode() == 500)
            .retryExceptions(IOException.class, TimeoutException.class)
            .failAfterMaxAttempts(true)
            .intervalFunction(
                IntervalFunction
                    .ofExponentialBackoff(Duration.ofSeconds(RETRY_INTERVAL), 2d))
            .build();
```

イベントパブリッシャーから、リトライ時、例外時、成功時のイベントを受け取ることが出来ます。

```java
        retry.getEventPublisher()
            .onRetry(event -> logger.info("onRetry : {}", event.toString()))
            .onError(event -> logger.info("onError : {}", event.toString()))
            .onSuccess(event -> logger.info("onSuccess : {}", event.toString()));
```

リトライしたいロジックは、`Callable` や、 `Supplier`、`Runnable` などのインターフェースでコールバックを記述します。


```java
            HttpResponse<String> response = retry.executeCallable(new Callable<HttpResponse<String>>() {
                public java.net.http.HttpResponse<String> call() throws Exception {
                    logger.info("Executing request : {} " ,request.uri());
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    return response;
                }
            }
```

## 参考リンク

* [resilience4j/resilience4j: Resilience4j is a fault tolerance library designed for Java8 and functional programming](https://github.com/resilience4j/resilience4j)
* [Resilience4j Retry](https://resilience4j.readme.io/docs/retry)

以上
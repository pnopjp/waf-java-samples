# HttpComponents リトライサンプル

Apache HttpComponets を利用したリトライサンプルです。

## 概要

本サンプルは、 Apache HttpComponents の機能を用いて HTTP リクエストをリトライするサンプルです。 呼び出す外部サービスとして任意のステータスコードを返却する WEB サービスを利用しています。

2つのパターンを確認できます。

1. 外部サービスが `200` を返すパターン。リトライせずに終了します。
2. 外部サービスが `500` を返すパターン。`500` はリトライするステータスコードのため、リトライを試行します。

## 前提

- Java 11 以降
- Maven 3.6 以降

## 依存ライブラリ

Apache HttpComponents 5 を利用していします。

```
        <dependency>
            <groupId>org.apache.httpcomponents.client5</groupId>
            <artifactId>httpclient5</artifactId>
            <version>5.1</version>
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

### 外部サービスが200を返すパターン

正常なステータスが返却されるのでリトライされず終了します。

```log
2021-09-27 11:58:03:199 INFO App - httpcomponets sample start
2021-09-27 11:58:03:719 INFO RetrySample - Executing request GET https://httpbin.org/status/200
2021-09-27 11:58:04:908 INFO RetrySample - success
```

### 外部サービスが500を返すパターン

リトライすべきステータス（`500`）が返却されるので、最大試行回数までリトライされ、最終的に失敗します。

```log
2021-09-27 11:58:04:946 INFO RetrySample - Executing request GET https://httpbin.org/status/500
2021-09-27 11:58:05:728 INFO RetrySample - should be retry. execute count : 1 , statu code : 500
2021-09-27 11:58:08:916 INFO RetrySample - should be retry. execute count : 2 , statu code : 500
2021-09-27 11:58:15:101 INFO RetrySample - should be retry. execute count : 3 , statu code : 500
2021-09-27 11:58:24:301 INFO RetrySample - should be retry. execute count : 4 , statu code : 500
2021-09-27 11:58:36:487 INFO RetrySample - should be retry. execute count : 5 , statu code : 500
2021-09-27 11:58:51:671 INFO RetrySample - Number of retries exceeded or response code does not match retry status code :  6
```

## ポイント

リトライ時の振る舞いは、`MyRetryStrategy` で実装されています。

リトライ回数等は、プログラムに直接記述してあります。プロダクションコードではこのような実装は避けて（例えば環境変数や構成ファイルから読み込む）ください。

```java
        private static int MAX_RETRY_COUNT = 5; // リトライ回数
        private static int RETRY_INTERVAL = 3; // リトライ間隔
```

`retryRequest` メソッド にリトライする条件を実装します。`200`番台は成功、`500`、`503`、`429` はリトライ対象、それ以外は失敗と判定します。

```java
        /**
         * リトライすべき条件を判定します。
         */
        @Override
        public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
            int code = response.getCode();

            if (code >= 200 && code <= 299) {
                logger.info("success");
                return false;
            }

            if (execCount <= MAX_RETRY_COUNT) {
                if (code == HttpStatus.SC_INTERNAL_SERVER_ERROR ||
                    code == HttpStatus.SC_SERVICE_UNAVAILABLE ||
                    code == HttpStatus.SC_TOO_MANY_REQUESTS) {

                    logger.info("should be retry. execute count : {} , statu code : {}", execCount, code);
                    return true;
                }
            }

            logger.info("Number of retries exceeded or response code does not match retry status code :  {}",
                execCount);
            return false;
        }
```

例外を受け取る `retryRequest` メソッドにはリトライすべき例外を判定します。

```java
        /**
         * リトライすべき例外を判定します。
         */
        @Override
        public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
            logger.info("retry with exception");
            if (execCount <= MAX_RETRY_COUNT) {
                return exception instanceof SocketTimeoutException;
            }
            return false;
        }
```

リトライ間隔を計算します。以下の例では、実行回数によってリニアに増加させています。

```java
        
        /**
         * リトライ間隔を計算します。
         */
        @Override
        public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
            // 段階的間隔
            return TimeValue.ofSeconds(RETRY_INTERVAL * execCount);
        }
```

## 参考リンク

* [Apache HttpComponents – Apache HttpComponents](https://hc.apache.org/)


以上
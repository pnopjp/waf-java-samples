# 独自に実装した場合のリトライサンプル

リトライロジックを理解するため実装したサンプルです。実際はライブラリやフレームワークの利用を検討してください。

## 概要

本サンプルはリトライの内部ロジックを理解しやすいように、ライブラやフレームワークを使わずにリトライを実装した例です。呼び出す外部サースとして任意のステータスコードを返却する WEB サービスを利用しています。

4つのパターンをを確認できます。

1. 外部サービスが `200` を返すパターン。リトライせずに終了します。
2. 外部サービスが `500` を返すパターン。`500` はリトライするステータスコードのため、リトライを試行します。
3. 外部サービスが `429` を返すパターン。上記と同様です。
4. 外部サービスが既定時間ないにレスポンスを返さないため、`HttpTimeoutException`がスローされる例。リトライ対象の例外であるため、リトライを試行します。

## 前提環境

- Java 11 以降
- Maven 3.6 以降

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
2021-09-22 11:25:54:815 INFO App - main start
2021-09-22 11:25:54:816 INFO App - *** success ***
2021-09-22 11:25:55:603 INFO RetrySample - retry count : 0
2021-09-22 11:25:56:814 INFO RetrySample - response : 200
```

### 外部サービスが500を返すパターン

リトライすべきステータス（`500`）が返却されるので、最大試行回数までリトライされ、最終的に失敗します。

```log
2021-09-22 11:25:56:814 INFO App - *** internal server errror ***
2021-09-22 11:25:56:816 INFO RetrySample - retry count : 0
2021-09-22 11:25:57:370 INFO RetrySample - response : 500
2021-09-22 11:25:57:371 INFO RetrySample - waiting....
2021-09-22 11:26:00:383 INFO RetrySample - retry count : 1
2021-09-22 11:26:00:564 INFO RetrySample - response : 500
2021-09-22 11:26:00:564 INFO RetrySample - waiting....
2021-09-22 11:26:03:576 INFO RetrySample - retry count : 2
2021-09-22 11:26:03:754 INFO RetrySample - response : 500 
2021-09-22 11:26:03:755 INFO RetrySample - waiting....
2021-09-22 11:26:06:763 INFO RetrySample - retry count : 3
2021-09-22 11:26:06:942 INFO RetrySample - response : 500 
2021-09-22 11:26:06:943 ERROR RetrySample - Number of retries exceeded
```

### 外部サービスが429を返すパターン。500と同様

`500` と同様です。

```log
2021-09-22 11:26:06:943 INFO App - *** too many request ***
2021-09-22 11:26:06:946 INFO RetrySample - retry count : 0
2021-09-22 11:26:07:500 INFO RetrySample - response : 429 
2021-09-22 11:26:07:501 INFO RetrySample - waiting....
2021-09-22 11:26:10:505 INFO RetrySample - retry count : 1
2021-09-22 11:26:10:688 INFO RetrySample - response : 429 
2021-09-22 11:26:10:688 INFO RetrySample - waiting....
2021-09-22 11:26:13:701 INFO RetrySample - retry count : 2
2021-09-22 11:26:13:881 INFO RetrySample - response : 429 
2021-09-22 11:26:13:882 INFO RetrySample - waiting....
2021-09-22 11:26:16:894 INFO RetrySample - retry count : 3
2021-09-22 11:26:17:076 INFO RetrySample - response : 429 
2021-09-22 11:26:17:076 ERROR RetrySample - Number of retries exceeded
```

### `HttpTimeoutException` がスローされるパターン

`HttpTimeoutException` がスローされますが、リトライ対象なのでリトライされ、最終的に失敗します。

```log
2021-09-22 11:26:17:077 INFO App - *** timeout ***
2021-09-22 11:26:17:079 INFO RetrySample - retry count : 0
2021-09-22 11:26:22:082 WARN RetrySample - HttpTimeoutException
2021-09-22 11:26:22:083 INFO RetrySample - waiting....
2021-09-22 11:26:25:098 INFO RetrySample - retry count : 1
2021-09-22 11:26:30:100 WARN RetrySample - HttpTimeoutException
2021-09-22 11:26:30:101 INFO RetrySample - waiting....
2021-09-22 11:26:33:114 INFO RetrySample - retry count : 2
2021-09-22 11:26:38:124 WARN RetrySample - HttpTimeoutException
2021-09-22 11:26:38:125 INFO RetrySample - waiting....
2021-09-22 11:26:41:131 INFO RetrySample - retry count : 3
2021-09-22 11:26:46:146 WARN RetrySample - HttpTimeoutException
2021-09-22 11:26:46:146 ERROR RetrySample - Number of retries exceeded
2021-09-22 11:26:46:147 INFO App - main end
```

## ポイント

リトライ回数は定数として宣言されています。変更するとリトライ回数が変化します。

```java
    private static final int MAX_RETRY_COUNT = 3;
```

ステータスコードによるリトライの判定。 `200`番台は成功、`500`、`503`、`429` はリトライ対象、それ以外は失敗と判定します。

```java
                if (code >= 200 && code <= 299) {
                    return true;
                }
                // リトライすべきステータスコードかチェック
                if ((code == 500 || code == 503 || code == 429) == false) {
                    return false;
                }
```

リトライする例外の判定。`HttpTimeoutException` をリトライ対象にしています。それ以外は失敗と判定します。

```java
            } catch (HttpTimeoutException e) {
                // タイムアウトの例外はリトライ対象
                logger.warn("HttpTimeoutException");
            } catch (IOException | InterruptedException e) {
                // それ以外の例外は失敗
                logger.error("Exception", e);
                return false;
            }
```
リトライ間隔は一定です。`retryCount * RETRY_INTERVAL` に変更すると、段階的に間隔が延びていきます。

```java
    private static final int RETRY_INTERVAL  = 3;

    ...

            try {
                logger.info("waiting....");
                TimeUnit.SECONDS.sleep(RETRY_INTERVAL);
            } catch (InterruptedException e) {
                return false;
            }
```

以上



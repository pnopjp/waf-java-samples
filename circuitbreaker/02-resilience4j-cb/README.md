# Rejilience4j のサーキットブレーカーサンプル

Resilience4j を利用したサーキットブレーカーサンプルです。

## 概要

本サンプルは、Resilience4j ライブラリのリトライコンポーネントを利用したサンプルです。各パラメータは以下のように定義しています。

| 項目  | 設定値  |
|---|---|
| 判定方法  | カウントベース |
| スライディングウィンドウ| 10個 |
| 失敗率 | 30% |
| HALF OPEN中の呼び出し許可回数 | 10回 |
| 記録する例外| IOException, RuntimeException |
| OPEN から HALF OPENへの移行| 自動 |
| OPEN から HALF OPENへの待機時間 | 5秒 |

<br>

> :warning: **本サンプルはサーキットブレーカーの振る舞いを理解するために小さめの値を設定しています。デフォルト値は、Resilience4jのリファレンスを参照してください。**

## 前提

- Java 17 以降
- Maven 3.6 以降

## 依存ライブラリ

Resilience4j の サーキットブレーカーライブラリを利用しています。

```xml
    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-circuitbreaker</artifactId>
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

### CLOSED -> OPEN

初期状態（CLOSED） ->　OPEN へ移行する例です。10回のスライディングウィンドウの失敗率が30%を超える（本例だと3回）と OPEN へ遷移しますが、最低でもスライディングウィンドウ数の呼び出しがないと評価されません。外部サービス呼び出しを3回に1回失敗するようにしてあり、10回目の呼び出し後に失敗率が評価され、OPEN に遷移しています。OPEN に遷移後は、外部サービス呼び出しはブロックされます。

```log
2021-10-11 11:30:56:556 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = 呼び出し成功
2021-10-11 11:30:56:567 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = false, cause = 呼び出し失敗
2021-10-11 11:30:56:568 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = 呼び出し成功
2021-10-11 11:30:56:570 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = 呼び出し成功
2021-10-11 11:30:56:572 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = false, cause = 呼び出し失敗
2021-10-11 11:30:56:574 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = 呼び出し成功
2021-10-11 11:30:56:576 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = 呼び出し成功
2021-10-11 11:30:56:577 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = false, cause = 呼び出し失敗
2021-10-11 11:30:56:578 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = 呼び出し成功
2021-10-11 11:30:56:605 INFO CircuitBreakerSample - state = CLOSED -> OPEN, success = true, result = 呼び出し成功
2021-10-11 11:30:56:610 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:612 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:613 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:615 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:617 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:619 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:622 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:624 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:626 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:30:56:627 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
```

### OPEN -> HALF OPEN と HALF OPEN -> OPEN

5秒以上待機させ、HALF OPEN 状態にします。前回の最終状態は、OPEN でしたが5秒以上経過したために HALF OPEN になります。また、HALF OPEN 中に5回外部サービス呼び出しを失敗させ、再度 OPEN に遷移します。

```log
2021-10-11 11:30:56:629 INFO CircuitBreakerSample - ---------------- waiting ----------------------
2021-10-11 11:31:02:631 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = false, cause = 呼び出し失敗
2021-10-11 11:31:02:632 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = false, cause = 呼び出し失敗
2021-10-11 11:31:02:633 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = false, cause = 呼び出し失敗
2021-10-11 11:31:02:634 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = false, cause = 呼び出し失敗
2021-10-11 11:31:02:636 INFO CircuitBreakerSample - state = HALF_OPEN -> OPEN, success = false, cause = 呼び出し失敗
2021-10-11 11:31:02:637 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:31:02:639 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:31:02:643 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:31:02:646 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
2021-10-11 11:31:02:651 INFO CircuitBreakerSample - state = OPEN -> OPEN, success = false, cause = CircuitBreaker 'mycircuitbreaker' is OPEN and does not permit further calls
```


再度5秒以上待機させ、HALF OPEN 状態にした後、HALF OPEN 中の失敗率が閾値以下ならば CLOSED に遷移します。

```log
2021-10-11 11:31:02:655 INFO CircuitBreakerSample - ---------------- waiting ----------------------
2021-10-11 11:31:08:657 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = false, cause = 呼び出し失敗
2021-10-11 11:31:08:658 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = true, result = 呼び出し成功
2021-10-11 11:31:08:661 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = true, result = 呼び出し成功
2021-10-11 11:31:08:662 INFO CircuitBreakerSample - state = HALF_OPEN -> HALF_OPEN, success = true, result = 呼び出し成功
2021-10-11 11:31:08:664 INFO CircuitBreakerSample - state = HALF_OPEN -> CLOSED, success = true, result = 呼び出し成功
2021-10-11 11:31:08:666 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = false, cause = 呼び出し失敗
2021-10-11 11:31:08:668 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = 呼び出し成功
2021-10-11 11:31:08:669 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = 呼び出し成功
2021-10-11 11:31:08:672 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = 呼び出し成功
2021-10-11 11:31:08:674 INFO CircuitBreakerSample - state = CLOSED -> CLOSED, success = true, result = 呼び出し成功
```

## ポイント


サーキットブレーカーの構成は `CircuitBreakerConfig` クラスで行います。詳細やデフォルト値は参考リンクからのリファレンスを参照してください。

```java
        CircuitBreakerConfig config = CircuitBreakerConfig
            .custom()
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10)
            .failureRateThreshold(30)
            .permittedNumberOfCallsInHalfOpenState(5)
            .recordExceptions(IOException.class, RuntimeException.class)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .waitDurationInOpenState(Duration.ofSeconds(5))
            .build();
```

失敗確率の調整は、カウンタの剰余演算で調整しています。構成値や失敗確率を調整して試してみてください。

```java
        // 10回中3回失敗すると OPEN 状態になり、以降 OPEN 状態なのでActionは呼ばれない
        for (int i = 0; i < 20; i++) {
            // 3回に一回例外を発生させる
            boolean throwException = i % 3 == 1;
            invoke(decorateFunction, throwException);
        }

        // HALF OPEN になるまで 5秒以上待つ
        logger.info("---------------- waiting ----------------------");
        sleep(Duration.ofSeconds(6));
```
## 参考リンク

* [CircuitBreaker](https://resilience4j.readme.io/docs/circuitbreaker)

以上
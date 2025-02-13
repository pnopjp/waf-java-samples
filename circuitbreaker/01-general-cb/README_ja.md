# 独自に実装した場合のサーキットブレーカーサンプル

サーキットブレーカーロジックを理解するために実装したサンプルです。実際はライブラリやフレームワークの利用を検討してください。

## 概要

ホンサンプルは、サーキットブレーカーの振る舞いを理解しやすいよう、ライブラリやフレームワークを使わずにサーキットブレーカーを実装した例です。

以下の状態遷移を確認できます。

1. CLOSED -> OPEN への遷移
2. OPEN -> HALF OPEN への遷移
3. HALF OPEN -> OPEN への遷移
4. HALF OPEN -> CLOSED への遷移

本サンプルでは以下のような条件で各状態を遷移します。

**CLOSE -> OPEN**

カウントベースで、指定した回数（例では5回）失敗すると、OPEN へ遷移します

**OPEN -> HALF OPEN**

経過時間ベースで、指定した秒数経過（例では10秒）すると、HALF OPEN へ遷移します。

**HALF OPEN -> OPEN**

HALF OPEN中に、1回失敗すると、再度 OPEN へ遷移します。本サンプルの実装では固定で変更できません。

**HALF OPEN->CLOSED**

カウントベースで、指定した回数（例では3回）成功すると、CLOSED へ遷移します

> :warning: **本サンプルはサーキットブレーカーの振る舞いを理解するためのサンプル実装です。例えば、マルチスレッドなどの考慮などはしていませんので、本サンプルをプロダクションコードに適用することはお控えください**

## 前提条件

- Java 17 以降
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

### CLOSED -> OPEN

初期状態（CLOESED）-> OPEN へ遷移する例です。10回外部サービスを呼び出す内の5回失敗させているため、サーキットブレーカーは OPEN へ遷移します。OPEN 中に該当サービスを呼び出しますが、サーキットブレーカーによって実際には呼び出されることはありません。

```log
2021-10-11 09:35:45:348 INFO CircuitBreaker - name = test, state = CLOSED -> CLOSED, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 1 , last changed = null
2021-10-11 09:35:45:350 INFO Main - I/O エラーが発生しました
2021-10-11 09:35:45:351 INFO MyAction - 呼び出し成功
2021-10-11 09:35:45:353 INFO CircuitBreaker - name = test, state = CLOSED -> CLOSED, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 1 , last changed = null
2021-10-11 09:35:45:355 INFO MyAction - 呼び出し失敗
2021-10-11 09:35:45:356 INFO CircuitBreaker - name = test, state = CLOSED -> CLOSED, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 2 , last changed = null
2021-10-11 09:35:45:358 INFO Main - I/O エラーが発生しました
2021-10-11 09:35:45:358 INFO MyAction - 呼び出し成功
2021-10-11 09:35:45:359 INFO CircuitBreaker - name = test, state = CLOSED -> CLOSED, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 2 , last changed = null
2021-10-11 09:35:45:360 INFO MyAction - 呼び出し失敗
2021-10-11 09:35:45:361 INFO CircuitBreaker - name = test, state = CLOSED -> CLOSED, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 3 , last changed = null
2021-10-11 09:35:45:362 INFO Main - I/O エラーが発生しました
2021-10-11 09:35:45:363 INFO MyAction - 呼び出し成功
2021-10-11 09:35:45:364 INFO CircuitBreaker - name = test, state = CLOSED -> CLOSED, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 3 , last changed = null
2021-10-11 09:35:45:365 INFO MyAction - 呼び出し失敗
2021-10-11 09:35:45:366 INFO CircuitBreaker - name = test, state = CLOSED -> CLOSED, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 4 , last changed = null
2021-10-11 09:35:45:368 INFO Main - I/O エラーが発生しました
2021-10-11 09:35:45:369 INFO MyAction - 呼び出し成功
2021-10-11 09:35:45:369 INFO CircuitBreaker - name = test, state = CLOSED -> CLOSED, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 4 , last changed = null
2021-10-11 09:35:45:371 INFO MyAction - 呼び出し失敗
2021-10-11 09:35:45:378 INFO CircuitBreaker - name = test, state = CLOSED -> OPEN, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 5 , last changed = 09:35:45.377
2021-10-11 09:35:45:379 INFO Main - I/O エラーが発生しました
2021-10-11 09:35:45:380 INFO CircuitBreaker - name = test, state = OPEN -> OPEN, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 5 , last changed = 09:35:45.377
2021-10-11 09:35:45:382 INFO Main - タイムアウト期間が経過していないので、依 然 OPEN 状態です
```

### OPEN -> HALF OPEN と HALF OPEN -> OPEN

5秒以上待機させ、HALF OPEN 状態にします。前回の最終状態は、OPEN でしたが5秒以上経過したために HALF OPEN になります。また、HALF OPEN 中に再度外部サービス呼び出しを失敗させ、再度 OPEN に遷移します。

```log
2021-10-11 09:35:45:383 INFO Main - -------------- Waiting ---------------
2021-10-11 09:35:55:385 INFO MyAction - 呼び出し失敗
2021-10-11 09:35:55:386 INFO CircuitBreaker - name = test, state = HALF_OPEN -> OPEN, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 6 , last changed = 09:35:55.385
2021-10-11 09:35:55:388 INFO Main - HALF OPEN 状態で呼び出しが失敗したので、 再度 OPEN 状態へ遷移します。
2021-10-11 09:35:55:389 INFO CircuitBreaker - name = test, state = OPEN -> OPEN, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 6 , last changed = 09:35:55.385
2021-10-11 09:35:55:390 INFO Main - タイムアウト期間が経過していないので、依 然 OPEN 状態です
2021-10-11 09:35:55:391 INFO CircuitBreaker - name = test, state = OPEN -> OPEN, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 6 , last changed = 09:35:55.385
2021-10-11 09:35:55:393 INFO Main - タイムアウト期間が経過していないので、依 然 OPEN 状態です
```

### HALF OPEN -> CLOSED

再度5秒以上待機させ、HALF OPEN 状態にした後、連続して3回以上成功すると CLOSED へ遷移します。

```log
2021-10-11 09:35:55:394 INFO Main - -------------- Waiting ---------------
2021-10-11 09:36:05:394 INFO MyAction - 呼び出し成功
2021-10-11 09:36:05:395 INFO CircuitBreaker - name = test, state = HALF_OPEN -> HALF_OPEN, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 6 , last changed = 09:36:05.394
2021-10-11 09:36:05:396 INFO MyAction - 呼び出し成功
2021-10-11 09:36:05:397 INFO CircuitBreaker - name = test, state = HALF_OPEN -> HALF_OPEN, last exception = java.io.IOException: I/O エラーが発生しました, failure count = 6 , last changed = 09:36:05.394
2021-10-11 09:36:05:399 INFO MyAction - 呼び出し成功
2021-10-11 09:36:05:400 INFO CircuitBreaker - name = test, state = HALF_OPEN -> CLOSED, last exception = null, failure count = 0 , last changed = 09:36:05.399
2021-10-11 09:36:05:401 INFO MyAction - 呼び出し成功
2021-10-11 09:36:05:402 INFO CircuitBreaker - name = test, state = CLOSED -> CLOSED, last exception = null, failure count = 0 , last changed = 09:36:05.399
2021-10-11 09:36:05:404 INFO MyAction - 呼び出し成功
2021-10-11 09:36:05:405 INFO CircuitBreaker - name = test, state = CLOSED -> CLOSED, last exception = null, failure count = 0 , last changed = 09:36:05.399
2021-10-11 09:36:05:406 INFO Main - end
```

## ポイント

状態遷移の閾値は、コンストラクタで定義しています。変更することでサーキットブレーカーの振る舞いを変化させることが出来ます。


```java
        // サーキットブレーカーの作成。5回失敗でOPENになり、5秒経過するとOPENからHALF_OPEN に遷移する
        int failureThreshold = 5;
        int halfOpenSuccessThreshold = 3;
        int openToHalfOpenWaitSecond = 5;
        CircuitBreaker circuitBreaker = new CircuitBreaker("test",
            failureThreshold,
            halfOpenSuccessThreshold,
            openToHalfOpenWaitSecond);
```


外部サービスに見立てたアクションは、与えられた引数によって強制的に例外をスロ-（失敗）するよう実装してあります。

```java
public class MyAction implements Action<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(MyAction.class);

    @Override
    public void run(Boolean throwException) throws Exception {
        if (throwException) {
            logger.info("呼び出し失敗");
            throw new IOException("I/O エラーが発生しました");
        }
        logger.info("呼び出し成功");
        return;
    }
}
```

以上

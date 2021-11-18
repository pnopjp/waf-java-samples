package org.pnop.sample.waf.cb.general;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static Logger logger = LoggerFactory.getLogger("Main");

    public static void main(String[] args) {

        // 外部サービス呼び出しに見立てたコールバック関数、BOOL値を受け取り例外をスローするか判定する
        Action<Boolean> action = new MyAction();

        // サーキットブレーカーの作成。5回失敗でOPENになり、5秒経過するとOPENからHALF_OPEN に遷移する
        int failureThreshold = 5;
        int halfOpenSuccessThreshold = 3;
        int openToHalfOpenWaitSecond = 5;
        CircuitBreaker circuitBreaker = new CircuitBreaker("test",
            failureThreshold,
            halfOpenSuccessThreshold,
            openToHalfOpenWaitSecond);

        // 成功、失敗を交互に繰り返し、最終的に、サーキットブレーカーはOPENになる
        for (int i = 0; i < 10; i++) {
            try {
                circuitBreaker.invoke(action, i % 2 == 0);
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }

        // 5秒以上経過させ、HALF_OPEN にする
        logger.info("-------------- Waiting ---------------");
        sleep(Duration.ofSeconds(10));

        // HALF_OPEN 状態で失敗すると、すぐにOPENに遷移する
        for (int i = 0; i < 3; i++) {
            try {
                circuitBreaker.invoke(action, true);
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }

        // 5秒以上経過させ、HALF_OPEN にする
        logger.info("-------------- Waiting ---------------");
        sleep(Duration.ofSeconds(10));

        // 連続して成功すると、HALF_OPEN から CLOSEDに遷移する
        for (int i = 0; i < 5; i++) {
            try {
                circuitBreaker.invoke(action, false);
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
        logger.info("end");
    }

    private static void sleep(Duration d) {
        try {
            Thread.sleep(d.toMillis());
        } catch (InterruptedException e) {
        }
    }
}

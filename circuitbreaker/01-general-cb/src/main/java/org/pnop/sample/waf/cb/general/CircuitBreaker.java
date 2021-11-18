package org.pnop.sample.waf.cb.general;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class CircuitBreaker {

    private static Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);

    private ICircuitBreakerStore stateStore;
    private CircuitBreakerStateEnum prevState = CircuitBreakerStateEnum.CLOSED;

    private long openToHalfOpenWaitTime;

    private String name;

    private int failureThreshold = 1;
    private int successThreshold = 1;

    /**
     * @param name                    リソース名
     * @param failureThreshold        OPENに遷移する失敗回数の閾値
     * @param halfOpenSuccessThresold HALF OPEN からCLOSED に遷移する成功回数の閾値 
     * @param openToHalfOpenWaitTime  OPENから HALF OPEN に遷移するタイマー（秒）
     */
    public CircuitBreaker(
        String name,
        int failureThreshold,
        int halfOpenSuccessThresold,
        long openToHalfOpenWaitTime) {

        this.name = name;
        this.failureThreshold = failureThreshold;
        this.successThreshold = halfOpenSuccessThresold;
        this.openToHalfOpenWaitTime = openToHalfOpenWaitTime;
        this.stateStore = new CircuitBreakerStateStore();
    }

    /**
     * @param <T>
     * @param arg
     * @param action
     * @throws Exception
     */
    public <T> void invoke(Action<T> action, T arg) throws Exception {

        prevState = stateStore.getState();
        try {
            if (stateStore.isOpen()) {
                whenCircuitIsOpen(action, arg);
                return;
            }

            // HALF_OPTN もしくは CLOSED ならサービス呼び出しする
            try {
                action.run(arg);
                transitionToClosed();
            } catch (Exception e) {
                // 例外が発生したら記録するし、OPEN状態への遷移を試みる
                trackException(e);
                transitionToOpen();
                throw e;
            }
        } finally {
            logger.info("{}", toString());
        }

    }

    private void trackException(Throwable t) {
        stateStore.setFailure(t);
    }

    private void transitionToOpen() {
        if (stateStore.isClosed()) {
            if (stateStore.getFailureCount() >= failureThreshold) {
                stateStore.open();
            }
        } else {
            stateStore.open();
        }
    }

    private void transitionToClosed() {
        if (stateStore.isHalfOpen()) {
            if (stateStore.successfullHalfOpen() >= this.successThreshold) {
                stateStore.reset();
            }
        }
    }

    private <T> void whenCircuitIsOpen(Action<T> action, T arg) throws Exception {

        var time = stateStore.getLastStateChangeDate().plusSeconds(openToHalfOpenWaitTime);

        // Open のタイムアウト期間が経過したかチェックする
        if (time.isBefore(LocalDateTime.now())) {

            // 経過していれば HalfOpen に遷移し、コールバックする
            // 成功したら Open に遷移する
            try {
                synchronized (this) {
                    prevState = CircuitBreakerStateEnum.HALF_OPEN;
                    stateStore.halfOpen();
                    action.run(arg);
                    transitionToClosed();
                    return;
                }
            } catch (Exception e) {
                trackException(e);
                transitionToOpen();
                throw new CircuitBreakerOpenException("HALF OPEN 状態で呼び出しが失敗したので、再度OPEN状態へ遷移します。", e);
            }
        }

        throw new CircuitBreakerOpenException("タイムアウト期間が経過していないので、依然 OPEN 状態です", this.stateStore.getLastException());
    }

    DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    @Override
    public String toString() {
        return String.format("name = %s, state = %s -> %s, last exception = %s, failure count = %s , last changed = %s",
            name,
            prevState,
            stateStore.getState(),
            stateStore.getLastException(),
            stateStore.getFailureCount(),
            stateStore.getLastStateChangeDate() == null ? null : stateStore.getLastStateChangeDate().format(f));

    }
}

package org.pnop.sample.waf.cb.general;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedDeque;

public class CircuitBreakerStateStore implements ICircuitBreakerStore {

    private ConcurrentLinkedDeque<Throwable> stack = new ConcurrentLinkedDeque<>();

    private CircuitBreakerStateEnum state = CircuitBreakerStateEnum.CLOSED;

    private LocalDateTime lastStateChangedDate;

    private int successfulHalfOpenCount = 0;

    @Override
    public CircuitBreakerStateEnum getState() {
        return state;
    }

    @Override
    public LocalDateTime getLastStateChangeDate() {
        return lastStateChangedDate;
    }

    @Override
    public Throwable getLastException() {
        return stack.isEmpty() ? null : stack.peek();
    }

    @Override
    public void trip(Throwable t) {
        changeState(CircuitBreakerStateEnum.OPEN);
        stack.push(t);
    }

    @Override
    public int getFailureCount() {
        return stack.size();
    }

    @Override
    public void setFailure(Throwable t) {
        stack.push(t);
    }

    @Override
    public void reset() {
        changeState(CircuitBreakerStateEnum.CLOSED);
        stack.clear();
        successfulHalfOpenCount = 0;
    }

    @Override
    public void close() {
        changeState(CircuitBreakerStateEnum.CLOSED);
    }

    @Override
    public void halfOpen() {
        changeState(CircuitBreakerStateEnum.HALF_OPEN);
    }

    @Override
    public void open() {
        successfulHalfOpenCount = 0;
        changeState(CircuitBreakerStateEnum.OPEN);
    }

    @Override
    public int successfullHalfOpen() {
        return ++successfulHalfOpenCount;
    }

    @Override
    public boolean isClosed() {
        return state == CircuitBreakerStateEnum.CLOSED;
    }

    @Override
    public boolean isHalfOpen() {
        return state == CircuitBreakerStateEnum.HALF_OPEN;
    }

    @Override
    public boolean isOpen() {
        return state == CircuitBreakerStateEnum.OPEN;
    }

    private void changeState(CircuitBreakerStateEnum state) {
        this.state = state;
        this.lastStateChangedDate = LocalDateTime.now();
    }

}

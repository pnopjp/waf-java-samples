package org.pnop.sample.waf.cb.general;

import java.time.LocalDateTime;

public interface ICircuitBreakerStore {

    CircuitBreakerStateEnum getState();

    LocalDateTime getLastStateChangeDate();

    Throwable getLastException();

    void trip(Throwable t);

    int getFailureCount();

    void setFailure(Throwable t);

    void reset();

    void close();

    void halfOpen();

    void open();

    int successfullHalfOpen();

    boolean isClosed();

    boolean isHalfOpen();

    boolean isOpen();

}
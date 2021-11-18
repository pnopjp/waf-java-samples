package org.pnop.sample.waf.cb.general;

public class CircuitBreakerOpenException extends Exception {

    private static final long serialVersionUID = 805857250073019856L;

    public CircuitBreakerOpenException() {
    }

    public CircuitBreakerOpenException(String message, Throwable innerException) {
        super(message, innerException);
    }
}

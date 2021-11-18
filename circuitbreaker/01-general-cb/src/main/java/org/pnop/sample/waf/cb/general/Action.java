package org.pnop.sample.waf.cb.general;

@FunctionalInterface
public interface Action<T> {
    void run(T value) throws Exception;
}

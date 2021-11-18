package org.pnop.sample.waf.cb.general;

/**
 * @author moris
 *
 */
public enum CircuitBreakerStateEnum {

    // 閉状態、サービス呼び出しされる
    CLOSED,

    // 半開状態、サービス呼び出しされるが、場合によっては直ぐに OPEN となる
    HALF_OPEN,

    // 開状態、サービスは呼び出されない
    OPEN,
}

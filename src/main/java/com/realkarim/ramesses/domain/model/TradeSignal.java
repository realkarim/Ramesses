package com.realkarim.ramesses.domain.model;

/**
 * Output signal produced by a {@link com.realkarim.ramesses.domain.strategy.TradingStrategy}.
 *
 * <ul>
 *   <li>{@link #BUY} — the strategy recommends entering the market.</li>
 *   <li>{@link #SELL} — the strategy recommends exiting the market.</li>
 *   <li>{@link #HOLD} — no action; the strategy sees no clear entry or exit opportunity.</li>
 * </ul>
 */
public enum TradeSignal {
    BUY, SELL, HOLD
}

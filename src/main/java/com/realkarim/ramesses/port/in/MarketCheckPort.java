package com.realkarim.ramesses.port.in;

/**
 * Inbound (driving) port that triggers one iteration of the trading loop.
 *
 * <p>Called periodically by the scheduler adapter. Each invocation fetches
 * the latest market data, evaluates the strategy, and executes a trade if
 * the signal and portfolio state align.
 */
public interface MarketCheckPort {

    /** Runs a single market-check cycle: fetch data, evaluate, and optionally trade. */
    void checkMarket();
}

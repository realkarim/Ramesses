package com.realkarim.ramesses.port.out;

import com.realkarim.ramesses.domain.model.Portfolio;

/**
 * Outbound (driven) port for executing trades and querying portfolio state.
 *
 * <p>Abstracts how buy/sell orders are placed and how portfolio state is tracked.
 * The current implementation simulates trades in memory; a future implementation
 * can wire this to real Binance order endpoints.
 */
public interface TradeExecutionPort {

    /** Executes a buy order at the given price, converting the full budget into ETH. */
    void buy(double price);

    /** Executes a sell order at the given price, converting all ETH holdings back to USDT. */
    void sell(double price);

    /** Returns the current portfolio snapshot. */
    Portfolio getPortfolio();
}

package com.realkarim.ramesses.domain.model;

import lombok.Value;

/**
 * Immutable snapshot of the current trading portfolio state.
 *
 * <p>Tracks the USDT budget available for buying, current ETH holdings, the entry price
 * of the open position (if any), cumulative realized P&amp;L across all closed trades,
 * and the current {@link PortfolioStep} that determines whether the next action should
 * be a buy or a sell.
 *
 * <p>A new Portfolio instance is created after every trade, preserving immutability.
 * Use {@link #initial(double)} to create the starting state with a given budget.
 */
@Value
public class Portfolio {
    double budget;
    double ethHoldings;
    PortfolioStep step;
    double entryPrice;
    double realizedPnl;

    /** Creates the initial portfolio: full budget, no holdings, ready to buy, zero P&amp;L. */
    public static Portfolio initial(double budget) {
        return new Portfolio(budget, 0.0, PortfolioStep.BUY_NEXT, 0.0, 0.0);
    }
}

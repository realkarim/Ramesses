package com.realkarim.ramesses.domain.model;

/**
 * State machine controlling the trading flow of the portfolio.
 *
 * <p>Ensures strict alternation between buying and selling:
 * <ul>
 *   <li>{@link #BUY_NEXT} — the portfolio holds no position; the next allowed action is to buy.</li>
 *   <li>{@link #SELL_NEXT} — the portfolio holds an open position; the next allowed action is to sell.</li>
 * </ul>
 *
 * <p>This prevents double-buying or double-selling within a single trade cycle.
 */
public enum PortfolioStep {
    BUY_NEXT, SELL_NEXT
}

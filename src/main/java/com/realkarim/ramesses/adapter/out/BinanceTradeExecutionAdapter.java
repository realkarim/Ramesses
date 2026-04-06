package com.realkarim.ramesses.adapter.out;

import com.realkarim.ramesses.domain.model.Portfolio;
import com.realkarim.ramesses.domain.model.PortfolioStep;
import com.realkarim.ramesses.port.out.TradeExecutionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * {@link TradeExecutionPort} adapter that simulates trade execution in memory.
 *
 * <p>Maintains the {@link Portfolio} state and applies trading fees on both sides:
 * <ul>
 *   <li><b>Buy</b>: converts the full USDT budget into ETH, then subtracts the fee
 *       from the ETH received (i.e. you receive slightly less ETH than budget/price).</li>
 *   <li><b>Sell</b>: converts all ETH holdings to USDT proceeds, then subtracts the
 *       fee from the proceeds. Calculates per-trade P&amp;L as
 *       {@code proceeds − costBasis} where cost basis accounts for the original
 *       buy-side fee, and accumulates it into realized P&amp;L.</li>
 * </ul>
 *
 * <p>No actual orders are placed on Binance — this adapter is for paper trading
 * and backtesting. Wire real {@code POST /api/v3/order} calls when ready for live trading.
 */
@Service
@Slf4j
public class BinanceTradeExecutionAdapter implements TradeExecutionPort {

    private Portfolio portfolio;
    private final double fee;

    public BinanceTradeExecutionAdapter(
        @Value("${application.trading.initial-budget:1000.0}") double initialBudget,
        @Value("${application.trading.fee:0.001}") double fee) {
        this.portfolio = Portfolio.initial(initialBudget);
        this.fee = fee;
    }

    /**
     * Simulates a market buy: converts entire budget to ETH at the given price,
     * deducts the trading fee from the ETH amount, and transitions the portfolio to SELL_NEXT.
     */
    @Override
    public void buy(double price) {
        double eth = portfolio.getBudget() / price;
        eth -= eth * fee;
        portfolio = new Portfolio(0.0, eth, PortfolioStep.SELL_NEXT, price, portfolio.getRealizedPnl());
        log.info("Buying at price: {}", price);
        log.info("Budget: {}, ETH: {}", portfolio.getBudget(), portfolio.getEthHoldings());
    }

    /**
     * Simulates a market sell: converts all ETH holdings to USDT at the given price,
     * deducts the trading fee from the proceeds, calculates trade P&amp;L against
     * the fee-adjusted cost basis, and transitions the portfolio to BUY_NEXT.
     */
    @Override
    public void sell(double price) {
        double proceeds = portfolio.getEthHoldings() * price;
        proceeds -= proceeds * fee;
        double costBasis = portfolio.getEthHoldings() * portfolio.getEntryPrice() / (1 - fee);
        double tradePnl = proceeds - costBasis;
        double realizedPnl = portfolio.getRealizedPnl() + tradePnl;
        portfolio = new Portfolio(proceeds, 0.0, PortfolioStep.BUY_NEXT, 0.0, realizedPnl);
        log.info("Selling at price: {}", price);
        log.info("Trade P&L: {} USDT | Realized P&L: {} USDT", String.format("%.4f", tradePnl), String.format("%.4f", realizedPnl));
        log.info("Budget: {}, ETH: {}", portfolio.getBudget(), portfolio.getEthHoldings());
    }

    @Override
    public Portfolio getPortfolio() {
        return portfolio;
    }
}

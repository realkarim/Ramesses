package com.realkarim.ramesses.domain.usecase;

import com.realkarim.ramesses.domain.model.PortfolioStep;
import com.realkarim.ramesses.domain.model.TradeSignal;
import com.realkarim.ramesses.domain.strategy.TradingStrategy;
import com.realkarim.ramesses.port.in.MarketCheckPort;
import com.realkarim.ramesses.port.out.MarketDataPort;
import com.realkarim.ramesses.port.out.TradeExecutionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Core use case that orchestrates one iteration of the trading loop.
 *
 * <p>Each call to {@link #checkMarket()} performs the following steps:
 * <ol>
 *   <li>Fetches the latest OHLCV bars via {@link MarketDataPort}.</li>
 *   <li>Passes them to the active {@link TradingStrategy} to get a signal.</li>
 *   <li>If the signal and portfolio step agree (BUY when BUY_NEXT, SELL when SELL_NEXT),
 *       executes the trade via {@link TradeExecutionPort}.</li>
 *   <li>Logs portfolio metrics: equity, realized P&amp;L, and — when a position is
 *       open — unrealized P&amp;L.</li>
 * </ol>
 *
 * <h3>P&amp;L calculations</h3>
 * <ul>
 *   <li><b>Cost basis</b> = (ETH holdings &times; entry price) / (1 &minus; fee),
 *       reflecting the total USDT spent including the buy-side fee.</li>
 *   <li><b>Sale proceeds</b> = ETH holdings &times; current price &times; (1 &minus; fee),
 *       reflecting the USDT received after the sell-side fee.</li>
 *   <li><b>Unrealized P&amp;L</b> = sale proceeds &minus; cost basis.</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class CheckMarketUseCase implements MarketCheckPort {

    private final MarketDataPort marketData;
    private final TradeExecutionPort tradeExecution;
    private final TradingStrategy strategy;
    private final double fee;

    private int iteration = 0;

    @Override
    public void checkMarket() {
        log.info("-----------------------------------------------");
        log.info("Iteration {}", ++iteration);

        var bars = marketData.getLatestBars();
        var signal = strategy.evaluate(bars);
        var portfolio = tradeExecution.getPortfolio();
        double latestPrice = bars.get(bars.size() - 1).getClose();

        if (signal == TradeSignal.BUY && portfolio.getStep() == PortfolioStep.BUY_NEXT) {
            log.info("Entering the market");
            tradeExecution.buy(latestPrice);
        } else if (signal == TradeSignal.SELL && portfolio.getStep() == PortfolioStep.SELL_NEXT) {
            log.info("Exiting the market");
            tradeExecution.sell(latestPrice);
        }

        var updated = tradeExecution.getPortfolio();
        double equity = updated.getBudget() + updated.getEthHoldings() * latestPrice;
        log.info("Current budget: {}", updated.getBudget());
        log.info("Current ETH: {}", updated.getEthHoldings());
        log.info("Total equity: {} USDT", String.format("%.4f", equity));
        log.info("Realized P&L: {} USDT", String.format("%.4f", updated.getRealizedPnl()));
        if (updated.getStep() == PortfolioStep.SELL_NEXT) {
            double costBasis = updated.getEthHoldings() * updated.getEntryPrice() / (1 - fee);
            double saleProceeds = updated.getEthHoldings() * latestPrice * (1 - fee);
            double unrealizedPnl = saleProceeds - costBasis;
            log.info("Unrealized P&L: {} USDT", String.format("%.4f", unrealizedPnl));
        }
    }
}

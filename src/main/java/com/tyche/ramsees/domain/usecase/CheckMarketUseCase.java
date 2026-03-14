package com.tyche.ramsees.domain.usecase;

import com.tyche.ramsees.domain.model.PortfolioStep;
import com.tyche.ramsees.domain.model.TradeSignal;
import com.tyche.ramsees.domain.strategy.TradingStrategy;
import com.tyche.ramsees.port.in.MarketCheckPort;
import com.tyche.ramsees.port.out.MarketDataPort;
import com.tyche.ramsees.port.out.TradeExecutionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CheckMarketUseCase implements MarketCheckPort {

    private final MarketDataPort marketData;
    private final TradeExecutionPort tradeExecution;
    private final TradingStrategy strategy;

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
        log.info("Current budget: {}", updated.getBudget());
        log.info("Current ETH: {}", updated.getEthHoldings());
    }
}

package com.tyche.ramesses.domain.strategy;

import com.tyche.ramesses.adapter.out.InMemoryMarketDataAdapter;
import com.tyche.ramesses.config.StrategyConfigProps;
import com.tyche.ramesses.domain.model.MarketBar;
import com.tyche.ramesses.util.BinanceHistoricalFetcher;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.analysis.criteria.NumberOfPositionsCriterion;
import org.ta4j.core.analysis.criteria.NumberOfWinningPositionsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.analysis.criteria.pnl.NetProfitCriterion;
import org.ta4j.core.cost.LinearTransactionCostModel;
import org.ta4j.core.cost.ZeroCostModel;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

@Slf4j
class MacDStrategyTest {

    private static final int HISTORICAL_DATA_MINIMUM_LENGTH = 100;

    @Test
    void backtest() {
        var bars = new BinanceHistoricalFetcher().fetchHistoricalBars();
        var config = defaultConfig();
        runBacktest(bars, config);
    }

    private void runBacktest(List<MarketBar> bars, StrategyConfigProps config) {
        var series = new BaseBarSeriesBuilder().withName("BACKTEST").build();
        for (var bar : bars) {
            series.addBar(bar.getTimestamp(), bar.getOpen(), bar.getHigh(),
                bar.getLow(), bar.getClose(), bar.getVolume());
        }

        var closePrice = new ClosePriceIndicator(series);
        var macd = new MACDIndicator(closePrice, config.getMacdShort(), config.getMacdLong());
        var macdSignal = new EMAIndicator(macd, config.getMacdSignalLength());
        var trendEma = new EMAIndicator(closePrice, config.getTrendEmaLength());

        var buyRule = new CrossedUpIndicatorRule(macd, macdSignal)
            .and((i, tr) -> macd.getValue(i).doubleValue() < 0)
            .and(new UnderIndicatorRule(closePrice, trendEma));
        var sellRule = new StopGainRule(closePrice, config.getStopGain())
            .or(new StopLossRule(closePrice, config.getStopLoss()));

        var strategy = new org.ta4j.core.BaseStrategy(buyRule, sellRule);

        var seriesManager = new BarSeriesManager(series,
            new LinearTransactionCostModel(0.001), new ZeroCostModel());
        var tradingRecord = seriesManager.run(strategy,
            HISTORICAL_DATA_MINIMUM_LENGTH, series.getBarCount());

        var totalReturn = new GrossReturnCriterion();
        log.info("Total return: {}", totalReturn.calculate(series, tradingRecord));
        log.info("Net profit: {}", new NetProfitCriterion().calculate(series, tradingRecord));
        log.info("Number of positions: {}", new NumberOfPositionsCriterion().calculate(series, tradingRecord));
        log.info("Number of winning positions: {}", new NumberOfWinningPositionsCriterion().calculate(series, tradingRecord));
        log.info("vs buy-and-hold: {}", new VersusBuyAndHoldCriterion(totalReturn).calculate(series, tradingRecord));

        for (var p : tradingRecord.getPositions()) {
            log.info(p.toString());
        }
    }

    private StrategyConfigProps defaultConfig() {
        var config = new StrategyConfigProps();
        config.setTrendEmaLength(100);
        config.setMacdShort(12);
        config.setMacdLong(26);
        config.setMacdSignalLength(9);
        config.setStopGain(0.5);
        config.setStopLoss(0.3);
        return config;
    }
}

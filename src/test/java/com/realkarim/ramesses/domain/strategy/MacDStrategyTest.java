package com.realkarim.ramesses.domain.strategy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.realkarim.ramesses.config.StrategyConfigProps;
import com.realkarim.ramesses.domain.model.MarketBar;
import com.realkarim.ramesses.util.BinanceHistoricalFetcher;
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
        assertTrue(!bars.isEmpty(), "Historical data should not be empty");

        var series = new BaseBarSeriesBuilder().withName("BACKTEST").build();
        for (var bar : bars) {
            series.addBar(bar.getTimestamp(), bar.getOpen(), bar.getHigh(),
                bar.getLow(), bar.getClose(), bar.getVolume());
        }

        var strategy = MacDStrategy.buildTa4jStrategy(series, config);

        var seriesManager = new BarSeriesManager(series,
            new LinearTransactionCostModel(0.001), new ZeroCostModel());
        var tradingRecord = seriesManager.run(strategy,
            HISTORICAL_DATA_MINIMUM_LENGTH, series.getBarCount());

        var grossReturn = new GrossReturnCriterion();
        double totalReturn = grossReturn.calculate(series, tradingRecord).doubleValue();
        double netProfit = new NetProfitCriterion().calculate(series, tradingRecord).doubleValue();
        int numberOfPositions = (int) new NumberOfPositionsCriterion().calculate(series, tradingRecord).doubleValue();
        int winningPositions = (int) new NumberOfWinningPositionsCriterion().calculate(series, tradingRecord).doubleValue();
        double vsBuyAndHold = new VersusBuyAndHoldCriterion(grossReturn).calculate(series, tradingRecord).doubleValue();

        log.info("Total return: {}", totalReturn);
        log.info("Net profit: {}", netProfit);
        log.info("Number of positions: {}", numberOfPositions);
        log.info("Number of winning positions: {}", winningPositions);
        log.info("vs buy-and-hold: {}", vsBuyAndHold);

        for (var p : tradingRecord.getPositions()) {
            log.info(p.toString());
        }

        assertTrue(numberOfPositions > 0, "Strategy should have entered the market at least once");
        assertTrue(totalReturn > 0, "Gross return should be a positive value");
        assertTrue(Double.isFinite(totalReturn), "Total return should be a finite number");
        assertTrue(Double.isFinite(netProfit), "Net profit should be a finite number");
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

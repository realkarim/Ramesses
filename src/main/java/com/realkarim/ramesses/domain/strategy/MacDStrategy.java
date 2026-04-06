package com.realkarim.ramesses.domain.strategy;

import com.realkarim.ramesses.config.StrategyConfigProps;
import com.realkarim.ramesses.domain.model.MarketBar;
import com.realkarim.ramesses.domain.model.TradeSignal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * MACD-based trading strategy implementation.
 *
 * <p>Uses the Moving Average Convergence Divergence (MACD) indicator combined with
 * a trend-following EMA filter to generate buy and sell signals.
 *
 * <h3>Buy rule (all conditions must be true):</h3>
 * <ol>
 *   <li>MACD line crosses above the signal line (bullish crossover)</li>
 *   <li>MACD value is below zero (crossover happens in bearish territory,
 *       indicating a reversal from oversold conditions)</li>
 *   <li>Price is below the trend EMA (confirming the asset is still undervalued
 *       relative to the longer-term trend)</li>
 * </ol>
 *
 * <h3>Sell rule (either condition triggers an exit):</h3>
 * <ul>
 *   <li>Stop-gain: price rises by the configured percentage above entry</li>
 *   <li>Stop-loss: price falls by the configured percentage below entry</li>
 * </ul>
 *
 * <p>The strategy lazily initialises its TA4J bar series on the first call to
 * {@link #evaluate(java.util.List)}, then incrementally appends only new bars on subsequent calls.
 */
public class MacDStrategy implements TradingStrategy {

    private static final Logger log = LoggerFactory.getLogger(MacDStrategy.class);

    private final StrategyConfigProps config;
    private final int maxBarCount;

    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private MACDIndicator macd;
    private EMAIndicator macdSignal;
    private EMAIndicator trendEma;
    private BaseStrategy strategy;

    public MacDStrategy(StrategyConfigProps config, int maxBarCount) {
        this.config = config;
        this.maxBarCount = maxBarCount;
    }

    /**
     * Evaluates the latest bars and returns a trade signal.
     *
     * <p>On the first invocation the full bar history is loaded into the series;
     * subsequent calls append only bars newer than the last known timestamp.
     * The strategy then checks entry/exit rules against the most recent bar index.
     */
    @Override
    public TradeSignal evaluate(List<MarketBar> bars) {
        if (series == null) {
            initSeries(bars);
        } else {
            appendNewBars(bars);
        }

        var endIndex = series.getEndIndex();
        log.info("Current price: {}", series.getLastBar().getClosePrice());
        log.info("trendEma: {}", trendEma.getValue(endIndex));
        log.info("macd: {}", macd.getValue(endIndex));
        log.info("macdSignal: {}", macdSignal.getValue(endIndex));

        if (strategy.shouldEnter(endIndex)) return TradeSignal.BUY;
        if (strategy.shouldExit(endIndex)) return TradeSignal.SELL;
        return TradeSignal.HOLD;
    }

    private void initSeries(List<MarketBar> bars) {
        series = new BaseBarSeriesBuilder()
            .withName("SERIES")
            .withMaxBarCount(maxBarCount)
            .build();

        for (var bar : bars) {
            series.addBar(bar.getTimestamp(), bar.getOpen(), bar.getHigh(),
                bar.getLow(), bar.getClose(), bar.getVolume());
        }

        closePrice = new ClosePriceIndicator(series);
        macd = new MACDIndicator(closePrice, config.getMacdShort(), config.getMacdLong());
        macdSignal = new EMAIndicator(macd, config.getMacdSignalLength());
        trendEma = new EMAIndicator(closePrice, config.getTrendEmaLength());

        strategy = buildTa4jStrategy(series, config);
    }

    /**
     * Builds the TA4J strategy (buy + sell rules) from the given series and config.
     *
     * <p>Exposed as package-private static so backtests can reuse the exact same
     * rule definitions without duplicating the indicator wiring.
     */
    static BaseStrategy buildTa4jStrategy(BarSeries series, StrategyConfigProps config) {
        var closePrice = new ClosePriceIndicator(series);
        var macd = new MACDIndicator(closePrice, config.getMacdShort(), config.getMacdLong());
        var macdSignal = new EMAIndicator(macd, config.getMacdSignalLength());
        var trendEma = new EMAIndicator(closePrice, config.getTrendEmaLength());

        var buyRule = new CrossedUpIndicatorRule(macd, macdSignal)
            .and((i, tr) -> macd.getValue(i).doubleValue() < 0)
            .and(new UnderIndicatorRule(closePrice, trendEma));
        var sellRule = new StopGainRule(closePrice, config.getStopGain())
            .or(new StopLossRule(closePrice, config.getStopLoss()));

        return new BaseStrategy(buyRule, sellRule);
    }

    /** Appends only bars with a timestamp later than the last bar already in the series. */
    private void appendNewBars(List<MarketBar> bars) {
        var lastTime = series.getLastBar().getEndTime();
        for (var bar : bars) {
            if (bar.getTimestamp().isAfter(lastTime)) {
                series.addBar(bar.getTimestamp(), bar.getOpen(), bar.getHigh(),
                    bar.getLow(), bar.getClose(), bar.getVolume());
                lastTime = bar.getTimestamp();
            }
        }
    }
}

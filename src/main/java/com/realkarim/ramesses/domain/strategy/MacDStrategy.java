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

        var buyRule = new CrossedUpIndicatorRule(macd, macdSignal)
            .and((i, tr) -> macd.getValue(i).doubleValue() < 0)
            .and(new UnderIndicatorRule(closePrice, trendEma));
        var sellRule = new StopGainRule(closePrice, config.getStopGain())
            .or(new StopLossRule(closePrice, config.getStopLoss()));

        strategy = new BaseStrategy(buyRule, sellRule);
    }

    // Appends only bars with a timestamp later than the last bar already in the series
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

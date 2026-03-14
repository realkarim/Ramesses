package com.tyche.ramsees.domain.strategy;

import com.tyche.ramsees.config.StrategyConfigProps;
import com.tyche.ramsees.domain.model.MarketBar;
import com.tyche.ramsees.domain.model.TradeSignal;
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

    public MacDStrategy(StrategyConfigProps config) {
        this.config = config;
    }

    @Override
    public TradeSignal evaluate(List<MarketBar> bars) {
        var series = toBarSeries(bars);
        var endIndex = series.getEndIndex();

        var closePrice = new ClosePriceIndicator(series);
        var macd = new MACDIndicator(closePrice, config.getMacdShort(), config.getMacdLong());
        var macdSignal = new EMAIndicator(macd, config.getMacdSignalLength());
        var trendEma = new EMAIndicator(closePrice, config.getTrendEmaLength());

        var buyRule = new CrossedUpIndicatorRule(macd, macdSignal)
            .and((i, tr) -> macd.getValue(i).doubleValue() < 0)
            .and(new UnderIndicatorRule(closePrice, trendEma));

        var sellRule = new StopGainRule(closePrice, config.getStopGain())
            .or(new StopLossRule(closePrice, config.getStopLoss()));

        var strategy = new BaseStrategy(buyRule, sellRule);

        log.info("Current price: {}", series.getLastBar().getClosePrice());
        log.info("trendEma: {}", trendEma.getValue(endIndex));
        log.info("macd: {}", macd.getValue(endIndex));
        log.info("macdSignal: {}", macdSignal.getValue(endIndex));

        if (strategy.shouldEnter(endIndex)) return TradeSignal.BUY;
        if (strategy.shouldExit(endIndex)) return TradeSignal.SELL;
        return TradeSignal.HOLD;
    }

    private BarSeries toBarSeries(List<MarketBar> bars) {
        var series = new BaseBarSeriesBuilder().withName("SERIES").build();
        for (var bar : bars) {
            series.addBar(bar.getTimestamp(), bar.getOpen(), bar.getHigh(),
                bar.getLow(), bar.getClose(), bar.getVolume());
        }
        return series;
    }
}

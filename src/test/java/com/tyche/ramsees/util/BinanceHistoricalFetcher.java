package com.tyche.ramsees.util;

import com.tyche.ramsees.adapter.out.BinanceMarketDataAdapter;
import com.tyche.ramsees.domain.model.MarketBar;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Test utility that fetches historical OHLCV bars from Binance for backtesting.
 * Loads 28 half-days of data (56 requests of up to 1000 bars each).
 */
public class BinanceHistoricalFetcher extends BinanceMarketDataAdapter {

    private static final long HALF_DAY_MILLIS = 1000L * 60 * 60 * 12;
    private static final int HALF_DAYS = 28 * 2;

    public BinanceHistoricalFetcher() {
        super("ETHUSDT", "5m");
    }

    public List<MarketBar> fetchHistoricalBars() {
        var endTime = Long.parseLong(this.getServerTime());
        var startTime = endTime - HALF_DAY_MILLIS;

        // Stack reverses the order so bars are chronological (oldest first)
        var stack = new Stack<MarketBar>();

        for (int i = 0; i < HALF_DAYS; i++) {
            var klines = fetchKlines("ETHUSDT", "5m", startTime, endTime, 1000);
            var bars = toMarketBars(klines);
            for (int j = bars.size() - 1; j >= 0; j--) {
                stack.push(bars.get(j));
            }
            endTime -= HALF_DAY_MILLIS;
            startTime -= HALF_DAY_MILLIS;
        }

        var result = new ArrayList<MarketBar>();
        while (!stack.isEmpty()) result.add(stack.pop());
        return result;
    }

    private List<MarketBar> toMarketBars(List<com.tyche.ramsees.api.dto.KlineResponseDTO> klines) {
        var bars = new ArrayList<MarketBar>();
        for (var k : klines) {
            bars.add(new MarketBar(
                java.time.ZonedDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(k.getCloseTime()),
                    java.time.ZoneId.systemDefault()),
                Double.parseDouble(k.getOpen()),
                Double.parseDouble(k.getHigh()),
                Double.parseDouble(k.getLow()),
                Double.parseDouble(k.getClose()),
                Double.parseDouble(k.getVolume())
            ));
        }
        return bars;
    }
}

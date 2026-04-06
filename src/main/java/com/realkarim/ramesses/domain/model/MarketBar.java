package com.realkarim.ramesses.domain.model;

import lombok.Value;
import java.time.ZonedDateTime;

/**
 * Immutable representation of a single OHLCV candlestick bar from the market.
 *
 * <p>Each bar captures a time-bucketed snapshot of price action: the opening price,
 * highest and lowest prices reached, closing price, and traded volume. The timestamp
 * corresponds to the bar's close time in UTC.
 *
 * <p>This is the core market data model passed between adapters, strategies, and use cases.
 */
@Value
public class MarketBar {
    ZonedDateTime timestamp;
    double open;
    double high;
    double low;
    double close;
    double volume;
}

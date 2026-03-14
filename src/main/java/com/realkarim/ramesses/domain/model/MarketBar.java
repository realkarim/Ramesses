package com.realkarim.ramesses.domain.model;

import java.time.ZonedDateTime;

public class MarketBar {
    private final ZonedDateTime timestamp;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final double volume;

    public MarketBar(ZonedDateTime timestamp, double open, double high,
        double low, double close, double volume) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public ZonedDateTime getTimestamp() { return timestamp; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }
    public double getVolume() { return volume; }
}

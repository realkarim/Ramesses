package com.tyche.ramesses.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Kline/Candlestick bars for a symbol. Klines are uniquely identified by their open time.
 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KlineResponseDTO {

    private Long openTime;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private Long closeTime;
    private String quoteAssetVolume;
    private Long numberOfTrades;
    private String takerBuyBaseAssetVolume;
    private String takerBuyQuoteAssetVolume;
    private String unknown;

    public Long getOpenTime() { return openTime; }
    public void setOpenTime(Long openTime) { this.openTime = openTime; }

    public String getOpen() { return open; }
    public void setOpen(String open) { this.open = open; }

    public String getHigh() { return high; }
    public void setHigh(String high) { this.high = high; }

    public String getLow() { return low; }
    public void setLow(String low) { this.low = low; }

    public String getClose() { return close; }
    public void setClose(String close) { this.close = close; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }

    public Long getCloseTime() { return closeTime; }
    public void setCloseTime(Long closeTime) { this.closeTime = closeTime; }

    public String getQuoteAssetVolume() { return quoteAssetVolume; }
    public void setQuoteAssetVolume(String quoteAssetVolume) { this.quoteAssetVolume = quoteAssetVolume; }

    public Long getNumberOfTrades() { return numberOfTrades; }
    public void setNumberOfTrades(Long numberOfTrades) { this.numberOfTrades = numberOfTrades; }

    public String getTakerBuyBaseAssetVolume() { return takerBuyBaseAssetVolume; }
    public void setTakerBuyBaseAssetVolume(String v) { this.takerBuyBaseAssetVolume = v; }

    public String getTakerBuyQuoteAssetVolume() { return takerBuyQuoteAssetVolume; }
    public void setTakerBuyQuoteAssetVolume(String v) { this.takerBuyQuoteAssetVolume = v; }

    public String getUnknown() { return unknown; }
    public void setUnknown(String unknown) { this.unknown = unknown; }

    @Override
    public String toString() {
        return "openTime: " + openTime
            + " open: " + open
            + " high: " + high
            + " low: " + low
            + " close: " + close
            + " volume: " + volume
            + " closeTime: " + closeTime
            + " quoteAssetVolume: " + quoteAssetVolume
            + " numberOfTrades: " + numberOfTrades
            + " takerBuyBaseAssetVolume: " + takerBuyBaseAssetVolume
            + " takerBuyQuoteAssetVolume: " + takerBuyQuoteAssetVolume;
    }
}

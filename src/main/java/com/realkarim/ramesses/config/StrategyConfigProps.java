package com.realkarim.ramesses.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.strategy")
public class StrategyConfigProps {

    private int trendEmaLength;
    private int macdShort;
    private int macdLong;
    private int macdSignalLength;
    private double stopGain;
    private double stopLoss;

    public int getTrendEmaLength() { return trendEmaLength; }
    public void setTrendEmaLength(int trendEmaLength) { this.trendEmaLength = trendEmaLength; }

    public int getMacdShort() { return macdShort; }
    public void setMacdShort(int macdShort) { this.macdShort = macdShort; }

    public int getMacdLong() { return macdLong; }
    public void setMacdLong(int macdLong) { this.macdLong = macdLong; }

    public int getMacdSignalLength() { return macdSignalLength; }
    public void setMacdSignalLength(int macdSignalLength) { this.macdSignalLength = macdSignalLength; }

    public double getStopGain() { return stopGain; }
    public void setStopGain(double stopGain) { this.stopGain = stopGain; }

    public double getStopLoss() { return stopLoss; }
    public void setStopLoss(double stopLoss) { this.stopLoss = stopLoss; }
}

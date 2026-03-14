package com.realkarim.ramesses.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.average-slots-splits")
public class SlotsConfigProps {

    private int fast;
    private int slow;

    public int getFast() { return fast; }
    public void setFast(int fast) { this.fast = fast; }

    public int getSlow() { return slow; }
    public void setSlow(int slow) { this.slow = slow; }
}

package com.tyche.ramsees.adapter.out;

import com.tyche.ramsees.domain.model.MarketBar;
import com.tyche.ramsees.port.out.MarketDataPort;
import java.util.Collections;
import java.util.List;

/**
 * MarketDataPort implementation backed by a pre-loaded list of bars.
 * Used for backtesting and testing without hitting the live Binance API.
 */
public class InMemoryMarketDataAdapter implements MarketDataPort {

    private final List<MarketBar> bars;

    public InMemoryMarketDataAdapter(List<MarketBar> bars) {
        this.bars = bars;
    }

    @Override
    public List<MarketBar> getLatestBars() {
        return Collections.unmodifiableList(bars);
    }
}

package com.realkarim.ramesses.port.out;

import com.realkarim.ramesses.domain.model.MarketBar;
import java.util.List;

/**
 * Outbound (driven) port for retrieving market price data.
 *
 * <p>Abstracts the data source so the domain does not depend on any specific
 * exchange API. Production implementations fetch from Binance; test
 * implementations can supply canned or in-memory data.
 */
public interface MarketDataPort {

    /**
     * Returns the most recent OHLCV bars, ordered chronologically (oldest first).
     *
     * <p>On the first call the implementation may return a large historical window;
     * subsequent calls typically append only the newest bar(s).
     */
    List<MarketBar> getLatestBars();
}

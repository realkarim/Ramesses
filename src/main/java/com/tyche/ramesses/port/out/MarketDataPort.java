package com.tyche.ramesses.port.out;

import com.tyche.ramesses.domain.model.MarketBar;
import java.util.List;

public interface MarketDataPort {
    List<MarketBar> getLatestBars();
}

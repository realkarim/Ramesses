package com.tyche.ramsees.port.out;

import com.tyche.ramsees.domain.model.MarketBar;
import java.util.List;

public interface MarketDataPort {
    List<MarketBar> getLatestBars();
}

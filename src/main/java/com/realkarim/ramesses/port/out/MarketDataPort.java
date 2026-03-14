package com.realkarim.ramesses.port.out;

import com.realkarim.ramesses.domain.model.MarketBar;
import java.util.List;

public interface MarketDataPort {
    List<MarketBar> getLatestBars();
}

package com.realkarim.ramesses.domain.strategy;

import com.realkarim.ramesses.domain.model.MarketBar;
import com.realkarim.ramesses.domain.model.TradeSignal;
import java.util.List;

public interface TradingStrategy {
    TradeSignal evaluate(List<MarketBar> bars);
}

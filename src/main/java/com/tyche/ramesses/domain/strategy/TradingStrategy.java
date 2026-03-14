package com.tyche.ramesses.domain.strategy;

import com.tyche.ramesses.domain.model.MarketBar;
import com.tyche.ramesses.domain.model.TradeSignal;
import java.util.List;

public interface TradingStrategy {
    TradeSignal evaluate(List<MarketBar> bars);
}

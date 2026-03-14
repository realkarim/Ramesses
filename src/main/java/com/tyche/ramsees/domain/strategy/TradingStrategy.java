package com.tyche.ramsees.domain.strategy;

import com.tyche.ramsees.domain.model.MarketBar;
import com.tyche.ramsees.domain.model.TradeSignal;
import java.util.List;

public interface TradingStrategy {
    TradeSignal evaluate(List<MarketBar> bars);
}

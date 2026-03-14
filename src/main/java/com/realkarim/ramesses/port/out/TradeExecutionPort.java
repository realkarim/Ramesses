package com.realkarim.ramesses.port.out;

import com.realkarim.ramesses.domain.model.Portfolio;

public interface TradeExecutionPort {
    void buy(double price);
    void sell(double price);
    Portfolio getPortfolio();
}

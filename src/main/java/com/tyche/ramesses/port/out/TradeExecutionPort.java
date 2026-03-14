package com.tyche.ramesses.port.out;

import com.tyche.ramesses.domain.model.Portfolio;

public interface TradeExecutionPort {
    void buy(double price);
    void sell(double price);
    Portfolio getPortfolio();
}

package com.tyche.ramsees.port.out;

import com.tyche.ramsees.domain.model.Portfolio;

public interface TradeExecutionPort {
    void buy(double price);
    void sell(double price);
    Portfolio getPortfolio();
}

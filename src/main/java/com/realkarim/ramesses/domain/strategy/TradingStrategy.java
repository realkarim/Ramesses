package com.realkarim.ramesses.domain.strategy;

import com.realkarim.ramesses.domain.model.MarketBar;
import com.realkarim.ramesses.domain.model.TradeSignal;
import java.util.List;

/**
 * Core domain port for pluggable trading strategies.
 *
 * <p>Implementations receive a window of recent market bars and return a
 * {@link TradeSignal} indicating whether to buy, sell, or hold. The strategy
 * is stateless with respect to portfolio — it only analyses price data.
 *
 * <p>New strategies (e.g. RSI, Bollinger Bands) can be added by implementing
 * this interface and registering them in {@code AppConfig}.
 */
public interface TradingStrategy {

    /**
     * Evaluates the latest market data and produces a trading signal.
     *
     * @param bars recent OHLCV bars, ordered chronologically (oldest first)
     * @return BUY, SELL, or HOLD
     */
    TradeSignal evaluate(List<MarketBar> bars);
}

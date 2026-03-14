# Ramesses

An automated cryptocurrency trading bot that monitors ETH price on Binance and executes buy/sell trades based on MACD technical analysis.

## What it does

Every N seconds (configurable), the bot:

1. Fetches the latest OHLCV candlestick data from Binance
2. Evaluates a MACD-based trading strategy on the full price history
3. Executes a buy or sell if the signal conditions are met

**Buy signal** — all three must be true:
- MACD line crosses above the signal line
- MACD value is negative (potential oversold condition)
- Price is below the 100-period trend EMA (downtrend context)

**Sell signal** — either:
- Stop gain: +0.5% profit reached
- Stop loss: -0.3% loss reached

## Tech Stack

- Java 11, Spring Boot 2.6.4
- [TA4J](https://github.com/ta4j/ta4j) — technical analysis indicators
- [Binance Connector Java](https://github.com/binance/binance-connector-java) — exchange API
- JFreeChart — chart rendering (dev/backtest tool)

## Configuration

All parameters are in `src/main/resources/application.yml`:

```yaml
application:
  scheduler:
    frequency: 10000          # How often the bot runs (ms)
  strategy:
    trend-ema-length: 100
    macd-short: 12
    macd-long: 26
    macd-signal-length: 9
    stop-gain: 0.5            # % profit target
    stop-loss: 0.3            # % loss limit
  trading:
    symbol: ETHUSDT
    interval: 5m              # Candlestick interval
    initial-budget: 1000.0    # Starting BUSD
    fee: 0.001                # Binance fee (0.1%)
```

## Running

```bash
./mvnw spring-boot:run
```

The bot will load 1000 historical bars on startup, then poll Binance at the configured frequency.

## Backtesting

Run `MacDStrategyTest` to backtest the MACD strategy against historical data fetched from Binance. The test logs:

- Total return
- Net profit
- Number of positions
- Win rate
- Performance vs buy-and-hold

## Architecture

Ramesses follows Hexagonal Architecture (Ports & Adapters). See [docs/Architecture.md](docs/Architecture.md) for a full breakdown.

```
SchedulerAdapter → CheckMarketUseCase → BinanceMarketDataAdapter
                                      → MacDStrategy
                                      → BinanceTradeExecutionAdapter
```

## Adding a New Strategy

Implement `TradingStrategy`, register it as the `TradingStrategy` bean in `AppConfig`. No other changes needed.

# Architecture

Ramesses follows **Hexagonal Architecture** (also known as Ports & Adapters). The core principle is that business logic has zero knowledge of external systems — it defines what it needs via interfaces (ports), and external systems implement those interfaces (adapters).

## Why Hexagonal

The trading domain — "given market data, decide when to buy or sell" — is pure logic that has nothing to do with Binance. Hexagonal enforces this separation:

- Strategies can be tested with plain Java objects, no Binance API calls
- Swapping from Binance to another exchange means writing one new adapter
- Backtesting reuses the exact same domain logic via `InMemoryMarketDataAdapter`

## Package Structure

```
com.tyche.ramesses/
├── domain/                        # Pure business logic — no Spring, no Binance
│   ├── model/
│   │   ├── MarketBar              # OHLCV candlestick bar
│   │   ├── Portfolio              # Current budget, ETH holdings, and trading step
│   │   ├── PortfolioStep          # BUY_NEXT | SELL_NEXT
│   │   └── TradeSignal            # BUY | SELL | HOLD
│   ├── strategy/
│   │   ├── TradingStrategy        # Strategy interface
│   │   └── MacDStrategy           # MACD-based implementation
│   └── usecase/
│       └── CheckMarketUseCase     # Orchestrates: fetch → evaluate → execute
│
├── port/
│   ├── in/
│   │   └── MarketCheckPort        # Driving port — what triggers the bot
│   └── out/
│       ├── MarketDataPort         # Driven port — how market data is obtained
│       └── TradeExecutionPort     # Driven port — how trades are executed
│
├── adapter/
│   ├── in/
│   │   └── SchedulerAdapter       # Triggers MarketCheckPort on a schedule
│   └── out/
│       ├── BinanceMarketDataAdapter    # Implements MarketDataPort via Binance API
│       ├── BinanceTradeExecutionAdapter # Implements TradeExecutionPort, tracks portfolio
│       └── InMemoryMarketDataAdapter   # Implements MarketDataPort from pre-loaded bars (backtesting)
│
├── config/
│   ├── AppConfig                  # Wires domain classes as Spring beans
│   ├── StrategyConfigProps        # MACD parameters from application.yml
│   ├── JobConfigProps             # Scheduler frequency from application.yml
│   └── SlotsConfigProps           # Moving average slot config from application.yml
│
└── api/dto/                       # Raw Binance API response shapes (adapter-internal)
    ├── KlineResponseDTO
    ├── PriceResponseDTO
    └── ServerTimeResponseDTO
```

## Dependency Rules

```
adapter  →  port  ←  domain
```

- **Domain** depends on nothing outside itself
- **Ports** are interfaces defined by the domain
- **Adapters** depend on ports, never on domain internals
- **Spring** is only present in adapters and config — never in domain or ports

## Data Flow

```
SchedulerAdapter  (every N seconds)
  → MarketCheckPort.checkMarket()
      → CheckMarketUseCase
          → MarketDataPort.getLatestBars()       ← BinanceMarketDataAdapter fetches klines
          → TradingStrategy.evaluate(bars)        ← MacDStrategy computes MACD signals
          → TradeExecutionPort.buy() / sell()     ← BinanceTradeExecutionAdapter updates portfolio
```

## Ports Reference

| Port | Direction | Purpose |
|------|-----------|---------|
| `MarketCheckPort` | Inbound (driving) | Entry point for the scheduler |
| `MarketDataPort` | Outbound (driven) | Fetch OHLCV candlestick bars |
| `TradeExecutionPort` | Outbound (driven) | Execute buy/sell and track portfolio |

## Adding a New Exchange

1. Implement `MarketDataPort` (fetch and convert bars to `MarketBar`)
2. Implement `TradeExecutionPort` (buy/sell logic and portfolio state)
3. Register the new adapters as Spring beans in `AppConfig`

No domain code changes required.

## Adding a New Strategy

1. Implement `TradingStrategy` — accept `List<MarketBar>`, return `TradeSignal`
2. Register it as the `TradingStrategy` bean in `AppConfig`

## Backtesting

Use `InMemoryMarketDataAdapter` with a pre-loaded `List<MarketBar>`:

```java
var bars = new BinanceHistoricalFetcher().fetchHistoricalBars();
var adapter = new InMemoryMarketDataAdapter(bars);
var strategy = new MacDStrategy(config);
var signal = strategy.evaluate(adapter.getLatestBars());
```

For full walk-forward backtesting, use ta4j's `BarSeriesManager` directly (see `MacDStrategyTest`).

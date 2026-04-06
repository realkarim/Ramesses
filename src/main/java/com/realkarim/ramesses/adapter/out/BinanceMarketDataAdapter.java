package com.realkarim.ramesses.adapter.out;

import com.binance.connector.client.impl.SpotClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.realkarim.ramesses.api.dto.KlineResponseDTO;
import com.realkarim.ramesses.api.dto.ServerTimeResponseDTO;
import com.realkarim.ramesses.domain.model.MarketBar;
import com.realkarim.ramesses.port.out.MarketDataPort;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * {@link MarketDataPort} adapter that fetches OHLCV bars from the Binance REST API.
 *
 * <p>Uses a lazy-initialisation and caching strategy to minimise API calls:
 * <ul>
 *   <li><b>First call</b>: fetches up to 1 000 historical bars to warm the series.</li>
 *   <li><b>Subsequent calls</b>: fetches only the latest bar and appends it.</li>
 * </ul>
 *
 * <p>Duplicate bars (same timestamp as the most recent cached bar) are silently
 * dropped by {@link #appendUnique(java.util.List)}. The cache is bounded to
 * {@code maxBars} entries — oldest bars are evicted when the limit is exceeded.
 *
 * <p>All bar timestamps are normalised to UTC regardless of the host machine's timezone.
 */
@Service
@Slf4j
public class BinanceMarketDataAdapter implements MarketDataPort {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String symbol;
    private final String interval;
    private final int maxBars;
    private final SpotClientImpl client = new SpotClientImpl();

    private final List<MarketBar> bars = new ArrayList<>();
    private boolean initialized = false;

    public BinanceMarketDataAdapter(
        @Value("${application.trading.symbol:ETHUSDT}") String symbol,
        @Value("${application.trading.interval:5m}") String interval,
        @Value("${application.trading.max-bars:500}") int maxBars) {
        this.symbol = symbol;
        this.interval = interval;
        this.maxBars = maxBars;
    }

    @Override
    public List<MarketBar> getLatestBars() {
        if (!initialized) {
            appendUnique(toMarketBars(fetchKlines(symbol, interval, null, null, 1000)));
            initialized = true;
        } else {
            appendUnique(toMarketBars(fetchKlines(symbol, interval, null, null, 1)));
        }
        return Collections.unmodifiableList(bars);
    }

    /**
     * Appends incoming bars that are not duplicates of the last cached bar.
     * Evicts the oldest bar when the cache exceeds {@code maxBars}.
     */
    private void appendUnique(List<MarketBar> incoming) {
        for (var bar : incoming) {
            if (bars.isEmpty() || !bar.getTimestamp().equals(bars.get(bars.size() - 1).getTimestamp())) {
                bars.add(bar);
                if (bars.size() > maxBars) {
                    bars.remove(0);
                }
            }
        }
    }

    public String getServerTime() {
        return new Gson().fromJson(client.createMarket().time(), ServerTimeResponseDTO.class).getServerTime();
    }

    protected List<KlineResponseDTO> fetchKlines(String symbol, String interval,
        Long startTime, Long endTime, Integer limit) {
        var parameters = new LinkedHashMap<String, Object>();
        parameters.put("symbol", symbol);
        parameters.put("interval", interval);
        parameters.put("limit", limit);
        if (startTime != null) parameters.put("startTime", startTime);
        if (endTime != null) parameters.put("endTime", endTime);

        var jsonArray = new JSONArray(client.createMarket().klines(parameters));
        var klineList = new ArrayList<KlineResponseDTO>();

        for (Object o : jsonArray) {
            try {
                klineList.add(OBJECT_MAPPER.readValue(o.toString(), KlineResponseDTO.class));
            } catch (JsonProcessingException e) {
                log.error("Exception while fetching klines", e);
            }
        }
        return klineList;
    }

    protected List<MarketBar> toMarketBars(List<KlineResponseDTO> klines) {
        var result = new ArrayList<MarketBar>();
        for (var k : klines) {
            result.add(new MarketBar(
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(k.getCloseTime()), ZoneId.of("UTC")),
                Double.parseDouble(k.getOpen()),
                Double.parseDouble(k.getHigh()),
                Double.parseDouble(k.getLow()),
                Double.parseDouble(k.getClose()),
                Double.parseDouble(k.getVolume())
            ));
        }
        return result;
    }
}

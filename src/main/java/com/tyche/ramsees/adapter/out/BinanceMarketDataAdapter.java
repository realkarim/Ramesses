package com.tyche.ramsees.adapter.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.binance.connector.client.impl.SpotClientImpl;
import com.google.gson.Gson;
import com.tyche.ramsees.api.dto.KlineResponseDTO;
import com.tyche.ramsees.api.dto.ServerTimeResponseDTO;
import com.tyche.ramsees.domain.model.MarketBar;
import com.tyche.ramsees.port.out.MarketDataPort;
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

@Service
@Slf4j
public class BinanceMarketDataAdapter implements MarketDataPort {

    private final String symbol;
    private final String interval;

    private final List<MarketBar> bars = new ArrayList<>();
    private boolean initialized = false;

    public BinanceMarketDataAdapter(
        @Value("${application.trading.symbol:ETHUSDT}") String symbol,
        @Value("${application.trading.interval:5m}") String interval) {
        this.symbol = symbol;
        this.interval = interval;
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

    private void appendUnique(List<MarketBar> incoming) {
        for (var bar : incoming) {
            if (bars.isEmpty() || !bar.getTimestamp().equals(bars.get(bars.size() - 1).getTimestamp())) {
                bars.add(bar);
            }
        }
    }

    public String getServerTime() {
        var result = new SpotClientImpl().createMarket().time();
        return new Gson().fromJson(result, ServerTimeResponseDTO.class).getServerTime();
    }

    protected List<KlineResponseDTO> fetchKlines(String symbol, String interval,
        Long startTime, Long endTime, Integer limit) {
        var parameters = new LinkedHashMap<String, Object>();
        parameters.put("symbol", symbol);
        parameters.put("interval", interval);
        parameters.put("limit", limit);
        if (startTime != null) parameters.put("startTime", startTime);
        if (endTime != null) parameters.put("endTime", endTime);

        var result = new SpotClientImpl().createMarket().klines(parameters);
        var jsonArray = new JSONArray(result);
        var klineList = new ArrayList<KlineResponseDTO>();

        for (Object o : jsonArray) {
            try {
                klineList.add(new ObjectMapper().readValue(o.toString(), KlineResponseDTO.class));
            } catch (JsonProcessingException e) {
                log.error("Exception while fetching klines", e);
            }
        }
        return klineList;
    }

    private List<MarketBar> toMarketBars(List<KlineResponseDTO> klines) {
        var result = new ArrayList<MarketBar>();
        for (var k : klines) {
            result.add(new MarketBar(
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(k.getCloseTime()), ZoneId.systemDefault()),
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

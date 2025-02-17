package com.cp.aitg.cryptoanalyzer.client.binance.market;

import com.cp.aitg.cryptoanalyzer.client.binance.client.BinanceApiClient;
import com.cp.aitg.cryptoanalyzer.client.binance.constant.HttpMethod;
import com.cp.aitg.cryptoanalyzer.client.binance.model.Kline;
import com.cp.aitg.cryptoanalyzer.client.binance.model.KlineRequest;
import com.cp.aitg.cryptoanalyzer.client.binance.model.OpenInterest;
import com.cp.aitg.cryptoanalyzer.client.binance.request.ApiRequest;
import com.fasterxml.jackson.core.type.TypeReference;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BinanceMarketApi {
    private final BinanceApiClient client;
    
    public BinanceMarketApi(BinanceApiClient client) {
        this.client = client;
    }

    public List<Kline> getKlines(KlineRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", request.getSymbol());
        params.put("interval", request.getInterval());

        if (request.getStartTime() != null) {
            params.put("startTime", request.getStartTime().toString());
        }
        if (request.getEndTime() != null) {
            params.put("endTime", request.getEndTime().toString());
        }
        if (request.getLimit() != null) {
            params.put("limit", request.getLimit().toString());
        }

        List<List<Object>> klines= client.executeRequest(new ApiRequest<>(
            "/api/v3/klines",
            HttpMethod.GET,
            params,
            false,
            new TypeReference<List<List<Object>>>() {}
        ));
        return klines.stream()
                .map(this::mapToKline)
                .collect(Collectors.toList());
    }
    private Kline mapToKline(List<Object> data) {
        Kline candlestick = new Kline();
        candlestick.setOpenTime(Long.parseLong(data.get(0).toString()));
        candlestick.setOpen(new BigDecimal(data.get(1).toString()));
        candlestick.setHigh(new BigDecimal(data.get(2).toString()));
        candlestick.setLow(new BigDecimal(data.get(3).toString()));
        candlestick.setClose(new BigDecimal(data.get(4).toString()));
        candlestick.setVolume(new BigDecimal(data.get(5).toString()));
        candlestick.setCloseTime(Long.parseLong(data.get(6).toString()));
        candlestick.setQuoteAssetVolume(new BigDecimal(data.get(7).toString()));
        candlestick.setNumberOfTrades(Long.parseLong(data.get(8).toString()));
        candlestick.setTakerBuyBaseAssetVolume(new BigDecimal(data.get(9).toString()));
        candlestick.setTakerBuyQuoteAssetVolume(new BigDecimal(data.get(10).toString()));
        return candlestick;
    }

    // 获取持仓量数据
    public List<OpenInterest> getOpenInterest(String symbol, String period, Long startTime, Long endTime, Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);
        params.put("period", period); // "5m","15m","30m","1h","2h","4h","6h","12h","1d"
        if(startTime != null) params.put("startTime", startTime.toString());
        if(endTime != null) params.put("endTime", endTime.toString());
        if(limit != null) params.put("limit", limit.toString());

        return client.executeRequest(new ApiRequest<>(
                "/futures/data/openInterestHist",
                HttpMethod.GET,
                params,
                false,
                new TypeReference<List<OpenInterest>>() {}
        ));
    }
}
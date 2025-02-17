package com.cp.aitg.cryptoanalyzer.client.binance.market;

import com.cp.aitg.cryptoanalyzer.client.binance.client.BinanceApiClient;
import com.cp.aitg.cryptoanalyzer.client.binance.constant.HttpMethod;
import com.cp.aitg.cryptoanalyzer.client.binance.model.Position;
import com.cp.aitg.cryptoanalyzer.client.binance.request.ApiRequest;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collections;
import java.util.List;

public class BinancePositionApi {
    private final BinanceApiClient client;
    
    public BinancePositionApi(BinanceApiClient client) {
        this.client = client;
    }

    public List<Position> getPositions() {
        return client.executeRequest(new ApiRequest<>(
                "/fapi/v2/positionRisk",  // 期货API
                HttpMethod.GET,
                Collections.emptyMap(),
                true,  // 需要签名
                new TypeReference<List<Position>>() {}
        ));
    }
}
package com.cp.aitg.service;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BinanceSymbolService {

    private final SpotClient client;

    public BinanceSymbolService(SpotClient client) {
        // 如果不需要 API Key 访问公共 API，可以使用空字符串
        this.client = client;
    }

    public List<String> getAllTradingPairs() {
        // 调用 Binance API 获取交易对信息
        String response = client.createMarket().exchangeInfo(HashMap.newHashMap(1));

        // 解析 JSON
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray symbolsArray = jsonResponse.getJSONArray("symbols");

        List<String> tradingPairs = new ArrayList<>();
        for (int i = 0; i < symbolsArray.length(); i++) {
            JSONObject symbol = symbolsArray.getJSONObject(i);
            if ("TRADING".equals(symbol.getString("status"))) {
                tradingPairs.add(symbol.getString("symbol"));
            }
        }
        return tradingPairs;
    }
}

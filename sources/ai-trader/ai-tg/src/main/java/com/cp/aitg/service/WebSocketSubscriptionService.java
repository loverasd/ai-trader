package com.cp.aitg.service;

import com.binance.connector.client.WebSocketStreamClient;
import com.cp.aitg.binance.vo.TradeData;
import com.cp.aitg.binance.vo.TradeStreamMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSubscriptionService {
    private final WebSocketStreamClient wsClient;
    private final PriceAlertService priceAlertService;
    private final Map<Integer, String> streamIds = new ConcurrentHashMap<>(); // 记录订阅 ID
    private static final Logger log = LoggerFactory.getLogger(WebSocketSubscriptionService.class);
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson 对象映射器

    @Autowired
    public WebSocketSubscriptionService(WebSocketStreamClient wsClient, PriceAlertService priceAlertService) {
        this.wsClient = wsClient;
        this.priceAlertService = priceAlertService;
    }

    public void subscribeToTradingPairs(List<String> symbols) {
        // 每 100 个交易对使用一个连接，支持 400 个交易对分成 4 个连接
        int batchSize = 100;
        for (int i = 0; i < symbols.size(); i += batchSize) {
            int end = Math.min(i + batchSize, symbols.size());
            List<String> batch = symbols.subList(i, end);
            subscribeBatch(batch);
        }
    }

    private void subscribeBatch(List<String> symbols) {
        ArrayList<String> streams = new ArrayList<>();
        symbols.forEach(symbol -> streams.add(symbol.toLowerCase() + "@trade")); // 使用 trade 流

        int streamId = wsClient.combineStreams(
                streams,
                r -> log.info("WebSocket opened for streams: {}", streams),                    // 无参数
                this::onMessage,                                                               // String 参数
                (code, reason) -> log.info("WebSocket closing: code={}, reason={}", code, reason), // int, String
                (code, reason) -> log.info("WebSocket closed: code={}, reason={}", code, reason),  // int, String
                (throwable, response) -> log.error("WebSocket error: {}", throwable.getMessage())  // Throwable, Response
        );
        streamIds.put(streamId, String.join(",", streams));
        log.info("Subscribed to {} trading pairs with stream ID: {}", streams.size(), streamId);
    }

    private void onMessage(String message) {
        try {
            // 使用 Jackson 解析 JSON 到 TradeStreamMessage 对象
            TradeStreamMessage tradeMessage = objectMapper.readValue(message, TradeStreamMessage.class);
            TradeData data = tradeMessage.getData();
            String symbol = data.getSymbol();
            double price = data.getPriceAsDouble(); // 使用便捷方法获取 double 类型价格
            priceAlertService.checkPriceAlert(symbol, price);
            log.debug("Received trade update: symbol={}, price={}", symbol, price);
        } catch (Exception e) {
            log.error("Failed to parse WebSocket message: {}", message, e);
        }
    }

    @PreDestroy
    public void closeAllStreams() {
        streamIds.keySet().forEach(wsClient::closeConnection);
        log.info("Closed all WebSocket streams");
    }
}
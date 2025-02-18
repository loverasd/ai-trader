package com.cp.aitg;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.cp.aitg.config.ExchangeConfig;
import com.cp.aitg.cryptoanalyzer.client.binance.client.BinanceApiClient;
import com.cp.aitg.service.NotificationService;
import com.cp.aitg.service.WebSocketSubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest
class AiTgApplicationTests {

//    final Long start = LocalDateTime.now().minusDays(15).getSecond();
    final LocalDateTime end = LocalDateTime.now();
    @Autowired
    BinanceApiClient client;
    @Autowired
    ExchangeConfig exchangeConfig;

    @Autowired
    NotificationService notificationService;
    @Test
    void contextLoads() {
        SpotClient client = new SpotClientImpl(exchangeConfig.getBinanceApiKey(), exchangeConfig.getBinanceApiSecret());

        Map<String,Object> parameters = new LinkedHashMap<String,Object>();
        parameters.put("symbol","BTCUSDT");
        parameters.put("side", "SELL");
        parameters.put("type", "LIMIT");
        parameters.put("timeInForce", "GTC");
        parameters.put("quantity", 0.01);
        parameters.put("price", 9500);

        String result = client.createTrade().testNewOrder(parameters);
        System.out.println(result);
    }
    @Test
    void contextLoads2() {
        SpotClient client = new SpotClientImpl();
        Map<String, Object> parameters = new LinkedHashMap<>();
        String result = client.createMarket().exchangeInfo(parameters);
        System.out.printf("result: %s", result);
    }

    @Autowired
    WebSocketSubscriptionService socketSubscriptionService;

    @Test
    void contextLoads3() {
        notificationService.sendPriceAlertNotification("test",10000.00,10000.00);
    }


}

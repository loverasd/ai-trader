package com.cp.aitg.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WebSocketStartupListener {
    private final WebSocketSubscriptionService wsService;
    private final BinanceSymbolService symbolService;
    private final PriceAlertService priceAlertService;

    @Autowired
    public WebSocketStartupListener(WebSocketSubscriptionService wsService,
                                    BinanceSymbolService symbolService,
                                    PriceAlertService priceAlertService) {
        this.wsService = wsService;
        this.symbolService = symbolService;
        this.priceAlertService = priceAlertService;
    }

    @PostConstruct
    public void init() {
        List<String> tradingPairs = symbolService.getAllTradingPairs();
        List<String> limitedPairs = tradingPairs.stream().limit(400).collect(Collectors.toList()); // 限制 400 个
        wsService.subscribeToTradingPairs(limitedPairs);

        // 示例：设置几个交易对的目标价格
        priceAlertService.setPriceAlert("BTCUSDT", 50000.0);
        priceAlertService.setPriceAlert("ETHUSDT", 3000.0);
        // 可从配置文件或数据库加载更多
    }
}
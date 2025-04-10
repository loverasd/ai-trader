package com.cp.aitg.service;

import com.cp.aitg.config.PriceAlertConfig;
import com.cp.aitg.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PriceAlertService {
    private final Map<String, PriceAlertConfig> alerts = new ConcurrentHashMap<>();
    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(PriceAlertService.class);

    @Autowired
    public PriceAlertService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setPriceAlert(String symbol, double targetPrice) {
        alerts.put(symbol, new PriceAlertConfig(symbol, targetPrice));
        log.info("Set price alert for {} at {}", symbol, targetPrice);
    }

    public void checkPriceAlert(String symbol, double currentPrice) {
        PriceAlertConfig config = alerts.get(symbol);
        if (config != null && config.isTriggered(currentPrice)) {
            notificationService.sendPriceAlertNotification(symbol, config.getTargetPrice(), currentPrice);
            config.setNotified(true); // 标记为已通知
            log.info("Price alert triggered for {}: target={}, current={}", symbol, config.getTargetPrice(), currentPrice);
        }
    }
}
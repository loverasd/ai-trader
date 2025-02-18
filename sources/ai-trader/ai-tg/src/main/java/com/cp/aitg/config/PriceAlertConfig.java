package com.cp.aitg.config;

import lombok.Data;

@Data
public class PriceAlertConfig {
    private String symbol;        // 交易对，例如 "BTCUSDT"
    private double targetPrice;   // 目标价格
    private boolean notified;     // 是否已通知（避免重复通知）

    public PriceAlertConfig(String symbol, double targetPrice) {
        this.symbol = symbol;
        this.targetPrice = targetPrice;
        this.notified = false;
    }

    public boolean isTriggered(double currentPrice) {
        return !notified && Math.abs(currentPrice - targetPrice) < 0.01; // 允许小范围误差
    }
}
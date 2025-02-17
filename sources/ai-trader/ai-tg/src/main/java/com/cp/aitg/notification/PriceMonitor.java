package com.cp.aitg.notification;

import com.binance.connector.client.WebSocketStreamClient;
import com.binance.connector.client.utils.websocketcallback.WebSocketMessageCallback;
import org.json.JSONObject;

/**
 * PriceMonitor 负责利用 WebSocketStreamClient 订阅指定交易对的实时价格，
 * 并在达到预设目标价格时触发通知。
 */
public class PriceMonitor {

    private final WebSocketStreamClient wsClient;
    private final String symbol;
    private final double targetPrice;
    private final NotificationService notificationService;
    private boolean notified = false;
    private int streamId;

    /**
     * 构造函数
     * @param wsClient WebSocketStreamClient 实例，用于建立数据连接
     * @param symbol 交易对，例如 "BTCUSDT"
     * @param targetPrice 预设目标价格，当当前价格达到该值时触发通知
     * @param notificationService 通知服务，用于消息推送
     */
    public PriceMonitor(WebSocketStreamClient wsClient, String symbol, double targetPrice, NotificationService notificationService) {
        this.wsClient = wsClient;
        this.symbol = symbol;
        this.targetPrice = targetPrice;
        this.notificationService = notificationService;
    }

    /**
     * 启动价格监控，订阅指定交易对的实时 ticker 数据。
     */
    public void startMonitoring() {
        // 通过 symbolTicker 方法订阅实时 ticker 数据
        streamId = wsClient.symbolTicker(symbol, new WebSocketMessageCallback() {
            @Override
            public void onMessage(String message) {
                handleTickerMessage(message);
            }
        });
    }

    /**
     * 处理 WebSocket 返回的 ticker 数据消息。
     * 这里假设返回的 JSON 数据中包含字段 "c" 表示当前价格。
     * @param message JSON 格式的字符串
     */
    private void handleTickerMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            // 获取当前价格，注意币安返回数据可能为字符串类型，这里直接解析为 double
            double currentPrice = json.getDouble("c");
            System.out.println("[" + symbol + "] 当前价格：" + currentPrice);
            if (!notified && meetsCondition(currentPrice)) {
                notified = true;
                // 当满足价格条件后，调用通知服务
                String notifyMsg = String.format("交易对 %s 达到目标价格：%.8f (当前价格：%.8f)", symbol, targetPrice, currentPrice);
                notificationService.notify(notifyMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断当前价格是否满足触发条件（这里简单以 >= 目标价格为例）。
     * @param currentPrice 当前实时价格
     * @return 满足条件返回 true，否则 false
     */
    private boolean meetsCondition(double currentPrice) {
        return currentPrice >= targetPrice;
    }

    /**
     * 停止监控，关闭 WebSocket 连接。
     */
    public void stopMonitoring() {
        wsClient.closeConnection(streamId);
    }
}

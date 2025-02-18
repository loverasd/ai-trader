package com.cp.aitg.binance.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 表示 Binance WebSocket Trade 流的消息结构
 */
@Data
public class TradeStreamMessage {
    /**
     * 流的名称，例如 "ethusdt@trade"，表示订阅的交易对和流类型
     */
    @JsonProperty("stream")
    private String stream;

    /**
     * 交易数据详情，嵌套的实际交易信息
     */
    @JsonProperty("data")
    private TradeData data;


    @Override
    public String toString() {
        return "TradeStreamMessage{" +
                "stream='" + stream + '\'' +
                ", data=" + data +
                '}';
    }
}
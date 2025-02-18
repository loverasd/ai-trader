package com.cp.aitg.binance.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 表示 Binance Trade 流中 "data" 部分的交易数据
 */
@Data
public class TradeData {
    /**
     * 事件类型，固定为 "trade"，表示这是交易事件
     */
    @JsonProperty("e")
    private String eventType;

    /**
     * 事件时间戳（毫秒），表示事件发生的时间
     */
    @JsonProperty("E")
    private long eventTime;

    /**
     * 交易对符号，例如 "ETHUSDT"，表示交易的货币对
     */
    @JsonProperty("s")
    private String symbol;

    /**
     * 交易 ID，唯一标识本次交易
     */
    @JsonProperty("t")
    private long tradeId;

    /**
     * 成交价格，字符串格式，例如 "2701.09000000"
     */
    @JsonProperty("p")
    private String price;

    /**
     * 成交数量，字符串格式，例如 "0.32740000"
     */
    @JsonProperty("q")
    private String quantity;

    /**
     * 交易时间戳（毫秒），表示交易完成的服务器时间
     */
    @JsonProperty("T")
    private long tradeTime;

    /**
     * 是否为买家主动发起的交易（买家市场单），true 表示买单，false 表示卖单
     */
    @JsonProperty("m")
    private boolean isBuyerMaker;

    /**
     * 是否为最优匹配，通常为 true，表示交易是最优价格匹配
     */
    @JsonProperty("M")
    private boolean isBestMatch;


    /**
     * 提供便捷方法将价格转换为 double 类型
     */
    public double getPriceAsDouble() {
        return Double.parseDouble(price);
    }

    @Override
    public String toString() {
        return "TradeData{" +
                "eventType='" + eventType + '\'' +
                ", eventTime=" + eventTime +
                ", symbol='" + symbol + '\'' +
                ", tradeId=" + tradeId +
                ", price='" + price + '\'' +
                ", quantity='" + quantity + '\'' +
                ", tradeTime=" + tradeTime +
                ", isBuyerMaker=" + isBuyerMaker +
                ", isBestMatch=" + isBestMatch +
                '}';
    }
}
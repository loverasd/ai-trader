package com.cp.aitg.cryptoanalyzer.client.binance.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Position {
    private String symbol;          // 交易对
    private String positionAmt;     // 持仓数量
    private String entryPrice;      // 入场价格
    private String markPrice;       // 标记价格
    private String unRealizedProfit;// 未实现盈亏
    private String positionSide;    // 持仓方向 BOTH, LONG, SHORT
}
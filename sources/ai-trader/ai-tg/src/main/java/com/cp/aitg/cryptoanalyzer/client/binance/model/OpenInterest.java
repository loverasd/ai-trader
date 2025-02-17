package com.cp.aitg.cryptoanalyzer.client.binance.model;

import lombok.Data;
import lombok.ToString;
import java.math.BigDecimal;

/**
 * 币安持仓量（Open Interest）数据模型
 */
@Data // 自动生成 getter、setter、toString、equals 和 hashCode 方法
@ToString // 自动生成 toString 方法
public class OpenInterest {
    private String symbol;             // 交易对
    private Long timestamp;            // 时间戳
    private String sumOpenInterest;    // 持仓量(张)
    private String sumOpenInterestValue; // 持仓名义价值
}
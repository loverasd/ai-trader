package com.cp.aitg.cryptoanalyzer.client.binance.model.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

// 价格区间过滤器
@Data
@EqualsAndHashCode(callSuper = true)
public class PercentPriceFilter extends Filter {
    private BigDecimal multiplierUp;   // 上限价格倍数
    private BigDecimal multiplierDown; // 下限价格倍数
    private Integer avgPriceMins;      // 计算平均价格的时间窗口(分钟)

    public boolean validate(BigDecimal markPrice, BigDecimal orderPrice) {
        BigDecimal upperLimit = markPrice.multiply(multiplierUp);
        BigDecimal lowerLimit = markPrice.multiply(multiplierDown);
        return orderPrice.compareTo(lowerLimit) >= 0 
            && orderPrice.compareTo(upperLimit) <= 0;
    }
}
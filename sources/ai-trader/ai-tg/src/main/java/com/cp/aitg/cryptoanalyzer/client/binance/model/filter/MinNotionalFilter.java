package com.cp.aitg.cryptoanalyzer.client.binance.model.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

// 最小名义价值过滤器
@Data
@EqualsAndHashCode(callSuper = true)
public class MinNotionalFilter extends Filter {
    private BigDecimal minNotional;    // 最小交易金额(价格 * 数量)
    private Boolean applyToMarket;     // 是否应用于市价单
    private Integer avgPriceMins;      // 计算平均价格的时间窗口(分钟)

    public boolean validate(BigDecimal price, BigDecimal quantity) {
        return price.multiply(quantity).compareTo(minNotional) >= 0;
    }
}
package com.cp.aitg.cryptoanalyzer.client.binance.model.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

// 价格过滤器 - 控制下单价格的范围和精度
@Data
@EqualsAndHashCode(callSuper = true)
public class PriceFilter extends Filter {
    private BigDecimal minPrice;    // 最小价格
    private BigDecimal maxPrice;    // 最大价格
    private BigDecimal tickSize;    // 价格精度(步长)

    public boolean validate(BigDecimal price) {
        if (price.compareTo(minPrice) < 0 || price.compareTo(maxPrice) > 0) {
            return false;
        }
        
        // 检查价格是否符合精度要求
        BigDecimal remainder = price.subtract(minPrice)
            .divideToIntegralValue(tickSize);
        return remainder.multiply(tickSize).add(minPrice).compareTo(price) == 0;
    }
}
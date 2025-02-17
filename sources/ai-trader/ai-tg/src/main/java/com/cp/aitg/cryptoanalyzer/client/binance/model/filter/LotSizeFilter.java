package com.cp.aitg.cryptoanalyzer.client.binance.model.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

// 数量过滤器 - 控制下单数量的范围和精度
@Data
@EqualsAndHashCode(callSuper = true)
public class LotSizeFilter extends Filter {
    private BigDecimal minQty;     // 最小下单数量
    private BigDecimal maxQty;     // 最大下单数量
    private BigDecimal stepSize;   // 数量精度(步长)

    public boolean validate(BigDecimal quantity) {
        if (quantity.compareTo(minQty) < 0 || quantity.compareTo(maxQty) > 0) {
            return false;
        }
        
        // 检查数量是否符合精度要求
        BigDecimal remainder = quantity.subtract(minQty)
            .divideToIntegralValue(stepSize);
        return remainder.multiply(stepSize).add(minQty).compareTo(quantity) == 0;
    }
}
package com.cp.aitg.cryptoanalyzer.client.binance.model.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

// 市价单数量过滤器
@Data
@EqualsAndHashCode(callSuper = true)
public class MarketLotSizeFilter extends Filter {
    private BigDecimal minQty;
    private BigDecimal maxQty;
    private BigDecimal stepSize;

    public boolean validate(BigDecimal quantity) {
        return quantity.compareTo(minQty) >= 0 
            && quantity.compareTo(maxQty) <= 0;
    }
}
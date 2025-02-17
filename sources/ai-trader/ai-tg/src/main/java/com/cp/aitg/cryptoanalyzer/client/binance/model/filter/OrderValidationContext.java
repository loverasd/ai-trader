package com.cp.aitg.cryptoanalyzer.client.binance.model.filter;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderValidationContext {
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal markPrice;
    private boolean isMarketOrder;
    private int currentOrderCount;
}
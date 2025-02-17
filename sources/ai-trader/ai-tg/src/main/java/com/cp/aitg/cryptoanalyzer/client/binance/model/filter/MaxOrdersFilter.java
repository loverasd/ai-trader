package com.cp.aitg.cryptoanalyzer.client.binance.model.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

// 最大订单数量过滤器
@Data
@EqualsAndHashCode(callSuper = true)
public class MaxOrdersFilter extends Filter {
    private Integer maxNumOrders;  // 最大允许的订单数量

    public boolean validate(int currentOrders) {
        return currentOrders <= maxNumOrders;
    }
}
package com.cp.aitg.cryptoanalyzer.client.binance.model.filter;

import java.util.List;

// 过滤器工具类
public class FilterUtils {
    public static boolean validateOrder(List<Filter> filters, OrderValidationContext context) {
        return filters.stream().allMatch(filter -> {
            if (filter instanceof PriceFilter) {
                return ((PriceFilter) filter).validate(context.getPrice());
            } else if (filter instanceof LotSizeFilter) {
                return ((LotSizeFilter) filter).validate(context.getQuantity());
            } else if (filter instanceof MinNotionalFilter) {
                return ((MinNotionalFilter) filter).validate(
                    context.getPrice(), 
                    context.getQuantity()
                );
            }
            // ... 其他过滤器的验证逻辑
            return true;
        });
    }
}
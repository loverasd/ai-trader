package com.cp.aitg.cryptoanalyzer.client.binance.model.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "filterType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PriceFilter.class, name = "PRICE_FILTER"),
    @JsonSubTypes.Type(value = LotSizeFilter.class, name = "LOT_SIZE"),
    @JsonSubTypes.Type(value = MarketLotSizeFilter.class, name = "MARKET_LOT_SIZE"),
    @JsonSubTypes.Type(value = MaxOrdersFilter.class, name = "MAX_NUM_ORDERS"),
    @JsonSubTypes.Type(value = MinNotionalFilter.class, name = "MIN_NOTIONAL"),
    @JsonSubTypes.Type(value = PercentPriceFilter.class, name = "PERCENT_PRICE")
})
public abstract class Filter {
    private String filterType;
}
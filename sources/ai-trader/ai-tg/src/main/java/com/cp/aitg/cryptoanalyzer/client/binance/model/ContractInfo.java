package com.cp.aitg.cryptoanalyzer.client.binance.model;

import com.cp.aitg.cryptoanalyzer.client.binance.model.filter.Filter;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ContractInfo {
    private String symbol;
    private String pair;
    private String contractType;
    private Long deliveryDate;
    private BigDecimal onboardDate;
    private String status;
    private BigDecimal maintMarginPercent;
    private BigDecimal requiredMarginPercent;
    private String baseAsset;
    private String quoteAsset;
    private String marginAsset;
    private Integer pricePrecision;
    private Integer quantityPrecision;
    private List<Filter> filters;
}
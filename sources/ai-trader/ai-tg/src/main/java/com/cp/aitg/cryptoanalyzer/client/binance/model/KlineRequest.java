package com.cp.aitg.cryptoanalyzer.client.binance.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KlineRequest {
    private String symbol;
    private String interval;
    private Long startTime;
    private Long endTime;
    private Integer limit;
}
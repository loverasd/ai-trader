package com.cp.aitg.cryptoanalyzer.client.binance.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinanceApiError extends RuntimeException {
    private int code;
    private String msg;
    
    @JsonCreator
    public BinanceApiError(
        @JsonProperty("code") int code,
        @JsonProperty("msg") String msg
    ) {
        this.code = code;
        this.msg = msg;
    }
}
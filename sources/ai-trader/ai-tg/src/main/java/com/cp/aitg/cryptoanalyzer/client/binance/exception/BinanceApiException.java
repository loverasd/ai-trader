package com.cp.aitg.cryptoanalyzer.client.binance.exception;

public class BinanceApiException extends RuntimeException {
    private final int code;
    
    public BinanceApiException(String message, Throwable cause) {
        super(message, cause);
        this.code = -1;
    }
    
    public BinanceApiException(int code, String message) {
        super(message);
        this.code = code;
    }
}
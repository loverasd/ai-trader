package com.cp.aitg.config;

import com.cp.aitg.cryptoanalyzer.client.binance.client.BinanceApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "file:/Users/chenpan/.ssh/bian_private.cfg", ignoreResourceNotFound = false)
public class BinanceConfiguration {
    
    @Bean
    public BinanceApiClient binanceApiClient(
            @Value("${binance.api.key}") String apiKey,
            @Value("${binance.api.secret}") String apiSecret,
            @Value("${binance.base.url:https://api.binance.com}") String baseUrl,
            @Value("${binance.testnet.enabled:false}") boolean useTestnet,
            ObjectMapper objectMapper,
            OkHttpClient okHttpClient
    ) {
        return new BinanceApiClient.Builder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .useTestnet(useTestnet)
                .objectMapper(objectMapper)
                .httpClient(okHttpClient)
                .build();
    }
}
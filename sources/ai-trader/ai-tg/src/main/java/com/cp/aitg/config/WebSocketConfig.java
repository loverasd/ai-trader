package com.cp.aitg.config;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.binance.connector.client.WebSocketStreamClient;
import com.binance.connector.client.impl.WebSocketStreamClientImpl;

@Configuration
public class WebSocketConfig {

    @Bean
    public WebSocketStreamClient webSocketStreamClient() {
        return new WebSocketStreamClientImpl();
    }

    @Bean
    public SpotClient binanceClient() {
        return new SpotClientImpl(System.getProperty("binance.api.key"), System.getProperty("binance.api.secret"));
    }


}

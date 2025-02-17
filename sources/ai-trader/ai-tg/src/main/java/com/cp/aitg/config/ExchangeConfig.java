package com.cp.aitg.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Getter
@Setter
@PropertySource(value = "file:/Users/chenpan/.ssh/bian_private.cfg", ignoreResourceNotFound = false)
public class ExchangeConfig {
    @Value("${binance.api.key}")
    private String binanceApiKey;
    
    @Value("${binance.api.secret}")
    private String binanceApiSecret;
    @Value("${binance.base.url}")
    private String binanceBaseUrl = "https://api.binance.com";
//    TESTNET_API_URL
    @Value("${binance.testnet.base.url}")
    private String binanceTestnetBaseUrl = "https://testnet.binance.vision";
    @Value("${okx.api.key}")
    private String okxApiKey;
    
    @Value("${okx.api.secret}")
    private String okxApiSecret;
    
    @Value("${telegram.bot.token}")
    private String telegramBotToken;
    
    @Value("${telegram.chat.id}")
    private String telegramChatId;
}

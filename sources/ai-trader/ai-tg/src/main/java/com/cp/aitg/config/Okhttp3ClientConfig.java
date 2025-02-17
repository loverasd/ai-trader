package com.cp.aitg.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Builder
@AllArgsConstructor
@Configuration
public class Okhttp3ClientConfig {
    @Builder.Default
    private boolean useTestnet = false;
    
    @Builder.Default
    private long connectTimeout = 10_000;
    
    @Builder.Default
    private long readTimeout = 30_000;
    
    @Builder.Default
    private long writeTimeout = 30_000;
    
    @Builder.Default
    private boolean proxyEnabled = false;
    
    @Builder.Default
    private int maxRetries = 3;
    
    private String proxyHost;
    private int proxyPort;

    public Okhttp3ClientConfig() {

    }
}
package com.cp.aitg.config;

import com.cp.aitg.cryptoanalyzer.client.common.RateLimitInterceptor;
import com.cp.aitg.cryptoanalyzer.client.common.RetryInterceptor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

    private static final int MAX_IDLE_CONNECTIONS = 10;
    private static final long KEEP_ALIVE_DURATION = 5L;

    @Bean
    public OkHttpClient okHttpClient(Okhttp3ClientConfig config) {
        ConnectionPool connectionPool = new ConnectionPool(
            MAX_IDLE_CONNECTIONS,
            KEEP_ALIVE_DURATION,
            TimeUnit.MINUTES
        );

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectionPool(connectionPool)
            .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
            .writeTimeout(config.getWriteTimeout(), TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true);

        // 添加重试拦截器
        builder.addInterceptor(new RetryInterceptor(config.getMaxRetries()));

        // 添加速率限制拦截器
        builder.addInterceptor(new RateLimitInterceptor());

        // 配置代理
        if (config.isProxyEnabled()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress(config.getProxyHost(), config.getProxyPort()));
            builder.proxy(proxy);
        }

        return builder.build();
    }

}
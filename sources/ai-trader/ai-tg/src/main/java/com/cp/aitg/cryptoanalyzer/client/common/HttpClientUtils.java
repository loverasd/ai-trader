package com.cp.aitg.cryptoanalyzer.client.common;

import com.cp.aitg.config.Okhttp3ClientConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;


import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpClientUtils {
    private static final int MAX_IDLE_CONNECTIONS = 10;
    private static final long KEEP_ALIVE_DURATION = 5L;
    
    public static OkHttpClient createHttpClient(Okhttp3ClientConfig config) {
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

    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
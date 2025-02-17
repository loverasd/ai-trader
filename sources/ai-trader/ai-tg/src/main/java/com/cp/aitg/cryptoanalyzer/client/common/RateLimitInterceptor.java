package com.cp.aitg.cryptoanalyzer.client.common;

import com.google.common.util.concurrent.RateLimiter;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public class RateLimitInterceptor implements Interceptor {
    private final RateLimiter rateLimiter;
    
    public RateLimitInterceptor() {
        // 默认限制为每秒10个请求
        this.rateLimiter = RateLimiter.create(10.0);
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        rateLimiter.acquire();
        return chain.proceed(chain.request());
    }
}
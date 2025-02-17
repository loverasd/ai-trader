package com.cp.aitg.cryptoanalyzer.client.common;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Random;

public class RetryInterceptor implements Interceptor {
    private final int maxRetries;
    private final Random random = new Random();

    public RetryInterceptor(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        int retryCount = 0;
        Response response = null;
        
        while (retryCount <= maxRetries) {
            try {
                response = chain.proceed(request);
                if (response.isSuccessful() || !isRetryable(response.code())) {
                    return response;
                }
            } catch (IOException e) {
                if (retryCount == maxRetries) throw e;
            }

            if (response != null) {
                response.close();
            }

            retryCount++;
            // 指数退避策略
            long backoffTime = (long) (Math.pow(2, retryCount) * 1000 + random.nextInt(1000));
            try {
                Thread.sleep(backoffTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        
        return response;
    }

    private boolean isRetryable(int code) {
        return code == 429 || code == 500 || code == 502 || code == 503 || code == 504;
    }
}
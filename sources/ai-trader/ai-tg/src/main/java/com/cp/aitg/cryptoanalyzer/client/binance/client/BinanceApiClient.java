package com.cp.aitg.cryptoanalyzer.client.binance.client;

import com.cp.aitg.cryptoanalyzer.client.binance.constant.HttpMethod;
import com.cp.aitg.cryptoanalyzer.client.binance.exception.BinanceApiError;
import com.cp.aitg.cryptoanalyzer.client.binance.exception.BinanceApiException;
import com.cp.aitg.cryptoanalyzer.client.binance.market.BinanceMarketApi;
import com.cp.aitg.cryptoanalyzer.client.binance.market.BinancePositionApi;
import com.cp.aitg.cryptoanalyzer.client.binance.model.ServerTime;
import com.cp.aitg.cryptoanalyzer.client.binance.request.ApiRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BinanceApiClient {
    private static final String API_URL = "https://api.binance.com";
    private static final String TESTNET_API_URL = "https://testnet.binance.vision";
    private final BinanceMarketApi marketApi;
    private final String apiKey;
    private final String apiSecret;
    private final String baseUrl;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TimeSyncManager timeSync;
    private final SignatureManager signature;
    private final BinancePositionApi positionApi;

    private BinanceApiClient(Builder builder) {
        this.apiKey = builder.apiKey;
        this.apiSecret = builder.apiSecret;
        this.baseUrl = builder.useTestnet ? TESTNET_API_URL : API_URL;
        this.httpClient = builder.httpClient;
        this.objectMapper = builder.objectMapper;
        this.timeSync = new TimeSyncManager();
        this.signature = new SignatureManager(apiSecret);
        this.marketApi = new BinanceMarketApi(this);
        this.positionApi = new BinancePositionApi(this);
    }
    public BinanceMarketApi market() {
        return marketApi;
    }
    public BinancePositionApi position() {
        return positionApi;
    }
    // Inner class for time synchronization
    private class TimeSyncManager {
        private static final long TIME_OFFSET_REFRESH_INTERVAL = 1800000; // 30 minutes
        private volatile long serverTimeOffset = 0;
        private volatile long lastTimeOffsetCheck = 0;

        private void syncTimeOffset() {
            try {
                ServerTime serverTime = executeRequest(
                    new ApiRequest<>(
                        "/api/v3/time",
                        HttpMethod.GET,
                        Collections.emptyMap(),
                        false,
                        new TypeReference<ServerTime>() {}
                    )
                );

                long currentTime = System.currentTimeMillis();
                serverTimeOffset = serverTime.getServerTime() - currentTime;
                lastTimeOffsetCheck = currentTime;
            } catch (Exception e) {
                throw new BinanceApiException("Failed to sync time offset", e);
            }
        }

        private long getServerTimeOffset() {
            if (System.currentTimeMillis() - lastTimeOffsetCheck > TIME_OFFSET_REFRESH_INTERVAL) {
                syncTimeOffset();
            }
            return serverTimeOffset;
        }
    }

    // Inner class for signature generation
    private class SignatureManager {
        private final Mac hmacSha256;

        private SignatureManager(String apiSecret) {
            try {
                hmacSha256 = Mac.getInstance("HmacSHA256");
                hmacSha256.init(new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256"));
            } catch (Exception e) {
                throw new BinanceApiException("Error initializing signature manager", e);
            }
        }

        private String generateSignature(String payload) {
            try {
                return Hex.encodeHexString(hmacSha256.doFinal(payload.getBytes()));
            } catch (Exception e) {
                throw new BinanceApiException("Error generating signature", e);
            }
        }
    }

    // Core request execution method
    public   <T> T executeRequest(ApiRequest<T> request) {
        try {
            String fullUrl = baseUrl + request.getPath();
            Map<String, Object> params = new HashMap<>(request.getParams());
            
            // Add timestamp and signature for authenticated requests
            if (request.isSecurityRequired()) {
                params.put("timestamp", String.valueOf(System.currentTimeMillis() + timeSync.getServerTimeOffset()));
                String queryString = buildQueryString(params);
                params.put("signature", signature.generateSignature(queryString));
            }

            // Build URL with parameters
            String queryString = buildQueryString(params);
            if (!queryString.isEmpty()) {
                fullUrl += "?" + queryString;
            }

            // Build and execute request
            Request.Builder requestBuilder = new Request.Builder()
                .url(fullUrl)
                .header("X-MBX-APIKEY", apiKey);

            if (request.getMethod() == HttpMethod.POST) {
                requestBuilder.post(RequestBody.create(MediaType.parse("application/json"), ""));
            } else {
                requestBuilder.get();
            }

            Response response = httpClient.newCall(requestBuilder.build()).execute();
            return handleResponse(response, request.getResponseType());
        } catch (IOException e) {
            throw new BinanceApiException("Error executing request", e);
        }
    }

    private String buildQueryString(Map<String, Object> params) {
        return params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }

    private <T> T handleResponse(Response response, TypeReference<T> responseType) throws IOException {
        String body = response.body().string();

        if (!response.isSuccessful()) {
            BinanceApiError error = objectMapper.readValue(body, BinanceApiError.class);
            throw new BinanceApiException(error.getCode(), error.getMessage());
        }

        JavaType javaType = objectMapper.getTypeFactory().constructType(responseType);
        Object o = objectMapper.readValue(body, javaType);
        return (T) o;

    }

    // Builder pattern for client construction
    public static class Builder {
        private String apiKey;
        private String apiSecret;
        private boolean useTestnet;
        private OkHttpClient httpClient;
        private ObjectMapper objectMapper;

        public Builder() {
            this.objectMapper = new ObjectMapper();
            this.httpClient = new OkHttpClient();
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder apiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
            return this;
        }

        public Builder useTestnet(boolean useTestnet) {
            this.useTestnet = useTestnet;
            return this;
        }

        public Builder httpClient(OkHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public BinanceApiClient build() {
            return new BinanceApiClient(this);
        }
    }
}
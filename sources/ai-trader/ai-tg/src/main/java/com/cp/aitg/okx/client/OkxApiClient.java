package com.cp.aitg.okx.client;

import com.cp.aitg.config.OkxConfig;
import com.cp.aitg.okx.common.OkxApiConstants;
import com.cp.aitg.okx.dto.OkxResponse; // 你需要创建这个泛型响应类
import com.cp.aitg.okx.exception.OkxApiException; // 自定义异常
import com.cp.aitg.okx.util.OkxSignatureUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter; // Guava RateLimiter
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.core.type.TypeReference; // 或者使用 JavaType
import com.fasterxml.jackson.databind.JavaType;


@Slf4j
@Component
public class OkxApiClient {

    private final OkHttpClient httpClient;
    private final OkxConfig okxConfig;
    private final ObjectMapper objectMapper; // Jackson ObjectMapper

    // 示例：为下单和查询分别设置速率限制 (根据 OKX 文档调整速率)
    // 下单类接口通常限制更严格
    @SuppressWarnings("UnstableApiUsage")
    private RateLimiter placeOrderRateLimiter = RateLimiter.create(10.0 / 60.0); // 假设 10次/分钟
    @SuppressWarnings("UnstableApiUsage")
    private RateLimiter queryRateLimiter = RateLimiter.create(20.0 / 60.0); // 假设 20次/分钟

    @Autowired
    public OkxApiClient(OkxConfig okxConfig, ObjectMapper objectMapper) {
        this.okxConfig = okxConfig;
        this.objectMapper = objectMapper;
        // 假设 Clash 提供的是 HTTP 代理 (这是常见的模式)
        // 如果 Clash 提供的是 SOCKS 代理，请使用 Proxy.Type.SOCKS
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7897)); // 使用你的 Clash 地址和端口

        this.httpClient = new OkHttpClient.Builder()
                .proxy(proxy) // <--- 设置代理
                .connectTimeout(Duration.ofSeconds(okxConfig.getConnectTimeoutSeconds())) // 连接超时适当延长，因为经过了代理
                .readTimeout(Duration.ofSeconds(okxConfig.getReadTimeoutSeconds()))    // 读取超时也适当延长
                // 可以添加代理、拦截器等
                .build();
    }

    /**
     * 发送 GET 请求
     *
     * @param path   API 路径 (e.g., /api/v5/account/positions)
     * @param params URL 查询参数 Map
     * @param <T>    期望的响应数据类型
     * @return OkxResponse<T> 包装的响应
     * @throws OkxApiException API 调用失败
     */
    public <T> OkxResponse<T> get(String path, Map<String, String> params, JavaType responseDataType) throws OkxApiException {    queryRateLimiter.acquire(); // 获取许可，否则阻塞

        HttpUrl.Builder urlBuilder = HttpUrl.parse(okxConfig.getBaseUrl() + path).newBuilder();
        if (params != null && !params.isEmpty()) {
            params.forEach(urlBuilder::addQueryParameter);
        }

        String requestPathWithQuery = path + (params != null && !params.isEmpty() ? "?" + buildQueryString(params) : "");
        String timestamp = OkxSignatureUtil.currentTimestamp();
        String signature = OkxSignatureUtil.sign(timestamp, "GET", requestPathWithQuery, "", okxConfig.getApiSecret());
        Request request = buildRequest("GET", urlBuilder.build(), null, timestamp, signature);

        return executeRequest(request, responseDataType);
    }
    /**
     * 发送 GET 请求 (兼容旧的使用 Class<T> 的方式，用于简单类型)
     * 推荐使用带有 JavaType 的版本来处理 List
     */
    @Deprecated // 建议使用 get(String, Map, JavaType)
    public <T> OkxResponse<T> get(String path, Map<String, String> params, Class<T> dataType) throws OkxApiException {
        JavaType specificType = objectMapper.getTypeFactory().constructType(dataType);
        JavaType responseType = objectMapper.getTypeFactory().constructParametricType(OkxResponse.class, specificType);
        return get(path, params, responseType); // 调用新的 get 方法
    }

    /**
     * 发送 POST 请求
     *
     * @param path   API 路径
     * @param body   请求体对象 (会被序列化为 JSON)
     * @param <T>    期望的响应数据类型
     * @return OkxResponse<T> 包装的响应
     * @throws OkxApiException API 调用失败
     */
    public <T> OkxResponse<T> post(String path, Object body, JavaType responseDataType) throws OkxApiException {
        // 根据 path 判断使用哪个 RateLimiter
        if (path.contains("/trade/order")) { // 粗略判断，可以更精细
             placeOrderRateLimiter.acquire();
        } else {
             queryRateLimiter.acquire();
        }


        String bodyJson = "";
        if (body != null) {
            try {
                bodyJson = objectMapper.writeValueAsString(body);
            } catch (JsonProcessingException e) {
                log.error("序列化请求体失败: {}", body, e);
                throw new OkxApiException("请求体序列化失败", e);
            }
        }

        String timestamp = OkxSignatureUtil.currentTimestamp();
        String signature = OkxSignatureUtil.sign(timestamp, "POST", path, bodyJson, okxConfig.getApiSecret());
        RequestBody requestBody = RequestBody.create(bodyJson, MediaType.parse(OkxApiConstants.APPLICATION_JSON));
        HttpUrl url = HttpUrl.parse(okxConfig.getBaseUrl() + path);

        Request request = buildRequest("POST", url, requestBody, timestamp, signature);
        OkxResponse<T> objectOkxResponse = executeRequest(request, responseDataType);
        return objectOkxResponse;
    }
    /**
     * 发送 POST 请求 (兼容旧的使用 Class<T> 的方式，用于简单类型)
     * 推荐使用带有 JavaType 的版本来处理 List
     */
    @Deprecated // 建议使用 post(String, Object, JavaType)
    public <T> OkxResponse<T> post(String path, Object body, Class<T> dataType) throws OkxApiException {
        JavaType specificType = objectMapper.getTypeFactory().constructType(dataType);
        JavaType responseType = objectMapper.getTypeFactory().constructParametricType(OkxResponse.class, specificType);
        return post(path, body, responseType); // 调用新的 post 方法
    }


    // --- 私有辅助方法 ---

    private Request buildRequest(String method, HttpUrl url, RequestBody body, String timestamp, String signature) {        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader(OkxApiConstants.OK_ACCESS_KEY_HEADER, okxConfig.getApiKey())
                .addHeader(OkxApiConstants.OK_ACCESS_SIGN_HEADER, signature)
                .addHeader(OkxApiConstants.OK_ACCESS_TIMESTAMP_HEADER, timestamp)
                .addHeader(OkxApiConstants.OK_ACCESS_PASSPHRASE_HEADER, okxConfig.getApiPassphrase())
                .addHeader(OkxApiConstants.CONTENT_TYPE_HEADER, OkxApiConstants.APPLICATION_JSON);

        if (okxConfig.isSimulatedTrading()) {
            builder.addHeader(OkxApiConstants.OK_SIMULATED_TRADING_HEADER, "1"); // 启用模拟盘
        }

        if ("GET".equalsIgnoreCase(method)) {
            builder.get();
        } else if ("POST".equalsIgnoreCase(method)) {
            builder.post(body == null ? RequestBody.create(new byte[0]) : body); // POST 即使没 body 也需要设置
        } // 可以添加 DELETE 等其他方法

        return builder.build();
    }


    private <T> OkxResponse<T> executeRequest(Request request, JavaType responseDataType) throws OkxApiException {
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBodyString = response.body() != null ? response.body().string() : null;
            log.debug("OKX API Response: Code={}, URL={}, Body={}", response.code(), request.url(), responseBodyString);

            if (!response.isSuccessful()) {
                log.error("OKX API 请求失败: Code={}, Message={}, URL={}, ResponseBody={}",
                        response.code(), response.message(), request.url(), responseBodyString);
                // 可以根据 response.code() 抛出更具体的异常
                 throw new OkxApiException("HTTP 错误: " + response.code() + " " + response.message(), response.code(), responseBodyString);
            }

            if (responseBodyString == null || responseBodyString.isEmpty()) {
                 throw new OkxApiException("OKX 返回空的响应体", response.code(), null);
            }

            // 使用传入的 JavaType 进行反序列化
            // 注意：这里 responseDataType 应该是 OkxResponse<List<YourDto>> 这种完整的类型
            OkxResponse<T> okxResponse = objectMapper.readValue(responseBodyString, responseDataType);

            // 检查 OKX 业务错误码 (业务错误不抛异常，直接返回给上层判断)
            if (!OkxApiConstants.SUCCESS_CODE.equals(okxResponse.getCode())) {
                log.warn("OKX API 业务错误: Code={}, Msg={}, RequestURL={}", okxResponse.getCode(), okxResponse.getMsg(), request.url());
            }
            return okxResponse;

        } catch (IOException e) {
            log.error("执行 OKX API 请求时发生 IO 异常: URL={}, Method={}", request.url(), request.method(), e);
            throw new OkxApiException("网络请求失败: " + e.getMessage(), e);
        }
    }

     private String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        params.forEach((key, value) -> {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key).append("=").append(value); // 注意：实战中可能需要 URL Encode value
        });
        return sb.toString();
    }

     // --- 你需要创建 OkxResponse 和 OkxApiException 类 ---
     // 示例:
     /*
     @Data
     public class OkxResponse<T> {
         private String code; // "0" 表示成功
         private String msg;
         private T data; // 实际数据列表或对象
         // 可能还有分页等信息
     }

     public class OkxApiException extends RuntimeException {
         private int httpStatusCode = -1;
         private String okxErrorCode;
         private String responseBody;

         public OkxApiException(String message) { super(message); }
         public OkxApiException(String message, Throwable cause) { super(message, cause); }
         public OkxApiException(String message, int httpStatusCode, String responseBody) {
             super(message);
             this.httpStatusCode = httpStatusCode;
             this.responseBody = responseBody;
         }
         public OkxApiException(String okxMessage, String okxErrorCode, String responseBody) {
             super(okxMessage);
             this.okxErrorCode = okxErrorCode;
             this.responseBody = responseBody;
         }
         // Getters...
     }
     */
}
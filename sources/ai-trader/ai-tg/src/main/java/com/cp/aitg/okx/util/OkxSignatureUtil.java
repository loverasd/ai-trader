package com.cp.aitg.okx.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class OkxSignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";
    // OKX 要求的时间格式: ISO 8601 UTC
    private static final DateTimeFormatter ISO8601_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    /**
     * 生成 OKX API V5 签名.
     *
     * @param timestamp   ISO 8601 UTC 时间戳字符串 (例如: 2023-10-27T10:30:00.123Z)
     * @param method      HTTP 方法 (大写, e.g., "GET", "POST")
     * @param requestPath 请求路径 (e.g., "/api/v5/trade/order")
     * @param body        请求体 (对于 POST 请求, 如果是 GET 或无 body 则为空字符串 "")
     * @param apiSecret   用户的 API Secret
     * @return Base64 编码的签名字符串
     * @throws RuntimeException 如果签名过程中发生错误
     */
    public static String sign(String timestamp, String method, String requestPath, String body, String apiSecret) {
        String message = timestamp + method.toUpperCase() + requestPath + (body == null ? "" : body);
        try {
            Mac sha256_HMAC = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("生成 OKX API 签名失败. Message: {}, Secret: [PROTECTED]", message, e);
            throw new RuntimeException("无法生成 API 签名", e);
        }
    }

    /**
     * 获取当前时间的 ISO 8601 UTC 格式字符串.
     * @return 时间戳字符串
     */
    public static String currentTimestamp() {
        return Instant.now().atOffset(ZoneOffset.UTC).format(ISO8601_FORMATTER);
    }

    /**
     * 生成客户端订单ID。如果用户提供了 clOrdId，则直接使用；否则生成唯一的标识。
     *
     * @param providedClOrdId 用户传入的客户端订单ID
     * @return 最终使用的客户端订单ID
     */
    public static String generateClOrdId(String providedClOrdId) {
        if (StringUtils.hasText(providedClOrdId)) {
            return providedClOrdId;
        }
        // 简单示例：使用当前时间戳和随机数生成
        return "cl" + System.currentTimeMillis() + "" + (int) (Math.random() * 1000);
    }
}
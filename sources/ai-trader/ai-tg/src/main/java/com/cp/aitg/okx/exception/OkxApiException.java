package com.cp.aitg.okx.exception;

/**
 * 自定义异常类，用于表示与 OKX API 交互时发生的错误.
 * 包含 HTTP 状态码、OKX 业务错误码和原始响应体（如果可用）。
 */
public class OkxApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer httpStatusCode; // HTTP 响应状态码 (例如 4xx, 5xx)
    private final String okxErrorCode;   // OKX 业务错误码 (例如 "51001")
    private final String responseBody;   // 原始的错误响应体 (JSON 字符串)

    /**
     * 构造函数，用于一般性错误或网络错误.
     * @param message 错误描述
     */
    public OkxApiException(String message) {
        super(message);
        this.httpStatusCode = 51001;
        this.okxErrorCode = null;
        this.responseBody = null;
    }

    /**
     * 构造函数，用于一般性错误或网络错误，并包含原始异常.
     * @param message 错误描述
     * @param cause   原始异常
     */
    public OkxApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = null;
        this.okxErrorCode = null;
        this.responseBody = null;
    }

    /**
     * 构造函数，用于表示 HTTP 协议层面的错误.
     * @param message        错误描述 (例如 "HTTP 错误: 400 Bad Request")
     * @param httpStatusCode HTTP 状态码
     * @param responseBody   原始响应体
     */
    public OkxApiException(String message, int httpStatusCode, String responseBody) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.okxErrorCode = null; // HTTP 错误通常没有 OKX 业务码
        this.responseBody = responseBody;
    }

    /**
     * 构造函数，用于表示 OKX 返回的业务逻辑错误 (HTTP 状态码通常是 200).
     * @param okxMessage     OKX 返回的错误信息 (来自 msg 字段)
     * @param okxErrorCode   OKX 返回的错误码 (来自 code 字段)
     * @param responseBody   原始响应体
     */
    public OkxApiException(String okxMessage, String okxErrorCode, String responseBody) {
        super(okxMessage != null ? okxMessage : "OKX Business Error Code: " + okxErrorCode); // 使用 OKX 消息作为异常消息
        this.httpStatusCode = null; // 业务错误时 HTTP 状态码通常是 200，所以设为 null
        this.okxErrorCode = okxErrorCode;
        this.responseBody = responseBody;
    }

    /**
     * 获取 HTTP 状态码 (如果适用).
     * @return HTTP 状态码，如果错误非 HTTP 相关则返回 null.
     */
    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * 获取 OKX 业务错误码 (如果适用).
     * @return OKX 错误码字符串，如果错误非 OKX 业务错误则返回 null.
     */
    public String getOkxErrorCode() {
        return okxErrorCode;
    }

    /**
     * 获取原始的错误响应体 (如果可用).
     * @return 响应体字符串，如果没有则返回 null.
     */
    public String getResponseBody() {
        return responseBody;
    }

    @Override
    public String toString() {
        return "OkxApiException{" +
                "message='" + getMessage() + '\'' +
                ", httpStatusCode=" + httpStatusCode +
                ", okxErrorCode='" + okxErrorCode + '\'' +
                ", responseBody='" + (responseBody != null ? (responseBody.length() > 100 ? responseBody.substring(0, 100) + "..." : responseBody) : "null") + '\'' +
                '}';
    }
}
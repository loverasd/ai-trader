package com.cp.aitg.cryptoanalyzer.client.binance.request;

import com.cp.aitg.cryptoanalyzer.client.binance.constant.HttpMethod;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Collections;
import java.util.Map;


public class ApiRequest<T> {
    private final String path; // 请求路径
    private final HttpMethod method; // HTTP 方法
    private final Map<String, Object> params; // 请求参数
    private final boolean signed; // 是否需要签名
    private final TypeReference<T> responseType; // 响应类型
    public ApiRequest(String path,
                      HttpMethod method,
                      Map<String, Object> params,
                      boolean signed,
                      TypeReference<T> responseType) {
        this.path = path;
        this.method = method;
        this.params = params != null ? params : Collections.emptyMap();
        this.signed = signed;
        this.responseType = responseType;
    }

    private JavaType constructJavaType(TypeReference<T> typeReference) {
        return TypeFactory.defaultInstance().constructType(typeReference.getType());
    }

    // Getters
    public String getPath() { return path; }
    public HttpMethod getMethod() { return method; }
    public Map<String, Object> getParams() { return params; }
    public boolean isSecurityRequired() { return signed; }
    public TypeReference<T> getResponseType() { return responseType; }  // 返回 JavaType
}
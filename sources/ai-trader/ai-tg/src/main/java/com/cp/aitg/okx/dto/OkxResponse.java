package com.cp.aitg.okx.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 需要无参构造函数供 Jackson 反序列化
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略未知字段，增加兼容性
public class OkxResponse<T> {
    /**
     * 错误码，请求成功时为"0"
     */
    private String code;

    /**
     * 错误信息，请求成功时为空
     */
    private String msg;

    /**
     * 请求接口返回的数据主体。通常是 List 或单个对象。
     * 注意：当 data 是 List 时，反序列化需要特殊处理 (使用 TypeReference 或 JavaType)
     */
    private T data;

    // 可选：有时响应会包含 inTime 和 outTime
    // private String inTime;
    // private String outTime;
}
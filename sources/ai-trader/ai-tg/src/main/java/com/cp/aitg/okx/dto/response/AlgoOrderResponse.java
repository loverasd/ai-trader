package com.cp.aitg.okx.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlgoOrderResponse {
    /**
     * 订单ID。
     * 如果下单成功，OKX 会为该订单分配一个唯一的 ID。
     * (下单成功时返回)
     */
    @JsonProperty("ordId")
    private String ordId;


    /**
     * 策略委托单ID
     */
    @JsonProperty("algoId")
    private String algoId;

    /**
     * 客户自定义策略委托单ID
     */
    @JsonProperty("algoClOrdId")
    private String algoClOrdId; // 注意：下单请求用 clOrdId, 响应里可能有 algoClOrdId

    /**
     * 客户自定义订单ID (如果下单时传入了 clOrdId)
     * 注意：文档有时不清晰，确认下单响应中是否包含原始 clOrdId 还是只有 algoClOrdId
     */
    @JsonProperty("clOrdId")
    private String clOrdId;



    /**
     * 结果代码，0 代表成功
     */
    @JsonProperty("sCode")
    private String sCode;

    /**
     * 结果消息
     */
    @JsonProperty("sMsg")
    private String sMsg;

    /**
     * 标签
     */
    @JsonProperty("tag")
    private String tag;
}
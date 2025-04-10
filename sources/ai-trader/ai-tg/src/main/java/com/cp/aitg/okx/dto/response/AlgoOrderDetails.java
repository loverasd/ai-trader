package com.cp.aitg.okx.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlgoOrderDetails {

    @JsonProperty("algoId")
    private String algoId; // 策略委托ID

    @JsonProperty("clOrdId")
    private String clOrdId; // 客户自定义订单ID (下单时传的)

    @JsonProperty("algoClOrdId")
    private String algoClOrdId; // 客户自定义策略委托ID (如果下单时传了)

    @JsonProperty("instType")
    private String instType; // 产品类型 SWAP, FUTURES, SPOT, OPTION

    @JsonProperty("instId")
    private String instId;   // 产品ID

    @JsonProperty("ordType")
    private String ordType; // 策略订单类型 conditional, oco, trigger, move_order_stop, iceberg, twap

    @JsonProperty("sz")
    private String sz; // 委托数量

    @JsonProperty("state")
    private String state; // 策略委托状态: live, paused, canceled, effective, partially_effective, order_failed

    @JsonProperty("side")
    private String side; // 订单方向 buy, sell

    @JsonProperty("posSide")
    private String posSide; // 持仓方向 long, short, net

    @JsonProperty("tdMode")
    private String tdMode; // 交易模式 cross, isolated, cash

    @JsonProperty("actualSz")
    private String actualSz; // 实际成交数量 (对于触发后成交的)

    @JsonProperty("actualPx")
    private String actualPx; // 实际成交均价 (对于触发后成交的)

    @JsonProperty("slTriggerPx")
    private String slTriggerPx; // 止损触发价

    @JsonProperty("slOrdPx")
    private String slOrdPx; // 止损委托价

    @JsonProperty("slTriggerPxType")
    private String slTriggerPxType; // 止损触发类型 last, index, mark

    @JsonProperty("tpTriggerPx")
    private String tpTriggerPx; // 止盈触发价

    @JsonProperty("tpOrdPx")
    private String tpOrdPx; // 止盈委托价

    @JsonProperty("tpTriggerPxType")
    private String tpTriggerPxType; // 止盈触发类型

    @JsonProperty("triggerPx")
    private String triggerPx; // 触发价格 (用于计划委托等)

    @JsonProperty("triggerPxType")
    private String triggerPxType; // 触发价格类型

    @JsonProperty("cTime")
    private String cTime; // 创建时间 (毫秒级时间戳)

    @JsonProperty("triggerTime")
    private String triggerTime; // 触发时间 (毫秒级时间戳)

    @JsonProperty("tag")
    private String tag; // 标签

    // ... 可能还有其他与特定策略类型相关的字段，如 OCO 参数、移动止损参数等
    // 务必对照文档补充需要的字段
}
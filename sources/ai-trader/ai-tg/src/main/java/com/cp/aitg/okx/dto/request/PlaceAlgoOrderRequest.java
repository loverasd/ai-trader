package com.cp.aitg.okx.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder // 使用 Builder 模式方便构建
@JsonInclude(JsonInclude.Include.NON_NULL) // 忽略 null 字段
public class PlaceAlgoOrderRequest {

    @JsonProperty("instId")
    private String instId; // 产品ID，如 BTC-USDT-SWAP

    @JsonProperty("tdMode")
    private String tdMode; // 交易模式 cross：全仓 isolated：逐仓 cash：非保证金

    @JsonProperty("side")
    private String side; // buy 或 sell

    @JsonProperty("ordType")
    private String ordType; // 订单类型: market, limit, post_only, fok, ioc, optimal_limit_ioc (策略委托时，通常是触发后的订单类型)
                            // 对于策略委托本身的类型，使用 algoClOrdId, triggerPx 等字段

    @JsonProperty("sz")
    private String sz; // 委托数量

    @JsonProperty("px")
    private String px; // 委托价格 (对于 limit 类型)

    @JsonProperty("posSide")
    private String posSide; // 持仓方向 long short net(net仅适用于非保证金模式) - 开仓时需要

    @JsonProperty("clOrdId")
    private String clOrdId; // 客户自定义订单ID

    @JsonProperty("tag")
    private String tag; // 订单标签

    // --- 止盈止损参数 ---
    @JsonProperty("tpTriggerPx")
    private String tpTriggerPx; // 止盈触发价

    @JsonProperty("tpOrdPx")
    private String tpOrdPx; // 止盈委托价 (填 -1 则市价)

    @JsonProperty("slTriggerPx")
    private String slTriggerPx; // 止损触发价

    // @JsonProperty("ccy")
    private String ccy; // 止损触发价

    @JsonProperty("slOrdPx")
    private String slOrdPx; // 止损委托价 (填 -1 则市价)

    // --- 策略委托特有参数 (如果使用 /api/v5/trade/order-algo) ---
    // OKX 的 Algo Order API 设计有点绕，通常是在一个请求里同时设置开仓条件(如果需要触发价)和止盈止损
    // 如果是直接限价开仓并带止盈止损，上述字段就够了
    // 如果是更复杂的策略，如移动止损、计划委托，需要参考文档添加更多参数
    // algoClOrdId, triggerPx, algoId 等

     // --- 针对止盈止损的额外设置（可选） ---
    @JsonProperty("tpTriggerPxType")
    private String tpTriggerPxType; // 止盈触发价类型 last:最新价格 index:指数价格 mark:标记价格。默认last

    @JsonProperty("slTriggerPxType")
    private String slTriggerPxType; // 止损触发价类型，同上

    // --- 可以有多个止盈或止损目标 (如果接口支持) ---
    // private List<TpSlDetail> takeProfitDetails;
    // private List<TpSlDetail> stopLossDetails;

    // 可以在这里添加一个内部类来表示更复杂的止盈止损结构（如果需要）
    // @Data
    // public static class TpSlDetail {
    //     private String triggerPrice;
    //     private String orderPrice; // "-1" for market
    //     private String quantityPercentage; // e.g., "50" for 50%
    // }
}
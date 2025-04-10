package com.cp.aitg.okx.controller.dto;

import com.cp.aitg.okx.util.OkxSignatureUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 忽略值为 null 的字段
public class ManualOrderRequest {

    @NotBlank(message = "产品ID (instId) 不能为空")
    private String instId;
    // 产品ID，例如 "BTC-USDT-SWAP"

    @NotBlank(message = "交易模式 (tdMode) 不能为空")
    @Pattern(regexp = "cross|isolated|cash", message = "交易模式必须是 cross、isolated 或 cash")
    private String tdMode;
    // 交易模式：全仓 (cross)、逐仓 (isolated) 或 非保证金 (cash)

    @NotBlank(message = "订单方向 (side) 不能为空")
    @Pattern(regexp = "buy|sell", message = "订单方向必须是 buy 或 sell")
    private String side;
    // 订单方向

    @NotBlank(message = "持仓方向 (posSide) 不能为空")
    @Pattern(regexp = "long|short|net", message = "持仓方向必须是 long、short 或 net")
    private String posSide;
    // 持仓方向: long、short（某些模式下 net 亦可）

    @NotBlank(message = "订单类型 (ordType) 不能为空")
    @Pattern(regexp = "limit|market|conditional|oco|chase|trigger|move_order_stop|twap", message = "订单类型不合法")
    private String ordType;
    /* 订单类型说明：
       - limit: 限价单
       - market: 市价单
       - conditional: 单向止盈止损订单
       - oco: 互斥订单
       - chase: 跟踪订单
       - trigger: 触发订单
       - move_order_stop: 追踪止损单
       - twap: TWAP 订单
    */

    @NotBlank(message = "订单数量 (sz) 不能为空")
    private String sz;
//    @NotBlank(message = "订单数量 (sz) 不能为空")

    private String ccy="USDT";
    // 订单数量

    // 限价单价格，市价单可为空
    private String px;

    // 客户端自定义订单ID（可选）
    private String clOrdId= OkxSignatureUtil.generateClOrdId(null);

    // 订单标签（可选）
    private String tag;

    // ------------------- 止盈/止损相关参数 -------------------
    private String tpTriggerPx;
    // 止盈触发价

    @JsonProperty("tpOrdPx")
    private String tpOrdPx;
    // 止盈委托价（填 "-1" 表示市价）

    private String slTriggerPx;
    // 止损触发价

    @JsonProperty("slOrdPx")
    private String slOrdPx;
    // 止损委托价（填 "-1" 表示市价）

    // ------------------- 触发订单相关参数 -------------------
    @JsonProperty("triggerPx")
    private String triggerPx;
    // 触发价格，当达到此价格时触发订单

    @JsonProperty("orderPx")
    private String orderPx;
    // 触发后的委托价格（限价订单填具体价格；市价订单填 "-1"）

    @JsonProperty("triggerPxType")
    private String triggerPxType;
    // 触发价格类型：支持 "last"（默认）、"index"、"mark"

    // 附加的 SL/TP 订单（仅适用于部分触发订单场景，如同时挂止盈和止损）
    @JsonProperty("attachAlgoOrds")
    private List<AttachAlgoOrder> attachAlgoOrds;

    // ------------------- 追踪止损订单参数 -------------------
    private String callbackRatio;
    // 回调比例，如 "0.01" 表示 1%

    private String callbackSpread;
    // 回调价差

    private String activePx;
    // 激活价格，达到此价格后开始追踪

    private Boolean reduceOnly;
    // 是否仅限于减仓（true 或 false），默认 false

    // ------------------- TWAP 订单参数 -------------------
    private String szLimit;
    // TWAP 订单中每次下单的平均数量

    private String pxLimit;
    // TWAP 订单中限价的价格上限，不低于 0

    private String timeInterval;
    // TWAP 订单中分批下单的时间间隔（单位：秒）

    private String pxSpread;
    // TWAP 订单中价格常量偏移

    // ------------------- Chase 订单参数 -------------------
    private String chaseType;
    // 跟踪类型，允许 "distance" 或 "ratio"

    private String chaseVal;
    // 跟踪值，根据 chaseType 可为具体距离或比例

    private String maxChaseType;
    // 最大跟踪类型，与 maxChaseVal 配合使用，可选 "distance" 或 "ratio"

    private String maxChaseVal;
    // 最大跟踪值：若 chaseType 为 distance，则表示最大距离；若为 ratio，则表示最大比例

    // ------------------- 内部类：附加的 SL/TP 订单参数 -------------------
    @Data
    @Builder
    @NoArgsConstructor  // 保证存在无参构造，便于 Jackson 反序列化
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AttachAlgoOrder {

        @JsonProperty("attachAlgoClOrdId")
        private String attachAlgoClOrdId;
        // 客户自定义附加订单ID（长度 <=32）

        @JsonProperty("tpTriggerPx")
        private String tpTriggerPx;
        // 附加止盈触发价

        @JsonProperty("tpOrdPx")
        private String tpOrdPx;
        // 附加止盈委托价（填 "-1" 表示市价）

        @JsonProperty("tpTriggerPxType")
        private String tpTriggerPxType;
        // 附加止盈触发价类型，默认 "last"

        @JsonProperty("slTriggerPx")
        private String slTriggerPx;
        // 附加止损触发价

        @JsonProperty("slOrdPx")
        private String slOrdPx;
        // 附加止损委托价（填 "-1" 表示市价）

        @JsonProperty("slTriggerPxType")
        private String slTriggerPxType;
        // 附加止损触发价类型，默认 "last"
    }

    // 校验逻辑：如果订单类型为 limit，则价格 (px) 必须不为空
    public boolean isValidLimitOrder() {
        return !"limit".equalsIgnoreCase(ordType) || (px != null && !px.isBlank());
    }
}

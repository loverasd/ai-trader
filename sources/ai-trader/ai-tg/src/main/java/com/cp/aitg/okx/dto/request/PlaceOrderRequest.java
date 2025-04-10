package com.cp.aitg.okx.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 标准订单请求 DTO (用于 POST /api/v5/trade/order).
 * 代表一个不包含复杂策略（如直接止盈止损）的基础下单请求。
 */
@Data
@Builder // 使用 Builder 模式方便构建对象
@NoArgsConstructor // Jackson 反序列化需要无参构造函数
@AllArgsConstructor // Builder 模式需要全参构造函数
@JsonInclude(JsonInclude.Include.NON_NULL) // 序列化为 JSON 时忽略值为 null 的字段
public class PlaceOrderRequest {

    /**
     * 产品ID，例如 "BTC-USDT-SWAP", "BTC-USDT".
     * (必需)
     */
    @JsonProperty("instId")
    private String instId;

    /**
     * 交易模式。
     * Margin mode: "cross" (全仓), "isolated" (逐仓)
     * Non-Margin mode: "cash" (非保证金，例如币币交易)
     * (必需)
     */
    @JsonProperty("tdMode")
    private String tdMode;

    /**
     * 订单方向。
     * "buy": 买入
     * "sell": 卖出
     * (必需)
     */
    @JsonProperty("side")
    private String side;

    /**
     * 订单类型。
     * "market": 市价单
     * "limit": 限价单
     * "post_only": 只做 Maker 单
     * "fok": 全部成交或立即取消
     * "ioc": 立即成交并取消剩余
     * "optimal_limit_ioc": 市价委托立即成交并取消剩余（仅适用交割/永续）
     * (必需)
     */
    @JsonProperty("ordType")
    private String ordType;

    /**
     * 委托数量。
     * 对于币币/币币杠杆，期权，交割/永续（开平仓模式）：指交易货币数量。
     * 对于交割/永续（买卖模式）：
     *   - 市价买单：指计价货币数量。
     *   - 其他：指合约张数。
     * (必需)
     */
    @JsonProperty("sz")
    private String sz;

    /**
     * 委托价格。
     * 仅适用于限价单、只做 Maker 单。
     * (条件必需: ordType 为 limit 或 post_only 时必需)
     */
    @JsonProperty("px")
    private String px;

    /**
     * 持仓方向。
     * 在开仓/平仓模式下必填，且仅可为 "long" 或 "short"。
     * (条件必需: 保证金模式下的开仓/平仓模式必需)
     */
    @JsonProperty("posSide")
    private String posSide;

    /**
     * 客户自定义订单ID。
     * 字母（区分大小写）与数字的组合，不支持空格等符号，长度限制 1~32 位。
     * 用于保证幂等性，防止重复下单。
     * (可选, 但强烈建议使用)
     */
    @JsonProperty("clOrdId")
    private String clOrdId;

    /**
     * 订单标签。
     * 字母（区分大小写）与数字的组合，不支持空格等符号，长度限制 1~16 位。
     * (可选)
     */
    @JsonProperty("tag")
    private String tag;

    /**
     * 订单数量单位设置（仅适用于 SPOT 市价买单）。
     * "base_ccy": 交易货币
     * "quote_ccy": 计价货币
     * 市价买单默认 "quote_ccy" (按金额买)，市价卖单默认 "base_ccy" (按数量卖)。
     * (可选)
     */
    @JsonProperty("tgtCcy")
    private String tgtCcy;

    /**
     * 是否只减仓。
     * true 或 false，默认为 false。
     * 仅适用于币币杠杆、交割/永续。
     * (可选)
     */
    @JsonProperty("reduceOnly")
    private Boolean reduceOnly;

    /**
     * 市价单委托数量sz的单位，仅适用于交割/永续的市价单。
     * "base_ccy": 交易货币
     * "quote_ccy": 计价货币
     * 默认为 "base_ccy"。
     * (可选)
     */
    @JsonProperty("szCcy")
    private String szCcy;

    /**
     * 是否禁止币币被动委托赠金，仅适用于 SPOT。
     * true 或 false。默认为 false。
     * (可选)
     */
    @JsonProperty("banAmend")
    private Boolean banAmend;

    // --- 注意：标准订单接口不直接支持在此处设置止盈止损 ---
    // 止盈止损需要通过策略委托接口 /api/v5/trade/order-algo 实现
}
package com.cp.aitg.okx.dto.request.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 基础订单请求类（所有订单类型共有属性）
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRequest {
    /**
     * instId：交易对 ID，例如 "BTC-USDT"
     */
    @NotNull(message = "instId（交易对）不能为空")
    private String instId;

    /**
     * tdMode：交易模式
     * <p>
     * 可选值：
     * - cross：全仓模式
     * - isolated：逐仓模式
     * - cash：非杠杆模式
     * - spot_isolated：仅适用于现货领投模式
     * </p>
     */
    @NotNull(message = "tdMode（交易模式）不能为空")
    private String tdMode;

    /**
     * ccy：资金币种
     * 适用于所有 isolated/MARGIN 订单和 cross/MARGIN 下的现货及期货模式订单
     */
    private String ccy;

    /**
     * side：订单方向
     * <p>
     * 必填值："buy"（买入）或 "sell"（卖出）
     * </p>
     */
    @NotNull(message = "side（买卖方向）不能为空")
    private String side;

    /**
     * posSide：持仓方向
     * <p>
     * 在长/短仓模式下必填，有效值为 "long" 或 "short"
     * </p>
     */
    private String posSide;

    /**
     * ordType：订单类型
     * <p>
     * 有效值：
     * - conditional：条件订单（触发后下单）
     * - oco：一撤二订单
     * - chase：追单（适用于期货和永续合约）
     * - trigger：触发订单
     * - move_order_stop：追踪止损订单（Trailing Stop Order）
     * - twap：TWAP 订单（时间加权平均价格）
     * </p>
     */
    @NotNull(message = "ordType（订单类型）不能为空")
    private String ordType;

    /**
     * sz：订单数量（字符串类型）
     * <p>
     * 数量相关参数：sz 与 closeFraction 二选一传递
     * </p>
     */
    private String sz;

    /**
     * tag：订单标签
     * <p>
     * 由大小写字母和数字组成，最多 16 个字符
     * </p>
     */
    private String tag;

    /**
     * tgtCcy：订单数量单位设置
     * <p>
     * 如：base_ccy 表示以基础币种计算，quote_ccy 表示以计价币种计算
     * 仅适用于部分现货订单场景
     * </p>
     */
    private String tgtCcy;

    /**
     * algoClOrdId：客户自定义算法订单 ID
     * <p>
     * 由大小写字母和数字组成，最多 32 个字符
     * </p>
     */
    private String algoClOrdId;

    /**
     * closeFraction：平仓比例
     * <p>
     * 当订单触发时平仓的比例，目前系统仅支持全平（值为 "1"），
     * 仅适用于 FUTURES 或 SWAP 市场。如果 posSide 为 "net"，则 reduceOnly 必须为 true。
     * </p>
     */
    private String closeFraction;


    /**
     * 条件订单（如止盈/止损订单）的请求定义
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConditionalOrderRequest extends OrderRequest {
        /**
         * tpTriggerPx：止盈触发价
         */
        private String tpTriggerPx;

        /**
         * tpTriggerPxType：止盈触发价类型
         * <p>
         * 可选值：
         * - last：最近成交价（默认）
         * - index：指数价格
         * - mark：标记价格
         * </p>
         */
        private String tpTriggerPxType;

        /**
         * tpOrdPx：止盈订单价格
         * <p>
         * 若传入值为 "-1"，则以市价执行
         * </p>
         */
        private String tpOrdPx;

        /**
         * tpOrdKind：止盈订单类型，默认 "condition"
         */
        private String tpOrdKind;

        /**
         * slTriggerPx：止损触发价
         */
        private String slTriggerPx;

        /**
         * slTriggerPxType：止损触发价类型
         * <p>
         * 可选值同 tpTriggerPxType：last、index、mark，默认 "last"
         * </p>
         */
        private String slTriggerPxType;

        /**
         * slOrdPx：止损订单价格
         * <p>
         * 若传入值为 "-1"，则以市价执行
         * </p>
         */
        private String slOrdPx;

        /**
         * cxlOnClosePos：是否与持仓关联
         * <p>
         * true 表示关联（平仓时自动撤销订单），false 表示不关联，默认 false
         * </p>
         */
        private Boolean cxlOnClosePos;

        /**
         * reduceOnly：是否仅用于减少持仓
         */
        private Boolean reduceOnly;
    }

    /**
     * 附加的 SL/TP 订单信息，用于触发订单的附加参数 attachAlgoOrds
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AttachAlgoOrder {
        /**
         * attachAlgoClOrdId：客户自定义附加订单 ID
         */
        private String attachAlgoClOrdId;

        /**
         * slTriggerPx：附加订单的止损触发价
         */
        private String slTriggerPx;

        /**
         * slOrdPx：附加订单的止损订单价格
         */
        private String slOrdPx;

        /**
         * tpTriggerPx：附加订单的止盈触发价
         */
        private String tpTriggerPx;

        /**
         * tpOrdPx：附加订单的止盈订单价格
         */
        private String tpOrdPx;
    }

    /**
     * 触发订单（Trigger Order）的请求定义
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TriggerOrderRequest extends OrderRequest {
        /**
         * triggerPx：触发价格
         * <p>
         * 当市场价格达到该值时，将触发下单操作
         * </p>
         */
        @NotNull(message = "triggerPx（触发价）不能为空，触发订单必须传此参数")
        private String triggerPx;

        /**
         * orderPx：下单价格
         * <p>
         * 若传入值为 "-1"，则下单以市价执行
         * </p>
         */
        @NotNull(message = "orderPx（订单价格）不能为空，若传 -1 则市价执行")
        private String orderPx;

        /**
         * triggerPxType：触发价类型
         * <p>
         * 可选值：last（最近成交价）、index（指数价）、mark（标记价），默认 "last"
         * </p>
         */
        private String triggerPxType;

        /**
         * quickMgnType：快速杠杆模式（已弃用，可选）
         */
        private String quickMgnType;

        /**
         * attachAlgoOrds：附加的止盈/止损订单信息列表（仅部分场景适用）
         */
        private List<AttachAlgoOrder> attachAlgoOrds;
    }

    /**
     * 追踪止损订单（Trailing Stop Order，也称 move_order_stop）的请求定义
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TrailingStopOrderRequest extends OrderRequest {
        /**
         * callbackRatio：回调比例（必填）
         * <p>
         * 例如 "0.05" 表示 5%，用于计算触发价格的浮动幅度
         * 若同时传入 callbackSpread，则二选一使用
         * </p>
         */
        @NotNull(message = "callbackRatio（回调比例）不能为空")
        private String callbackRatio;

        /**
         * callbackSpread：回调价幅（可选）
         */
        private String callbackSpread;

        /**
         * activePx：激活价格
         * <p>
         * 如果设置此值，则系统在市场价格达到该值后开始跟踪计算触发价
         * 若不设置，则订单立即进入跟踪状态
         * </p>
         */
        private String activePx;

        /**
         * quickMgnType：快速杠杆模式（已弃用，可选）
         */
        private String quickMgnType;

        /**
         * reduceOnly：是否仅用于减少持仓
         */
        private Boolean reduceOnly;
    }

    /**
     * TWAP 订单（时间加权平均价格）的请求定义
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TWAPOrderRequest extends OrderRequest {
        /**
         * szLimit：平均下单数量
         * <p>
         * 表示将订单拆分为若干小单时每单的平均数量
         * </p>
         */
        @NotNull(message = "szLimit（平均下单数量）不能为空")
        private String szLimit;

        /**
         * pxLimit：限价
         * <p>
         * 下单价格上限，不能低于 0
         * </p>
         */
        @NotNull(message = "pxLimit（限价）不能为空")
        private String pxLimit;

        /**
         * timeInterval：下单时间间隔（单位秒）
         */
        @NotNull(message = "timeInterval（时间间隔）不能为空")
        private String timeInterval;

        /**
         * pxVar：价格浮动比例（百分比），与 pxSpread 二选一
         * <p>
         * 取值范围如 0.0001 ~ 0.01，0.01 表示 1%
         * </p>
         */
        private String pxVar;

        /**
         * pxSpread：价格浮动常数，与 pxVar 二选一
         * <p>
         * 价格浮动的绝对值，不低于 0
         * </p>
         */
        private String pxSpread;
    }

    /**
     * 追单订单（Chase Order）的请求定义
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChaseOrderRequest extends OrderRequest {
        /**
         * chaseType：追单类型
         * <p>
         * 可选值：
         * - distance：距离最优买卖价的绝对值距离
         * - ratio：按比例计算的浮动值，例如 0.1 表示 10%
         * </p>
         */
        private String chaseType;

        /**
         * chaseVal：追单值，根据 chaseType 的值决定单位
         */
        private String chaseVal;

        /**
         * maxChaseType：最大追单类型，当需要限制追单距离时使用
         * <p>
         * 与 maxChaseVal 搭配使用，二者必须同时传递
         * </p>
         */
        private String maxChaseType;

        /**
         * maxChaseVal：最大追单值，根据 maxChaseType 的值决定单位
         * <p>
         * 与 maxChaseType 搭配使用，二者必须同时传递
         * </p>
         */
        private String maxChaseVal;

        /**
         * reduceOnly：是否仅用于减少持仓
         */
        private Boolean reduceOnly;
    }
}

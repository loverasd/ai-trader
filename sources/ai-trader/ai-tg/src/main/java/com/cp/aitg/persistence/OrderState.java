package com.cp.aitg.persistence;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.NoArgsConstructor; // Jackson 需要无参构造

import java.io.Serializable; // 推荐实现 Serializable，特别是用于 Redis 缓存
import java.time.LocalDateTime;

/**
 * 代表一个交易订单的状态和关键信息.
 * 用于在系统中跟踪订单从创建到完成的整个生命周期。
 */
@Data // Lombok: 生成 getter, setter, equals, hashCode, toString
@NoArgsConstructor // Jackson 反序列化需要无参构造函数
@JsonInclude(JsonInclude.Include.NON_NULL) // JSON 序列化时忽略 null 字段
@JsonIgnoreProperties(ignoreUnknown = true) // JSON 反序列化时忽略未知字段，增加兼容性
public class OrderState implements Serializable { // 实现 Serializable 接口

    private static final long serialVersionUID = 1L; // Serializable 版本号

    /**
     * 策略委托 ID (OKX 返回). 这是主要的唯一标识符。
     */
    private String algoId;

    /**
     * 客户自定义订单 ID (下单时传入). 用于幂等性和用户侧跟踪。
     */
    private String clOrdId;
    /**
     * 客户自定义订单 ID (下单时传入). 用于幂等性和用户侧跟踪。
     */
    private String ordId;

    /**
     * 交易产品 ID (例如 "BTC-USDT-SWAP").
     */
    private String instId;

    /**
     * 当前订单状态.
     */
    private Status status;

    /**
     * 订单来源 (例如 "MANUAL", "AUTO_STRATEGY_MA_CROSS").
     */
    private String source;

    /**
     * 订单方向 ("buy" 或 "sell").
     */
    private String side;

    /**
     * 持仓方向 ("long", "short", 或 "net").
     */
    private String posSide;

    /**
     * 订单数量 (单位：张 或 币，根据 instId 确定).
     */
    private String size;

    /**
     * 计划入场价格 (限价单的价格).
     */
    private String entryPrice;

    /**
     * 实际入场平均价格 (订单成交后填充).
     */
    private String actualEntryPrice;

    /**
     * 实际出场平均价格 (平仓成交后填充).
     */
    private String exitPrice;

    /**
     * 止损触发价格.
     */
    private String slTriggerPx;

    /**
     * 止损委托价格 ("-1" 表示市价止损).
     */
    private String slOrdPx;

    /**
     * 止盈触发价格.
     */
    private String tpTriggerPx;

    /**
     * 止盈委托价格 ("-1" 表示市价止盈).
     */
    private String tpOrdPx;

    /**
     * 订单记录创建时间.
     * 使用 Jackson 注解确保与 Spring Boot 默认的 ObjectMapper 兼容 ISO 8601 格式。
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    /**
     * 订单实际开仓时间 (状态变为 OPEN 的时间).
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime openedAt;

    /**
     * 订单最终关闭时间 (状态变为 CLOSED 或 FAILED 的时间).
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime closedAt;

    /**
     * 记录最后更新时间.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;

    /**
     * 平仓原因 (例如 "TP" (Take Profit), "SL" (Stop Loss), "MANUAL_CLOSE", "RISK_CLOSE", "CANCELED", "FAILED").
     */
    private String closeReason;

    /**
     * 盈亏金额 (可选，可以动态计算或在平仓时计算并存储).
     * 使用 Double 类型，注意精度问题，或者使用 BigDecimal。
     */
    private Double profitLoss;

    /**
     * 附加信息 (可选，用于存储策略特定参数或其他备注信息).
     * 可以是 JSON 字符串或其他格式。
     */
    private String remarks;

    /**
     * 订单状态枚举.
     */
    public enum Status {
        /**
         * 订单已提交到交易所，等待触发或成交 (初始状态).
         */
        PLACED,

        /**
         * 订单（通常是策略委托）已被交易所接受并激活，正在监控触发条件或等待成交.
         * 这个状态可以用来区分刚提交的 PLACED 和 交易所确认已在监控的状态。
         */
        MONITORING,

        /**
         * 订单已部分或全部成交，持仓已建立.
         */
        OPEN,

        /**
         * 平仓指令已发出，等待平仓完成. (可选状态)
         */
        CLOSING,

        /**
         * 订单已完全关闭（平仓完成，或被取消且未开仓）. 这是一个最终状态。
         */
        CLOSED,

        /**
         * 订单执行失败（例如，交易所拒绝，保证金不足，触发后下单失败等）. 这是一个最终状态。
         */
        FAILED,

        /**
         * 状态未知或无法确定 (例如，查询失败或遇到预期外的状态).
         */
        UNKNOWN
    }

    // --- 可选的辅助方法 ---

    /**
     * 判断订单是否处于活动状态 (未最终完成).
     * @return 如果状态是 PLACED, MONITORING, OPEN 或 CLOSING (可选)，则返回 true。
     */
    public boolean isActive() {
        return this.status == Status.PLACED ||
               this.status == Status.MONITORING ||
               this.status == Status.OPEN ||
               this.status == Status.CLOSING; // 根据需要调整
    }

    /**
     * 判断订单是否已进入最终状态.
     * @return 如果状态是 CLOSED 或 FAILED，则返回 true。
     */
    public boolean isTerminal() {
        return this.status == Status.CLOSED || this.status == Status.FAILED;
    }
}
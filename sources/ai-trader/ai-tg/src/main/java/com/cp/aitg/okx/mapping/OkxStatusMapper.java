package com.cp.aitg.okx.mapping;

import com.cp.aitg.okx.dto.response.AlgoOrderDetails;
import com.cp.aitg.persistence.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component // 如果无状态，也可以使用静态工具类
@Slf4j
public class OkxStatusMapper {

    /**
     * 将 OKX 算法订单状态字符串映射为内部 OrderState.Status 枚举。
     *
     * @param okxAlgoStatus 来自 OKX 算法订单详情的状态字符串。
     * @return 对应的内部 OrderState.Status。
     */
    public OrderState.Status mapAlgoStatusToInternal(String okxAlgoStatus) {
        if (okxAlgoStatus == null) return OrderState.Status.UNKNOWN;
        switch (okxAlgoStatus) {
            case "live":
            case "paused":
                // 表示策略本身处于激活状态，但不保证仓位已开启。
                // 监控服务需要结合仓位信息或订单成交详情。
                // 暂时映射为 MONITORING，假定初始状态为 PLACED。
                return OrderState.Status.MONITORING;
            case "effective":   // 策略条件满足，订单可能已完全成交或完成。
            case "partially_effective": // 部分成交/完成，需要谨慎处理。暂时映射为 OPEN？或单独设置 PARTIAL 状态？这里映射为 OPEN，假定监控服务后续处理完全关闭的情况。
                // TODO: 重新评估 partially_effective 的处理 —— 可能表示仓位已开启但未达到完整规模。
                // 或者如果 effective 表示完全关闭，则直接映射为 CLOSED？需要测试。为简化起见，此处假定 effective 表示关闭完成。
                log.debug("将 OKX 状态 '{}' 映射为 CLOSED（effective 表示完成）", okxAlgoStatus);
                return OrderState.Status.CLOSED;
            case "canceled":
            case "order_failed": // 触发后后续订单下单失败。
                return OrderState.Status.CLOSED; // 作为最终状态处理

            // 为其他相关的 OKX 状态添加映射
            default:
                log.warn("未知的 OKX 策略委托状态: {}", okxAlgoStatus);
                return OrderState.Status.UNKNOWN;
        }
    }

    /**
     * 将 OKX 标准订单状态字符串映射为内部 OrderState.Status 枚举。
     * （你需要在 OkxApiService 中实现 getStandardOrderDetails）
     *
     * @param okxStandardStatus 来自 OKX 标准订单详情的状态字符串（例如 "filled", "partially_filled", "canceled"）。
     * @return 对应的内部 OrderState.Status。
     */
    public OrderState.Status mapStandardStatusToInternal(String okxStandardStatus) {
        if (okxStandardStatus == null) return OrderState.Status.UNKNOWN;
        // 基于 OKX 文档中 GET /api/v5/trade/order 的 'state' 字段
        switch (okxStandardStatus) {
            case "live": // 订单处于活动状态但未成交
                return OrderState.Status.PLACED; // 或者下单后如果希望监控，则映射为 MONITORING
            case "partially_filled":
                return OrderState.Status.OPEN; // 部分成交，仓位已部分开启
            case "filled":
                // 如果这是初始入场订单，则映射为 OPEN。
                // 如果这是平仓订单（TP/SL/手动），则表示平仓完成 -> CLOSED。
                // 监控服务需要上下文来区分，此处暂时映射为 OPEN，假设监控服务会检查是否为平仓订单。
                // 或者查询本身可能仅针对入场订单。
                // 为简化起见，filled 表示入场已完成。
                log.debug("将 OKX 标准订单状态 '{}' 映射为 OPEN（filled 表示入场完成）", okxStandardStatus);
                return OrderState.Status.OPEN;
            case "canceled":
            case "mmp_canceled": // 市场做市商保护取消订单
                return OrderState.Status.CLOSED; // 如果未开启，作为最终状态处理

            // 如有需要，可添加其它状态（例如 'placing'）
            default:
                log.warn("未知的 OKX 标准订单状态: {}", okxStandardStatus);
                return OrderState.Status.UNKNOWN;
        }
    }

    /**
     * 根据 AlgoOrderDetails 确定平仓原因。（简化版）
     *
     * @param details AlgoOrderDetails 对象。
     * @return 表示平仓原因的字符串。
     */
    public String determineAlgoCloseReason(AlgoOrderDetails details) {
        if (details == null || details.getState() == null) return "Unknown";
        switch (details.getState()) {
            case "effective":
                // TODO: 需要细化，需检查由哪个部分触发（TP/SL）。
                // 可能需要查询与 algoId 相关的订单历史记录。
                return "Triggered (TP/SL - Needs Detail)";
            case "canceled":
                return "Canceled";
            case "order_failed":
                return "Order Failed";
            default:
                // 如果因仓位检查或其他逻辑而关闭，调用者应设置相应原因。
                return "Closed (" + details.getState() + ")";
        }
    }

    /**
     * 根据标准订单详情确定平仓原因。（简化版）
     *
     * @param details 标准订单的 OrderDetails 对象（需要你定义这个 DTO）。
     * @return 表示平仓原因的字符串。
     */
    public String determineStandardCloseReason(AlgoOrderDetails details) {
        if (details == null || details.getState() == null) return "Unknown";
        switch (details.getState()) {
            case "canceled":
            case "mmp_canceled":
                return "Canceled";
            case "filled":
                // 如果监控服务确认这是 TP 或 SL 订单，则返回 "TP" 或 "SL"。
                // 否则，很可能是手动或风险控制平仓。
                return "Filled (Needs Context - TP/SL/Manual?)";
            default:
                return "Closed (" + details.getState() + ")";
        }
    }
}

package com.cp.aitg.config;

import com.cp.aitg.persistence.OrderState; // 需要引用 Status 枚举

public class RedisKeys {

    private static final String BASE_PREFIX = "aitg"; // 应用统一前缀
    private static final String ORDER_STATE_PREFIX = BASE_PREFIX + ":orderstate";

    /**
     * 获取存储 OrderState JSON 的 Key
     * e.g., aitg:orderstate:id:12345abc
     * @param algoId 策略委托 ID
     * @return Redis Key
     */
    public static String getOrderStateKey(String algoId) {
        return ORDER_STATE_PREFIX + ":id:" + algoId;
    }

    /**
     * 获取存储特定状态下所有 algoId 的 Set 的 Key
     * e.g., aitg:orderstate:status:OPEN
     * @param status 订单状态
     * @return Redis Key for the status index set
     */
    public static String getStatusIndexKey(OrderState.Status status) {
        if (status == null) {
            // 处理未知或 null 状态的情况，可以记录日志或使用默认 key
             return ORDER_STATE_PREFIX + ":status:UNKNOWN";
        }
        return ORDER_STATE_PREFIX + ":status:" + status.name(); // 使用枚举名称
    }

    // 可以添加其他 Key 的定义，例如按 clOrdId 索引等
    // public static String getClOrdIdIndexKey(String clOrdId) {
    //     return ORDER_STATE_PREFIX + ":clOrdId:" + clOrdId;
    // }
}
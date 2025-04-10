package com.cp.aitg.persistence;

import java.util.List;
import java.util.Optional; // 使用 Optional 明确表示可能不存在

/**
 * 订单状态持久化服务接口.
 * 定义了保存、更新、查询和删除交易订单状态的操作。
 */
public interface StatePersistenceService {

    /**
     * 保存一个新的订单状态记录.
     * 如果记录已存在（基于唯一标识符，如 algoId），行为取决于具体实现（可能更新或抛异常）。
     * 推荐：如果需要严格区分新增和更新，可以提供单独的 create 和 update 方法。
     *
     * @param state 要保存的订单状态对象，不能为 null，且应包含有效的标识符。
     * @throws IllegalArgumentException 如果 state 为 null 或缺少必要标识符。
     * @throws RuntimeException 如果持久化过程中发生错误。
     */
    void saveOrderState(OrderState state);

    /**
     * 更新一个已存在的订单状态记录.
     * 如果记录不存在，此方法可能什么都不做，或者根据实现抛出异常。
     *
     * @param state 要更新的订单状态对象，不能为 null，且应包含有效的标识符。
     * @throws IllegalArgumentException 如果 state 为 null 或缺少必要标识符。
     * @throws RuntimeException 如果持久化过程中发生错误或记录不存在（取决于实现）。
     */
    void updateOrderState(OrderState state);

    /**
     * 根据策略委托 ID (algoId) 查找订单状态记录.
     *
     * @param algoId 策略委托 ID，不能为空。
     * @return 包含找到的 OrderState 的 Optional，如果未找到则为空 Optional。
     * @throws IllegalArgumentException 如果 algoId 为 null 或空。
     */
    OrderState findOrderStateByAlgoId(String algoId);

    /**
     * 根据客户自定义订单 ID (clOrdId) 查找订单状态记录.
     * 注意：一个 clOrdId 理论上应该只对应一个订单。
     *
     * @param clOrdId 客户自定义订单 ID，不能为空。
     * @return 包含找到的 OrderState 的 Optional，如果未找到则为空 Optional。
     * @throws IllegalArgumentException 如果 clOrdId 为 null 或空。
     */
    Optional<OrderState> findOrderStateByClOrdId(String clOrdId);


    /**
     * 查找所有处于活动状态的订单记录.
     * "活动状态" 通常指订单尚未最终完成（如 PLACED, MONITORING, OPEN）。
     * 具体哪些状态算作活动状态由实现类决定。
     *
     * @return 包含所有活动 OrderState 的列表，如果没有活动订单则返回空列表。
     */
    List<OrderState> findActiveOrders();

    /**
     * 根据状态查找订单记录列表.
     *
     * @param status 要查询的状态，不能为 null。
     * @return 包含指定状态的所有 OrderState 的列表，如果没有则返回空列表。
      * @throws IllegalArgumentException 如果 status 为 null。
     */
    List<OrderState> findOrdersByStatus(OrderState.Status status);


    /**
     * 根据策略委托 ID (algoId) 删除订单状态记录.
     * 通常在订单确认完全结束后（如已通知、已归档）调用此方法清理状态存储。
     *
     * @param algoId 要删除记录的策略委托 ID，不能为空。
     * @throws IllegalArgumentException 如果 algoId 为 null 或空。
     * @throws RuntimeException 如果删除过程中发生错误。
     */
    void removeOrderState(String algoId);
}
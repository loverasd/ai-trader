package com.cp.aitg.persistence;

import com.cp.aitg.config.RedisKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStatePersistenceService implements StatePersistenceService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper; // 用于 JSON 序列化/反序列化
    private final long defaultTtlMinutes = 60 * 24 * 7; // 默认 TTL: 7 天 (防止无限增长)

    @Override
    public void saveOrderState(OrderState state) {
        if (state == null || state.getAlgoId() == null) {
            log.warn("尝试保存空的或缺少 algoId 的 OrderState");
            return;
        }
        String algoId = state.getAlgoId();
        String orderKey = RedisKeys.getOrderStateKey(algoId);
        String statusKey = RedisKeys.getStatusIndexKey(state.getStatus());

        try {
            String stateJson = objectMapper.writeValueAsString(state);

            // 使用 Redis 事务 (MULTI/EXEC) 保证原子性
            List<Object> txResults = redisTemplate.execute(new SessionCallback<List<Object>>() {
                @Override
                public List<Object> execute(RedisOperations operations) throws DataAccessException {
                    operations.multi(); // 开始事务
                    operations.opsForValue().set(orderKey, stateJson, defaultTtlMinutes, TimeUnit.MINUTES); // 保存订单数据并设置 TTL
                    operations.opsForSet().add(statusKey, algoId); // 添加到状态索引 Set
                    // 可以为状态索引 Set 也设置 TTL，但通常不必要，除非状态非常多且长期不清理
                    // operations.expire(statusKey, defaultTtlMinutes + 60, TimeUnit.MINUTES); // 比主数据长一点
                    return operations.exec(); // 执行事务
                }
            });

            if (txResults == null || txResults.contains(null)) {
                // 事务中至少有一个命令失败 (可能是 WATCH 导致的乐观锁失败，这里没用 WATCH)
                // 或者 Redis 返回了意外的 null
                 log.error("保存 OrderState 到 Redis 事务失败: AlgoID={}, Results={}", algoId, txResults);
                 // 可以考虑回滚或重试，但对于简单 set/sadd 失败概率低
                 throw new RuntimeException("保存订单状态到 Redis 事务失败: " + algoId);
            }
            log.info("OrderState 已保存到 Redis: AlgoID={}", algoId);

        } catch (JsonProcessingException e) {
            log.error("序列化 OrderState 失败: AlgoID={}", algoId, e);
            throw new RuntimeException("序列化订单状态失败", e);
        } catch (Exception e) {
             log.error("保存 OrderState 到 Redis 时发生异常: AlgoID={}", algoId, e);
             throw new RuntimeException("保存订单状态到 Redis 失败", e);
        }
    }

    @Override
    public void updateOrderState(OrderState newState) {
         if (newState == null || newState.getAlgoId() == null) {
            log.warn("尝试更新空的或缺少 algoId 的 OrderState");
            return;
        }
        String algoId = newState.getAlgoId();
        String orderKey = RedisKeys.getOrderStateKey(algoId);

        try {
            // 1. 获取旧状态以确定旧的 status key
            OrderState oldState = findOrderStateByAlgoId(algoId);
            if (oldState == null) {
                 log.warn("尝试更新不存在的 OrderState: AlgoID={}. 将执行保存操作。", algoId);
                 saveOrderState(newState); // 如果不存在，则执行保存
                 return;
            }

            String oldStatusKey = RedisKeys.getStatusIndexKey(oldState.getStatus());
            String newStatusKey = RedisKeys.getStatusIndexKey(newState.getStatus());
            String newStateJson = objectMapper.writeValueAsString(newState);

            // 2. 使用事务更新
            List<Object> txResults = redisTemplate.execute(new SessionCallback<List<Object>>() {
                 @Override
                public List<Object> execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.opsForValue().set(orderKey, newStateJson, defaultTtlMinutes, TimeUnit.MINUTES); // 更新数据并重置 TTL
                    // 如果状态改变了，更新索引
                    if (oldState.getStatus() != newState.getStatus()) {
                        operations.opsForSet().remove(oldStatusKey, algoId);
                        operations.opsForSet().add(newStatusKey, algoId);
                    }
                    return operations.exec();
                }
            });

             if (txResults == null || txResults.contains(null)) {
                 log.error("更新 OrderState 到 Redis 事务失败: AlgoID={}, Results={}", algoId, txResults);
                 throw new RuntimeException("更新订单状态到 Redis 事务失败: " + algoId);
            }
            log.info("OrderState 已更新: AlgoID={}, Status: {} -> {}", algoId, oldState.getStatus(), newState.getStatus());

        } catch (JsonProcessingException e) {
            log.error("序列化更新后的 OrderState 失败: AlgoID={}", algoId, e);
             throw new RuntimeException("序列化更新订单状态失败", e);
        } catch (Exception e) {
             log.error("更新 OrderState 到 Redis 时发生异常: AlgoID={}", algoId, e);
             throw new RuntimeException("更新订单状态到 Redis 失败", e);
        }
    }

    @Override
    public OrderState findOrderStateByAlgoId(String algoId) {
        if (algoId == null) return null;
        String orderKey = RedisKeys.getOrderStateKey(algoId);
        try {
            String stateJson = redisTemplate.opsForValue().get(orderKey);
            if (stateJson == null) {
                return null;
            }
            return objectMapper.readValue(stateJson, OrderState.class);
        } catch (JsonProcessingException e) {
            log.error("反序列化 OrderState 失败: AlgoID={}", algoId, e);
            // 可以选择删除损坏的数据 redisTemplate.delete(orderKey);
            return null;
        } catch (Exception e) {
             log.error("从 Redis 获取 OrderState 时发生异常: AlgoID={}", algoId, e);
             return null;
        }
    }

    @Override
    public Optional<OrderState> findOrderStateByClOrdId(String clOrdId) {
        return Optional.empty();
    }

    @Override
    public List<OrderState> findActiveOrders() {
        // 定义哪些状态是活动的
        Set<OrderState.Status> activeStatuses = EnumSet.of(
                OrderState.Status.PLACED,
                OrderState.Status.MONITORING,
                OrderState.Status.OPEN
                // OrderState.Status.CLOSING // 根据需要决定是否包含 CLOSING
        );

        Set<String> activeAlgoIds = new HashSet<>();
        try {
             // 获取所有活动状态索引 Set 的 Key
            List<String> activeStatusKeys = activeStatuses.stream()
                    .map(RedisKeys::getStatusIndexKey)
                    .collect(Collectors.toList());

            if (activeStatusKeys.isEmpty()) {
                return Collections.emptyList();
            }

            // 使用 SUNION 获取所有活动状态下的 algoId
            // 注意: SUNION 对于大量 Key 可能有性能影响，如果 Set 非常大，考虑分批 SMEMBERS
            Set<String> unionResult = redisTemplate.opsForSet().union(activeStatusKeys.get(0), activeStatusKeys.subList(1, activeStatusKeys.size()));

            if (!CollectionUtils.isEmpty(unionResult)) {
                activeAlgoIds.addAll(unionResult);
            }

            /* 或者使用循环 SMEMBERS (如果 SUNION 效率低)
            for (OrderState.Status status : activeStatuses) {
                String statusKey = RedisKeys.getStatusIndexKey(status);
                Set<String> idsInSet = redisTemplate.opsForSet().members(statusKey);
                if (idsInSet != null) {
                    activeAlgoIds.addAll(idsInSet);
                }
            }
            */

            if (activeAlgoIds.isEmpty()) {
                return Collections.emptyList();
            }

            // 使用 MGET 批量获取订单数据
            List<String> orderKeys = activeAlgoIds.stream()
                    .map(RedisKeys::getOrderStateKey)
                    .collect(Collectors.toList());

            List<String> stateJsonList = redisTemplate.opsForValue().multiGet(orderKeys);

            // 反序列化并过滤掉 null (可能由于 TTL 过期或数据损坏)
            return stateJsonList.stream()
                    .filter(Objects::nonNull)
                    .map(json -> {
                        try {
                            return objectMapper.readValue(json, OrderState.class);
                        } catch (JsonProcessingException e) {
                            log.error("反序列化活动订单失败: JSON={}", json, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
             log.error("查找活动订单时 Redis 操作异常", e);
             return Collections.emptyList(); // 返回空列表而不是抛异常，让监控任务继续
        }
    }

    @Override
    public List<OrderState> findOrdersByStatus(OrderState.Status status) {
        return List.of();
    }

    @Override
    public void removeOrderState(String algoId) {
        if (algoId == null) return;

        OrderState stateToRemove = findOrderStateByAlgoId(algoId);
        if (stateToRemove == null) {
            log.warn("尝试移除不存在的 OrderState: AlgoID={}", algoId);
            return; // 不存在，无需操作
        }

        String orderKey = RedisKeys.getOrderStateKey(algoId);
        String statusKey = RedisKeys.getStatusIndexKey(stateToRemove.getStatus());

        try {
            // 使用事务确保原子性
            List<Object> txResults = redisTemplate.execute(new SessionCallback<List<Object>>() {
                 @Override
                public List<Object> execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.delete(orderKey); // 删除主数据
                    operations.opsForSet().remove(statusKey, algoId); // 从状态索引移除
                    return operations.exec();
                }
            });

            if (txResults == null || txResults.contains(null) || txResults.stream().anyMatch(r -> r instanceof Long && (Long)r == 0)) {
                // 检查删除结果，如果删除返回 0 表示 key 不存在或删除失败
                log.warn("移除 OrderState 的 Redis 事务可能未完全成功: AlgoID={}, Results={}", algoId, txResults);
                 // 不抛异常，因为目标是移除，部分成功也可接受
            }
            log.info("OrderState 已从 Redis 移除: AlgoID={}", algoId);

        } catch (Exception e) {
             log.error("从 Redis 移除 OrderState 时发生异常: AlgoID={}", algoId, e);
             // 考虑是否需要重试或记录失败
        }
    }
}
package com.cp.aitg.okx.service;

import com.cp.aitg.notification.NotificationService; // 使用该接口
import com.cp.aitg.okx.common.OkxApiConstants;
import com.cp.aitg.okx.dto.OkxResponse;
import com.cp.aitg.okx.dto.response.AlgoOrderDetails;
import com.cp.aitg.okx.dto.response.PositionInfo;
import com.cp.aitg.okx.exception.OkxApiException;
import com.cp.aitg.okx.mapping.OkxStatusMapper; // 注入状态映射器
import com.cp.aitg.okx.service.OkxApiService;
import com.cp.aitg.persistence.OrderState;
import com.cp.aitg.persistence.StatePersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderMonitoringService {

    private final StatePersistenceService statePersistenceService;
    private final OkxApiService okxApiService;
    private final OkxStatusMapper statusMapper; // 注入状态映射器
    private final NotificationService notificationService; // 使用该接口

    // 通过配置属性定义 cron 表达式以便灵活配置
    // @Value("${app.monitoring.cron:0 */1 * * * ?}") // 示例：从配置中注入
    private static final String MONITORING_CRON = "0 */1 * * * ?"; // 默认：每分钟执行一次

    @Scheduled(cron = MONITORING_CRON)
    public void monitorActiveOrders() {
        log.info("开始执行活动订单监控任务...");
        List<OrderState> activeOrders = statePersistenceService.findActiveOrders();

        if (activeOrders.isEmpty()) {
            log.debug("当前没有活动订单需要监控。");
            return;
        }

        log.info("正在监控 {} 个活动订单...", activeOrders.size());
        for (OrderState orderState : activeOrders) {
            try {
                monitorOrder(orderState);
            } catch (Exception e) {
                // 针对单个订单记录错误日志，但继续监控其它订单
                log.error("监控订单 ClOrdID: {} 时出错: {}", orderState.getClOrdId(), e.getMessage(), e);
                // 可考虑为每个订单添加错误追踪，以避免重复失败的查询
            }
        }
        log.info("活动订单监控任务结束。");
    }

    private void monitorOrder(OrderState orderState) {
        log.debug("监控订单: ClOrdID={}, AlgoID={}, OrdID={}",
                orderState.getClOrdId(), orderState.getAlgoId(), orderState.getOrdId());

        if (StringUtils.hasText(orderState.getAlgoId())) {
            monitorAlgoOrder(orderState);
        } else if (StringUtils.hasText(orderState.getOrdId())) {
            monitorStandardOrder(orderState);
        } else {
            log.warn("订单状态缺少 algoId 和 ordId，无法进行监控。ClOrdID: {}", orderState.getClOrdId());
            // 也许将此订单标记为失败
            updateStateToFailed(orderState, "缺失订单标识");
        }
    }

    // --- 算法订单监控逻辑 ---
    private void monitorAlgoOrder(OrderState orderState) {
        String algoId = orderState.getAlgoId();
        try {
            // 1. 首先检查历史记录中是否存在终结状态
            Optional<AlgoOrderDetails> closedDetailsOpt = findClosedAlgoOrderInHistory(algoId);
            if (closedDetailsOpt.isPresent()) {
                handleClosure(orderState, closedDetailsOpt.get());
                return;
            }

            // 2. 如果没有关闭状态，则检查待处理/实时状态
            Optional<AlgoOrderDetails> pendingDetailsOpt = findPendingAlgoOrder(algoId, orderState.getInstId());
            if (pendingDetailsOpt.isEmpty()) {
                log.warn("历史记录或待处理订单中均未查询到该算法订单。AlgoID: {}。正在检查仓位。", algoId);
                checkPositionAndHandleClosureIfNeeded(orderState);
                return;
            }

            // 3. 处理当前状态
            processAlgoOrderState(orderState, pendingDetailsOpt.get());

        } catch (OkxApiException e) {
            log.error("监控 AlgoID {} 时 API 出错：Code={}, Msg={}", algoId, e.getOkxErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("监控 AlgoID {} 时发生意外错误：{}", algoId, e.getMessage(), e);
        }
    }

    private Optional<AlgoOrderDetails> findClosedAlgoOrderInHistory(String algoId) throws OkxApiException {
        OkxResponse<List<AlgoOrderDetails>> historyResponse = okxApiService.getAlgoOrderHistory("effective,canceled,order_failed", algoId);
        if (isOkxResponseSuccess(historyResponse)) {
            return historyResponse.getData().stream()
                    .filter(d -> algoId.equals(d.getAlgoId()))
                    .findFirst();
        }
        // 如有必要处理 API 错误响应（例如记录日志，但可能不停止监控）
        if (!OkxApiConstants.SUCCESS_CODE.equals(historyResponse.getCode())) {
            log.warn("查询算法订单历史记录失败。AlgoID: {}，Code={}, Msg={}", algoId, historyResponse.getCode(), historyResponse.getMsg());
        }
        return Optional.empty();
    }

    private Optional<AlgoOrderDetails> findPendingAlgoOrder(String algoId, String instId) throws OkxApiException {
        OkxResponse<List<AlgoOrderDetails>> pendingResponse = okxApiService.getPendingAlgoOrders(algoId, null, instId, null);
        if (isOkxResponseSuccess(pendingResponse)) {
            return pendingResponse.getData().stream()
                    .filter(d -> algoId.equals(d.getAlgoId()))
                    .findFirst();
        }
        if (!OkxApiConstants.SUCCESS_CODE.equals(pendingResponse.getCode())) {
            log.warn("查询待处理算法订单失败。AlgoID: {}，Code={}, Msg={}", algoId, pendingResponse.getCode(), pendingResponse.getMsg());
        }
        return Optional.empty();
    }

    private void processAlgoOrderState(OrderState currentState, AlgoOrderDetails details) {
        OrderState.Status currentInternalStatus = currentState.getStatus();
        OrderState.Status newInternalStatus = statusMapper.mapAlgoStatusToInternal(details.getState());

        log.debug("AlgoID: {}，OKX 状态: {}，当前内部状态: {}，映射后的内部状态: {}",
                details.getAlgoId(), details.getState(), currentInternalStatus, newInternalStatus);

        // 状态转换逻辑
        if (newInternalStatus == OrderState.Status.CLOSED && currentInternalStatus != OrderState.Status.CLOSED) {
            handleClosure(currentState, details);
        } else if (newInternalStatus == OrderState.Status.OPEN && currentInternalStatus == OrderState.Status.PLACED) {
            handlePositionOpened(currentState, details.getActualPx()); // 尝试获取实际价格
        } else if (newInternalStatus == OrderState.Status.MONITORING && currentInternalStatus == OrderState.Status.PLACED) {
            // 可选：如有需要，从 PLACED 状态转换到 MONITORING
            updateOrderState(currentState, OrderState.Status.MONITORING, null);
            log.info("AlgoID: {} 转换为 MONITORING 状态", currentState.getAlgoId());
        } else if (newInternalStatus != currentInternalStatus && newInternalStatus != OrderState.Status.UNKNOWN) {
            // 记录其他可能意外的状态变化
            log.info("AlgoID: {} 状态变化: {} -> {} (OKX 状态: {})",
                    currentState.getAlgoId(), currentInternalStatus, newInternalStatus, details.getState());
            updateOrderState(currentState, newInternalStatus, null);
        }
        // 如果状态保持为 OPEN 或 MONITORING，则继续正常监控
    }

    // --- 标准订单监控逻辑 ---
    private void monitorStandardOrder(OrderState orderState) {
        String ordId = orderState.getOrdId();
        String instId = orderState.getInstId();
        try {
            // 1. 查询标准订单详情
            // TODO: 在 OkxApiService 中实现 getStandardOrderDetails 并定义 OrderDetails DTO
            // OkxResponse<List<OrderDetails>> detailsResponse = okxApiService.getStandardOrderDetails(instId, ordId, null);
            Optional<AlgoOrderDetails> detailsOpt = Optional.empty(); // 占位符

            /* // 当实现后替换占位符为实际调用
            if (isOkxResponseSuccess(detailsResponse)) {
                detailsOpt = detailsResponse.getData().stream()
                               .filter(d -> ordId.equals(d.getOrdId())) // 假设 OrderDetails 有 getOrdId() 方法
                               .findFirst();
            } else if (!OkxApiConstants.SUCCESS_CODE.equals(detailsResponse.getCode())) {
                 log.warn("查询标准订单详情失败。OrdID: {}，Code={}, Msg={}", ordId, detailsResponse.getCode(), detailsResponse.getMsg());
            }
            */

            if (detailsOpt.isEmpty()) {
                log.warn("未查询到标准订单。OrdID: {}。正在检查仓位。", ordId);
                // 标准订单可能比算法订单更快从历史记录中消失
                // 若缺少订单详情，则更多依赖于仓位检查
                checkPositionAndHandleClosureIfNeeded(orderState);
                return;
            }

            // 2. 处理当前状态
            processStandardOrderState(orderState, detailsOpt.get());

        } /*catch (OkxApiException e) { // 实现后捕获
            log.error("监控 OrdID {} 时 API 出错：Code={}, Msg={}", ordId, e.getOkxErrorCode(), e.getMessage());
        }*/ catch (Exception e) {
            log.error("监控 OrdID {} 时发生意外错误：{}", ordId, e.getMessage(), e);
        }
    }

    private void processStandardOrderState(OrderState currentState, AlgoOrderDetails details) {
        OrderState.Status currentInternalStatus = currentState.getStatus();
        // 假设 OrderDetails 有 getState() 方法
        // OrderState.Status newInternalStatus = statusMapper.mapStandardStatusToInternal(details.getState());

        // 占位符逻辑 —— 需要 details.getState() 信息
        OrderState.Status newInternalStatus = OrderState.Status.UNKNOWN; // 替换为实际映射

        log.debug("OrdID: {}，OKX 状态: {}，当前内部状态: {}，映射后的内部状态: {}",
                currentState.getOrdId(), "N/A"/*details.getState()*/, currentInternalStatus, newInternalStatus);

        // 标准订单的状态转换逻辑
        if (newInternalStatus == OrderState.Status.CLOSED && currentInternalStatus != OrderState.Status.CLOSED) {
            handleClosure(currentState, details);
        } else if (newInternalStatus == OrderState.Status.OPEN && currentInternalStatus == OrderState.Status.PLACED) {
            // 假设 OrderDetails 有 getAvgPx() 方法
            // handlePositionOpened(currentState, details.getAvgPx());
            handlePositionOpened(currentState, null); // 占位符
        } else if (newInternalStatus != currentInternalStatus && newInternalStatus != OrderState.Status.UNKNOWN) {
            log.info("OrdID: {} 状态变化: {} -> {}", currentState.getOrdId(), currentInternalStatus, newInternalStatus);
            updateOrderState(currentState, newInternalStatus, null);
        }
    }

    // --- 公共辅助方法 ---

    private void handlePositionOpened(OrderState orderState, String actualEntryPrice) {
        if (orderState.getStatus() == OrderState.Status.OPEN) return; // 已经标记为开启

        log.info("订单已开启: ClOrdID={}", orderState.getClOrdId());
        orderState.setStatus(OrderState.Status.OPEN);
        orderState.setOpenedAt(LocalDateTime.now());
        if (StringUtils.hasText(actualEntryPrice)) {
            orderState.setActualEntryPrice(actualEntryPrice);
            log.info("已记录实际入场价格: {}", actualEntryPrice);
        }
        updateOrderState(orderState, OrderState.Status.OPEN, null);
        notificationService.sendPositionOpened(orderState);
    }

    private void handleClosure(OrderState orderState, Object details) { // 使用 Object 类型以实现多态
        if (orderState.getStatus() == OrderState.Status.CLOSED) {
            log.debug("订单已标记为 CLOSED，跳过关闭处理。ClOrdID={}", orderState.getClOrdId());
            return;
        }
        log.info("正在处理订单关闭：ClOrdID={}", orderState.getClOrdId());

        String closeReason = "Unknown";
        String okxState = "N/A";
        if (details instanceof AlgoOrderDetails algoDetails) {
            closeReason = statusMapper.determineAlgoCloseReason(algoDetails);
            okxState = algoDetails.getState();
//TODO : 需要根据实际情况处理不同的状态
            // 例如：如果是 TP/SL 触发，则可能需要记录实际退出价格
            // orderState.setExitPrice(algoDetails.getExitPrice());
        } else if (details instanceof AlgoOrderDetails standardDetails) {
            // closeReason = statusMapper.determineStandardCloseReason(standardDetails);
            // okxState = standardDetails.getState();
            // 占位符，直到定义 OrderDetails
            closeReason = "Closed (Standard Order)";
        } else if (details == null) {
            // 由仓位检查或其他方式检测到关闭
            closeReason = orderState.getCloseReason() != null ? orderState.getCloseReason() : "检测到关闭";
        }

        log.info("订单 ClOrdID={} 已关闭。OKX 状态: {}，确定原因: {}", orderState.getClOrdId(), okxState, closeReason);

        orderState.setStatus(OrderState.Status.CLOSED);
        orderState.setClosedAt(LocalDateTime.now());
        orderState.setCloseReason(closeReason);
        // TODO: 增强关闭逻辑 —— 获取实际退出价格并计算盈亏
        // 这可能涉及使用 ordId/algoId 查询成交历史记录
        // 例如：fetchAndSetExitDetails(orderState);

        updateOrderState(orderState, OrderState.Status.CLOSED, closeReason); // 持久化最终状态
        notificationService.sendPositionClosed(orderState);
    }

    private void updateStateToFailed(OrderState orderState, String reason) {
        if (orderState.getStatus() == OrderState.Status.FAILED) return;
        log.error("将订单标记为 FAILED: ClOrdID={}，原因: {}", orderState.getClOrdId(), reason);
        orderState.setStatus(OrderState.Status.FAILED);
        orderState.setClosedAt(LocalDateTime.now()); // 使用 closedAt 记录终止时间
        orderState.setCloseReason(reason);
        updateOrderState(orderState, OrderState.Status.FAILED, reason);
        // 发送告警通知？
        notificationService.sendErrorAlert("订单失败: ClOrdID=" + orderState.getClOrdId(), null);
    }

    private void checkPositionAndHandleClosureIfNeeded(OrderState orderState) {
        // 仅在订单可能处于开启或已下单状态时检查仓位
        if (orderState.getStatus() != OrderState.Status.OPEN &&
                orderState.getStatus() != OrderState.Status.PLACED &&
                orderState.getStatus() != OrderState.Status.MONITORING) {
            return;
        }

        log.debug("正在对 ClOrdID={} 进行仓位检查", orderState.getClOrdId());
        try {
            OkxResponse<List<PositionInfo>> positionResponse = okxApiService.getPositions(null, orderState.getInstId());
            boolean positionExists = false;
            if (isOkxResponseSuccess(positionResponse)) {
                positionExists = positionResponse.getData().stream()
                        .anyMatch(p -> Objects.equals(orderState.getInstId(), p.getInstId()) &&
                                Objects.equals(orderState.getPosSide(), p.getPosSide()) && // 同时检查 posSide
                                StringUtils.hasText(p.getPos()) && Double.parseDouble(p.getPos()) > 0); // 检查 pos > 0
            } else {
                log.warn("查询仓位信息失败，跳过检查。ClOrdID={}，Code={}，Msg={}",
                        orderState.getClOrdId(), positionResponse.getCode(), positionResponse.getMsg());
                return; // 当仓位检查失败时，不要假定订单已关闭
            }

            if (!positionExists && (orderState.getStatus() == OrderState.Status.OPEN ||
                    orderState.getStatus() == OrderState.Status.MONITORING)) {
                log.warn("开放/监控中的订单 ClOrdID={} 仓位消失，标记为关闭。", orderState.getClOrdId());
                orderState.setCloseReason("仓位被外部/意外关闭"); // 在调用 handleClosure 之前设置原因
                handleClosure(orderState, null); // 触发关闭处理
            } else if (!positionExists && orderState.getStatus() == OrderState.Status.PLACED) {
                log.warn("下单订单 ClOrdID={} 未查询到仓位，标记为失败/关闭。", orderState.getClOrdId());
                // 决定此处是否应标记为 FAILED 或 CLOSED/CANCELED
                updateStateToFailed(orderState, "已下单但未查到仓位");
            } else if (positionExists && orderState.getStatus() == OrderState.Status.PLACED) {
                // 仓位存在但订单状态仍为 PLACED？应转换为 OPEN 状态
                // 这种情况可能发生在初始成交通知/状态更新遗漏的情况下
                log.warn("下单订单 ClOrdID={} 检测到仓位，标记为 OPEN。", orderState.getClOrdId());
                // 若可能则尝试从仓位中获取入场价格
                String avgPx = positionResponse.getData().stream()
                        .filter(p -> Objects.equals(orderState.getInstId(), p.getInstId()) &&
                                Objects.equals(orderState.getPosSide(), p.getPosSide()))
                        .map(PositionInfo::getAvgPx).findFirst().orElse(null);
                handlePositionOpened(orderState, avgPx);
            }
            // 如果仓位存在且状态为 OPEN/MONITORING，则不做处理——属于正常状态

        } catch (OkxApiException e) {
            log.error("ClOrdID={} 仓位检查时 API 出错：{}", orderState.getClOrdId(), e.getMessage());
        } catch (NumberFormatException e) {
            log.error("ClOrdID={} 仓位检查时解析仓位数量错误：{}", orderState.getClOrdId(), e.getMessage());
        } catch (Exception e) {
            log.error("ClOrdID={} 仓位检查时发生意外错误：{}", orderState.getClOrdId(), e.getMessage(), e);
        }
    }

    private void updateOrderState(OrderState orderState, OrderState.Status newStatus, String reason) {
        orderState.setStatus(newStatus);
        orderState.setUpdatedAt(LocalDateTime.now());
        if (reason != null) { // 仅在提供原因时更新
            orderState.setCloseReason(reason);
        }
        try {
            statePersistenceService.updateOrderState(orderState);
            log.debug("订单状态已在持久化存储中更新：ClOrdID={}，新状态={}", orderState.getClOrdId(), newStatus);
        } catch (Exception e) {
            log.error("无法为 ClOrdID={} 更新持久化的订单状态：{}", orderState.getClOrdId(), e.getMessage(), e);
            // 严重错误 —— 内存中的状态可能与持久化状态不一致
            // 建议为持久化失败添加告警或重试逻辑
        }
    }

    // 检查 OKX 响应是否成功（集中处理）
    private boolean isOkxResponseSuccess(OkxResponse<?> response) {
        // 将数据为空的列表视为可能有效但结果为空，并不一定表示失败
        return response != null && OkxApiConstants.SUCCESS_CODE.equals(response.getCode()) && response.getData() != null;
        // 优化：仅当预期返回多个项时，检查数据是否为列表且不为空。
        // 对于单个项的 GET 请求，数据非列表或为空也可能是有效的。
        // 让我们恢复之前更适用于列表结果的检查方式：
        // return response != null && OkxApiConstants.SUCCESS_CODE.equals(response.getCode()) && response.getData() != null && !((List<?>) response.getData()).isEmpty();
        // 目前先使用更通用的检查：code 为 "0" 且数据不为 null。调用方可根据需要检查是否为空。
        // return response != null && OkxApiConstants.SUCCESS_CODE.equals(response.getCode()) && response.getData() != null;
    }
}

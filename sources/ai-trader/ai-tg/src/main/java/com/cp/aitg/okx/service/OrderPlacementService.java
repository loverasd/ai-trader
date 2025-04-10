package com.cp.aitg.okx.service;

import com.cp.aitg.okx.common.OkxApiConstants;
import com.cp.aitg.okx.controller.dto.ManualOrderRequest;
import com.cp.aitg.okx.dto.OkxResponse;
import com.cp.aitg.okx.dto.request.PlaceAlgoOrderRequest;
import com.cp.aitg.okx.dto.request.PlaceOrderRequest;
import com.cp.aitg.okx.dto.request.order.OrderRequest;
import com.cp.aitg.okx.dto.response.AlgoOrderResponse;
import com.cp.aitg.okx.exception.OkxApiException;
import com.cp.aitg.persistence.OrderState;
import com.cp.aitg.persistence.StatePersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderPlacementService {

    private final OkxApiService okxApiService;
    private final StatePersistenceService statePersistenceService;

    /**
     * 执行手动订单请求，决定使用标准订单还是算法订单下单。
     *
     * @param manualRequest 手动订单请求 DTO。
     * @return 对应的 OKX API 响应（可能包装的是 OrderResponse 或 AlgoOrderResponse）。
     * @throws OkxApiException          当 API 调用失败时抛出异常。
     * @throws IllegalArgumentException 当请求无效时抛出异常。
     */
    public OkxResponse<?> placeManualOrder(ManualOrderRequest manualRequest)
            throws OkxApiException, IllegalArgumentException {

        validateManualRequest(manualRequest);
        String clOrdId = generateClOrdId(manualRequest.getClOrdId());
        // --- 决定使用哪种 API 路径 ---
        if (isAlgoOrder(manualRequest)) {
            // 使用算法订单 API (/order-algo)
            return placeManualAlgoOrder(manualRequest, clOrdId);
        } else {
            // 使用标准订单 API (/order)
            return placeManualStandardOrder(manualRequest, clOrdId);
        }
    }

    /**
     * 下单方法，接收 OrderRequest 基类对象。
     * 如果传入的订单对象属于算法订单类型（Conditional、Trigger、TrailingStop、TWAP、Chase），
     * 则使用算法订单 API；否则使用标准订单 API。
     *
     * @param request 订单请求对象，可能为基础订单或其子类
     * @return OKX API 返回的响应
     * @throws OkxApiException          下单过程中的异常
     * @throws IllegalArgumentException 参数校验不通过时抛出异常
     */
    public OkxResponse<?> placeOrder(OrderRequest request)
            throws OkxApiException, IllegalArgumentException {

        // 先对订单请求对象进行校验（根据不同订单类型补充不同验证）
        validateOrderRequest(request);

        // 生成或使用提供的订单客户端 ID
        String clOrdId = generateClOrdId(request.getAlgoClOrdId());

        // 根据订单请求对象的具体类型决定调用哪个 API 路径
        if (isAlgoOrder(request)) {
            // 订单为算法订单，调用 /order-algo 接口
            return placeAlgoOrder(request, clOrdId);
        } else {
            // 订单为标准订单，调用 /order 接口
            return placeStandardOrder(request, clOrdId);
        }
    }

    /**
     * 调用算法订单 API (/order-algo) 下单。
     * 根据不同的算法订单类型，可在此方法中分支处理各自特殊的业务逻辑。
     *
     * @param request 订单请求对象（算法订单类型）
     * @param clOrdId 订单客户端 ID
     * @return OKX 返回的响应
     * @throws OkxApiException 下单过程中的异常
     */
    private OkxResponse<?> placeAlgoOrder(OrderRequest request, String clOrdId)
            throws OkxApiException {
        // 例如，根据具体类型进行不同处理：
        if (request instanceof OrderRequest.ConditionalOrderRequest) {
            // 处理条件订单（如止盈/止损订单）的下单逻辑
            // 此处调用 /order-algo 接口并传入对应参数
        } else if (request instanceof OrderRequest.TriggerOrderRequest) {
            // 处理触发订单的下单逻辑
        } else if (request instanceof OrderRequest.TrailingStopOrderRequest) {
            // 处理追踪止损订单的下单逻辑
        } else if (request instanceof OrderRequest.TWAPOrderRequest) {
            // 处理 TWAP 订单的下单逻辑
        } else if (request instanceof OrderRequest.ChaseOrderRequest) {
            // 处理追单订单的下单逻辑
        }
        // 这里返回一个示例响应，实际应调用 HTTP 客户端进行接口调用
        return new OkxResponse<>();
    }

    /**
     * 调用标准订单 API (/order) 下单。
     *
     * @param request 订单请求对象（标准订单）
     * @param clOrdId 订单客户端 ID
     * @return OKX 返回的响应
     * @throws OkxApiException 下单过程中的异常
     */
    private OkxResponse<?> placeStandardOrder(OrderRequest request, String clOrdId)
            throws OkxApiException {
        // 调用标准下单接口，传入 request 对象中的参数
        return new OkxResponse<>();
    }

    /**
     * 校验订单请求，根据不同订单类型进行不同的业务验证。
     * <p>
     * 例如：
     * - 如果是限价单（ordType 为 "limit"），需要验证必须提供价格（字段 px，这里假设有该字段）。
     * - 如果是触发订单（TriggerOrderRequest），则必须提供触发价（triggerPx）和下单价（orderPx）。
     *
     * @param request 订单请求对象
     * @throws IllegalArgumentException 校验不通过则抛出异常
     */
    private void validateOrderRequest(OrderRequest request) {
        // 示例：限价单必须提供价格（假设 OrderRequest 中有 getPx() 方法，如果没有需要在子类中增加该验证）
        if ("limit".equalsIgnoreCase(request.getOrdType()) &&
                !StringUtils.hasText(/*request.getPx()*/ "价格字段")) {
            throw new IllegalArgumentException("手动限价单必须提供价格 (px)");
        }
        // 针对触发订单的额外验证
        if (request instanceof OrderRequest.TriggerOrderRequest) {
            OrderRequest.TriggerOrderRequest triggerOrder = (OrderRequest.TriggerOrderRequest) request;
            if (!StringUtils.hasText(triggerOrder.getTriggerPx())) {
                throw new IllegalArgumentException("触发订单必须提供触发价 (triggerPx)");
            }
            if (!StringUtils.hasText(triggerOrder.getOrderPx())) {
                throw new IllegalArgumentException("触发订单必须提供下单价 (orderPx)");
            }
        }
        // 可根据业务需要，为其他订单类型（例如 TrailingStopOrderRequest、TWAPOrderRequest 等）添加额外校验逻辑
    }


    /**
     * 判断订单请求是否属于算法订单类型。
     * 算法订单类型包括：
     * - ConditionalOrderRequest （条件订单，包括止盈/止损订单）
     * - TriggerOrderRequest（触发订单）
     * - TrailingStopOrderRequest（追踪止损订单）
     * - TWAPOrderRequest（TWAP 订单）
     * - ChaseOrderRequest（追单订单）
     *
     * @param request 订单请求对象
     * @return true 表示为算法订单，false 为标准订单
     */
    private boolean isAlgoOrder(OrderRequest request) {
        return request instanceof OrderRequest.ConditionalOrderRequest
                || request instanceof OrderRequest.TriggerOrderRequest
                || request instanceof OrderRequest.TrailingStopOrderRequest
                || request instanceof OrderRequest.TWAPOrderRequest
                || request instanceof OrderRequest.ChaseOrderRequest;
    }


    /**
     * 判断手动订单请求是否为算法订单。
     * 除了判断止损/止盈参数外，还会检查触发订单、追踪止损以及 TWAP 订单相关参数。
     *
     * @param request 手动订单请求 DTO
     * @return 若满足任一算法单条件则返回 true；否则返回 false。
     */
    private boolean isAlgoOrder(ManualOrderRequest request) {
        // 判断是否存在止损/止盈参数
        boolean hasStopLoss = StringUtils.hasText(request.getSlTriggerPx());
        boolean hasTakeProfit = StringUtils.hasText(request.getTpTriggerPx());
        // 判断是否存在触发订单参数（例如触发价格）
        boolean hasTrigger = StringUtils.hasText(request.getTriggerPx());
        // 判断是否存在追踪止损订单参数（例如回调比例或回调幅度）
        boolean hasTrailing = StringUtils.hasText(request.getCallbackRatio())
                || StringUtils.hasText(request.getCallbackSpread());
        // 判断是否存在 TWAP 订单参数（例如时间间隔和价格限制）
        boolean hasTwap = StringUtils.hasText(request.getTimeInterval())
                && StringUtils.hasText(request.getPxLimit());

        return hasStopLoss || hasTakeProfit || hasTrigger || hasTrailing || hasTwap;
    }

    // --- 针对不同订单类型的私有方法 ---

    private OkxResponse<List<AlgoOrderResponse>> placeManualAlgoOrder(ManualOrderRequest manualRequest, String clOrdId) {
        log.info("Placing manual order with TP/SL via /order-algo. ClOrdID: {}", clOrdId);
        PlaceAlgoOrderRequest algoRequest = buildAlgoOrderRequest(manualRequest, clOrdId);
        OkxResponse<List<AlgoOrderResponse>> response = okxApiService.placeAlgoOrderWithSLTP(algoRequest);
        handleAlgoOrderResponse(response, manualRequest, clOrdId);
        return response;
    }

    private OkxResponse<List<AlgoOrderResponse>> placeManualStandardOrder(ManualOrderRequest manualRequest, String clOrdId) {
        log.info("Placing manual standard order via /order. ClOrdID: {}", clOrdId);
        PlaceOrderRequest standardRequest = buildStandardOrderRequest(manualRequest, clOrdId);
        OkxResponse<List<AlgoOrderResponse>> response = okxApiService.placeStandardOrder(standardRequest);
        handleStandardOrderResponse(response, manualRequest, clOrdId);
        return response;
    }

    // --- 请求构建逻辑 ---

    private PlaceAlgoOrderRequest buildAlgoOrderRequest(ManualOrderRequest manualRequest, String clOrdId) {
        PlaceAlgoOrderRequest.PlaceAlgoOrderRequestBuilder builder = PlaceAlgoOrderRequest.builder()
                .instId(manualRequest.getInstId())
                .tdMode(manualRequest.getTdMode())
                .side(manualRequest.getSide())
                .posSide(manualRequest.getPosSide())
                .ordType(manualRequest.getOrdType()) // 假定 OKX 在带有 TP/SL 的情况下允许限价/市价
                .sz(manualRequest.getSz())
                .ccy(manualRequest.getCcy())
                .px(manualRequest.getPx()) // 市价单时为 null
                .clOrdId(clOrdId)
                .tag(StringUtils.hasText(manualRequest.getTag()) ? manualRequest.getTag() : "ManualAlgoOrder");

        // 只有在存在触发价格时才添加 TP/SL
        if (StringUtils.hasText(manualRequest.getTpTriggerPx())) {
            builder.tpTriggerPx(manualRequest.getTpTriggerPx());
            builder.tpOrdPx(StringUtils.hasText(manualRequest.getTpOrdPx()) ? manualRequest.getTpOrdPx() : "-1");
        }
        if (StringUtils.hasText(manualRequest.getSlTriggerPx())) {
            builder.slTriggerPx(manualRequest.getSlTriggerPx());
            builder.slOrdPx(StringUtils.hasText(manualRequest.getSlOrdPx()) ? manualRequest.getSlOrdPx() : "-1");
        }
        return builder.build();
    }

    private PlaceOrderRequest buildStandardOrderRequest(ManualOrderRequest manualRequest, String clOrdId) {
        return PlaceOrderRequest.builder()
                .instId(manualRequest.getInstId())
                .tdMode(manualRequest.getTdMode())
                .side(manualRequest.getSide())
                .posSide(manualRequest.getPosSide()) // 包含 posSide
                .ordType(manualRequest.getOrdType())
                .sz(manualRequest.getSz())
                .px(manualRequest.getPx()) // 市价单时为 null
                .clOrdId(clOrdId)
                .tag(StringUtils.hasText(manualRequest.getTag()) ? manualRequest.getTag() : "ManualStdOrder")
                .build();
    }

    // --- 响应处理与状态持久化 ---

    private void handleAlgoOrderResponse(OkxResponse<List<AlgoOrderResponse>> response, ManualOrderRequest request, String clOrdId) {
        if (isOkxResponseSuccess(response)) {
            AlgoOrderResponse orderResult = response.getData().get(0); // 假定第一个结果即为所需订单结果
            if (isOkxResultSuccess(orderResult.getSCode())) {
                String algoId = orderResult.getAlgoId();
                log.info("Manual Algo Order placed successfully. AlgoID: {}, ClOrdID: {}", algoId, clOrdId);
                persistInitialOrderState(request, clOrdId, algoId, null);
            } else {
                handleOkxResultError(orderResult.getSCode(), orderResult.getSMsg(), request);
            }
        } else if (!isOkxResponseCodeSuccess(response.getCode())) {
            handleOkxResponseError(response.getCode(), response.getMsg(), request);
        } else {
            handleOkxResponseEmptyData(response, "Algo Order");
        }
    }

    private void handleStandardOrderResponse(OkxResponse<List<AlgoOrderResponse>> response, ManualOrderRequest request, String clOrdId) {
        if (isOkxResponseSuccess(response)) {
            AlgoOrderResponse orderResult = response.getData().get(0); // 假定第一个结果即为所需订单结果
            if (isOkxResultSuccess(orderResult.getSCode())) {
                String ordId = orderResult.getOrdId();
                log.info("Manual Standard Order placed successfully. OrdID: {}, ClOrdID: {}", ordId, clOrdId);
                persistInitialOrderState(request, clOrdId, null, ordId);
            } else {
                handleOkxResultError(orderResult.getSCode(), orderResult.getSMsg(), request);
            }
        } else if (!isOkxResponseCodeSuccess(response.getCode())) {
            handleOkxResponseError(response.getCode(), response.getMsg(), request);
        } else {
            handleOkxResponseEmptyData(response, "Standard Order");
        }
    }

    private void persistInitialOrderState(ManualOrderRequest request, String clOrdId, String algoId, String ordId) {
        OrderState newState = createOrderState(request, clOrdId, algoId, ordId);
        try {
            statePersistenceService.saveOrderState(newState);
            log.info("Initial order state persisted: ClOrdID={}, AlgoID={}, OrdID={}", clOrdId, algoId, ordId);
        } catch (Exception e) {
            // 记录持久化错误，但即使 API 调用成功也不影响整个下单流程的返回
            log.error("Failed to persist initial order state for ClOrdID={}: {}", clOrdId, e.getMessage(), e);
            // 可考虑发送告警
        }
    }

    // --- 辅助方法 ---

    private void validateManualRequest(ManualOrderRequest request) {
        if ("limit".equalsIgnoreCase(request.getOrdType()) && !StringUtils.hasText(request.getPx())) {
            throw new IllegalArgumentException("手动限价单必须提供价格 (px)");
        }
        // 如有需要，可添加更复杂的业务验证
    }

    private String generateClOrdId(String providedClOrdId) {
        return StringUtils.hasText(providedClOrdId)
                ? providedClOrdId
                : "manual-" + UUID.randomUUID().toString().substring(0, 16); // 默认 ID 略长
    }

    private OrderState createOrderState(ManualOrderRequest request, String clOrdId, String algoId, String ordId) {
        OrderState newState = new OrderState();
        newState.setClOrdId(clOrdId);
        newState.setAlgoId(algoId);
        newState.setOrdId(ordId); // 确保 OrderState 包含 ordId 字段
        newState.setInstId(request.getInstId());
        newState.setStatus(OrderState.Status.PLACED);
        newState.setSource("MANUAL");
        newState.setCreatedAt(LocalDateTime.now());
        newState.setUpdatedAt(LocalDateTime.now()); // 初始化 updatedAt
        newState.setSide(request.getSide());
        newState.setPosSide(request.getPosSide());
        newState.setEntryPrice(request.getPx());
        newState.setSize(request.getSz());
        newState.setSlTriggerPx(request.getSlTriggerPx());
        newState.setTpTriggerPx(request.getTpTriggerPx());
        newState.setSlOrdPx(request.getSlOrdPx());
        newState.setTpOrdPx(request.getTpOrdPx());
        return newState;
    }

    private boolean isOkxResponseSuccess(OkxResponse<?> response) {
        return response != null && OkxApiConstants.SUCCESS_CODE.equals(response.getCode()) && response.getData() != null && !((List<?>) response.getData()).isEmpty();
    }

    private boolean isOkxResponseCodeSuccess(String code) {
        return OkxApiConstants.SUCCESS_CODE.equals(code);
    }

    private boolean isOkxResultSuccess(String sCode) {
        return OkxApiConstants.SUCCESS_CODE.equals(sCode);
    }

    private void handleOkxResultError(String sCode, String sMsg, ManualOrderRequest request) {
        log.error("OKX order placement failed (Result Error): SCode={}, SMsg={}, Request={}", sCode, sMsg, request);
        throw new OkxApiException(sMsg, sCode, null); // 抛出异常，由 Controller 捕获
    }

    private void handleOkxResponseError(String code, String msg, ManualOrderRequest request) {
        log.error("OKX order placement failed (Response Error): Code={}, Msg={}, Request={}", code, msg, request);
        // 如果 OkxApiService 已对此情况抛出了异常，则无需在此处抛出异常
        // 如果 OkxApiService 返回错误响应而不是抛出异常，则可在此处抛出异常：
        // throw new OkxApiException(msg, code, null);
        // 此处假定 OkxApiService 对非 "0" 的 code 已经抛出异常
    }

    private void handleOkxResponseEmptyData(OkxResponse<?> response, String orderType) {
        log.error("OKX {} placement response error: Code '0' but data is null or empty. Response: {}", orderType, response);
        throw new OkxApiException(orderType + " placed successfully according to code, but no order details returned.", response.getCode(), null);
    }
}

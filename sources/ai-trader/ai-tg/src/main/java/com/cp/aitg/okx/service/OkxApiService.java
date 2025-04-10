package com.cp.aitg.okx.service;

import com.cp.aitg.okx.client.OkxApiClient;
import com.cp.aitg.okx.common.OkxApiConstants;
import com.cp.aitg.okx.dto.*; // 引入所有 DTO
import com.cp.aitg.okx.dto.request.*;
import com.cp.aitg.okx.dto.response.*; // 引入所有 Response DTO
import com.cp.aitg.okx.exception.OkxApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper; // 注入 ObjectMapper


@Slf4j
@Service
@RequiredArgsConstructor // Lombok: 自动生成包含 final 字段的构造函数并注入
public class OkxApiService {

    private final OkxApiClient apiClient;

    private final ObjectMapper objectMapper = new ObjectMapper(); // 注入 ObjectMapper

    // 辅助方法获取泛型 List 的 JavaType
    private <T> JavaType getListType(Class<T> elementClass) {
        return objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass);
    }
    // 辅助方法获取泛型 OkxResponse<T> 的 JavaType
    private <T> JavaType getOkxResponseType(JavaType dataType) {
        return objectMapper.getTypeFactory().constructParametricType(OkxResponse.class, dataType);
    }

    /**
     * 下策略委托单 (包含止盈止损)
     *
     * @param request 包含订单详情和止盈止损信息的请求对象
     * @return 包含 algoId 的响应数据
     * @throws OkxApiException 如果 API 调用失败或业务出错
     */
    public OkxResponse<List<AlgoOrderResponse>> placeAlgoOrderWithSLTP(PlaceAlgoOrderRequest request) throws OkxApiException {
        log.info("准备下策略委托单 (带止盈止损): {}", request);
        // 构建 List<AlgoOrderResponse> 的类型
        JavaType responseDataType = getListType(AlgoOrderResponse.class);
        // 构建 OkxResponse<List<AlgoOrderResponse>> 的类型
        JavaType okxResponseListType = getOkxResponseType(responseDataType);

        OkxResponse<List<AlgoOrderResponse>> response = apiClient.post(
                OkxApiConstants.PLACE_ALGO_ORDER_PATH,
                request,
                okxResponseListType // 传递构造好的 JavaType
        );
        // 检查业务错误码 (ApiClient 应该已经处理了 HTTP 错误)
        if (!OkxApiConstants.SUCCESS_CODE.equals(response.getCode())) {
            log.error("策略委托下单失败: Code={}, Msg={}, Request={}", response.getCode(), response.getMsg(), request);
            throw new OkxApiException(response.getMsg(), response.getCode(), null); // 抛出业务异常
        }
        log.info("策略委托下单成功: {}", response.getData());
        return response;
    }
    /**
     * 下标准订单 (不直接附加止盈止损).
     * 调用 OKX V5 的 `/api/v5/trade/order` 接口 (POST)。
     *
     * @param request 包含订单详情的 {@link PlaceOrderRequest} 对象。
     * @return {@link OkxResponse} 包含 {@link AlgoOrderResponse} 列表的结果。
     *         即使只下一个订单，OKX 接口也返回列表形式。
     * @throws OkxApiException 如果 API 调用失败（网络、签名错误）或 OKX 返回业务错误码。
     */
    public OkxResponse<List<AlgoOrderResponse>> placeStandardOrder(PlaceOrderRequest request) throws OkxApiException {
        // 记录准备下单的日志，包含请求详情
        log.info("准备下达标准订单: {}", request);

        // 验证请求对象是否为 null (虽然调用者应该保证，但加一层防护)
        if (request == null) {
            throw new IllegalArgumentException("标准订单请求不能为空");
        }

        // 构建 Jackson 需要的 JavaType，用于正确反序列化响应体中的 data 字段
        JavaType responseDataType = getListType(AlgoOrderResponse.class);
        // 将列表类型包装在 OkxResponse 泛型中
        JavaType okxResponseListType = getOkxResponseType(responseDataType);

        // 调用底层 ApiClient 的 post 方法
        OkxResponse<List<AlgoOrderResponse>> response = apiClient.post(
                OkxApiConstants.PLACE_ORDER_PATH,         // 使用标准下单的 API 路径
                request,     // 将单个请求对象包装成只含一个元素的 List
                okxResponseListType                      // 传递构造好的完整响应类型信息
        );

        // --- 处理响应 ---

        // 检查外层 OkxResponse 的 code 是否为 "0" (代表整个 API 请求成功)
        if (!OkxApiConstants.SUCCESS_CODE.equals(response.getCode())) {
            log.error("标准订单下单 API 请求失败: Code={}, Msg={}, Request={}", response.getCode(), response.getMsg(), request);
            // 如果外层 code 非 "0"，直接抛出异常，携带 OKX 返回的错误信息
            // 注意：OkxApiException 的构造函数应能处理这种情况
            throw new OkxApiException(response.getMsg(), response.getCode(), null);
        }

        // 检查 data 字段是否有效 (理论上 code="0" 时 data 不应为 null，但多一层检查更安全)
        if (response.getData() == null || response.getData().isEmpty()) {
            log.error("标准订单下单响应异常: Code='0' 但 data 为空或 null. Response: {}", response);
            throw new OkxApiException("标准订单下单请求成功，但未返回有效的订单结果数据", response.getCode(), null);
        }

        // 检查 data 数组中每个订单结果的 sCode 是否为 "0" (代表具体某个订单的处理成功)
        // 即使外层 code="0"，内部订单也可能因为业务规则（如余额不足）而失败
        for (AlgoOrderResponse orderResult : response.getData()) {
            if (!OkxApiConstants.SUCCESS_CODE.equals(orderResult.getSCode())) {
                log.error("标准订单下单部分失败（内部订单错误）: OrdId={}, ClOrdId={}, SCode={}, SMsg={}, Request={}",
                        orderResult.getOrdId(), orderResult.getClOrdId(), orderResult.getSCode(), orderResult.getSMsg(), request);
                // 如果任何一个内部订单处理失败，抛出异常
                throw new OkxApiException(orderResult.getSMsg(), orderResult.getSCode(), null);
            }
        }

        // 如果所有检查都通过，记录成功日志
        log.info("标准订单下单成功: {}", response.getData());

        // 返回完整的、成功的响应对象
        return response;
    }


    /**
     * 撤销策略委托
     * @param request 包含要撤销的 algoId 和 instId 的请求对象列表
     * @return 撤销结果
     * @throws OkxApiException API 调用失败
     */
    public OkxResponse<List<CancelAlgoOrderResponse>> cancelAlgoOrder(List<CancelAlgoOrderRequest> request) throws OkxApiException {
        log.info("准备撤销策略委托: {}", request);
        JavaType responseDataType = getListType(CancelAlgoOrderResponse.class);
        JavaType okxResponseListType = getOkxResponseType(responseDataType);

        OkxResponse<List<CancelAlgoOrderResponse>> response = apiClient.post(
                OkxApiConstants.CANCEL_ALGO_ORDER_PATH,
                request,
                okxResponseListType
        );
         if (!OkxApiConstants.SUCCESS_CODE.equals(response.getCode())) {
            log.error("撤销策略委托失败: Code={}, Msg={}, Request={}", response.getCode(), response.getMsg(), request);
            throw new OkxApiException(response.getMsg(), response.getCode(), null);
        }
        log.info("撤销策略委托成功: {}", response.getData());
        return response;
    }


    /**
     * 获取未完成的策略委托列表
     * @param algoId 策略委托ID (可选)
     * @param instType 产品类型 (可选) SWAP, FUTURES, SPOT, OPTION
     * @param instId 产品ID (可选)
     * @param ordType 策略订单类型 (可选) conditional, oco, trigger, move_order_stop, iceberg, twap
     * @return 未完成策略委托列表
     * @throws OkxApiException API 错误
     */
    public OkxResponse<List<AlgoOrderDetails>> getPendingAlgoOrders(String algoId, String instType, String instId, String ordType) throws OkxApiException {
        Map<String, String> params = new HashMap<>();
        if (algoId != null) params.put("algoId", algoId);
        if (instType != null) params.put("instType", instType);
        if (instId != null) params.put("instId", instId);
        if (ordType != null) params.put("ordType", ordType);

        log.debug("查询未完成策略委托: {}", params);
        JavaType responseDataType = getListType(AlgoOrderDetails.class);
        JavaType okxResponseListType = getOkxResponseType(responseDataType);

        OkxResponse<List<AlgoOrderDetails>> response = apiClient.get(
                OkxApiConstants.GET_ALGO_ORDER_PENDING_PATH,
                params,
                okxResponseListType
        );
        // 不再需要检查 response.getCode()，ApiClient 应该处理或返回错误信息
        log.debug("查询未完成策略委托结果: {}", response.getData());
        return response;
    }

     /**
     * 获取策略委托历史 (近3个月)
     * @param state 策略委托状态 live, paused, canceled, effective, order_failed
     * @param algoId 策略委托ID (可选)
     * // ... 其他可选参数 instType, instId, ordType, after, before, limit
     * @return 策略委托历史列表
     * @throws OkxApiException API 错误
     */
    public OkxResponse<List<AlgoOrderDetails>> getAlgoOrderHistory(String state, String algoId /*... other params */) throws OkxApiException {
        Map<String, String> params = new HashMap<>();
        params.put("state", state); // 必须
        if (algoId != null) params.put("algoId", algoId);
        // ... 添加其他参数

        log.debug("查询策略委托历史: {}", params);
        JavaType responseDataType = getListType(AlgoOrderDetails.class);
        JavaType okxResponseListType = getOkxResponseType(responseDataType);

        OkxResponse<List<AlgoOrderDetails>> response = apiClient.get(
                OkxApiConstants.GET_ALGO_ORDER_HISTORY_PATH,
                params,
                okxResponseListType
        );
        log.debug("查询策略委托历史结果: {}", response.getData());
        return response;
    }


    /**
     * 获取持仓信息
     *
     * @param instType 产品类型 (可选, e.g., "SWAP", "FUTURES", "MARGIN", "SPOT")
     * @param instId   产品 ID (可选, e.g., "BTC-USDT-SWAP")
     * @return 持仓信息列表
     * @throws OkxApiException API 调用失败
     */
    public OkxResponse<List<PositionInfo>> getPositions(String instType, String instId) throws OkxApiException {
        Map<String, String> params = new HashMap<>();
        if (instType != null) params.put("instType", instType);
        if (instId != null) params.put("instId", instId);

        log.debug("查询持仓信息: instType={}, instId={}", instType, instId);
        JavaType responseDataType = getListType(PositionInfo.class);
        JavaType okxResponseListType = getOkxResponseType(responseDataType);

        OkxResponse<List<PositionInfo>> response = apiClient.get(
                OkxApiConstants.GET_POSITIONS_PATH,
                params,
                okxResponseListType
        );
         // ApiClient 已经处理了错误检查
        log.debug("查询持仓信息成功，数量: {}", response.getData() != null ? response.getData().size() : 0);
        return response;
    }

    // --- 可以添加更多方法，如获取普通订单详情、撤销普通订单等 ---
}
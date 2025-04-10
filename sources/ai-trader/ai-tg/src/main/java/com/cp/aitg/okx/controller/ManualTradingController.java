package com.cp.aitg.okx.controller;

import com.cp.aitg.okx.controller.dto.ManualOrderRequest;
import com.cp.aitg.okx.dto.OkxResponse;
import com.cp.aitg.okx.exception.OkxApiException;
import com.cp.aitg.okx.service.OrderPlacementService;
import com.cp.aitg.okx.util.OkxSignatureUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/manual")
@RequiredArgsConstructor
public class ManualTradingController {

    private final OrderPlacementService orderPlacementService; // Inject OrderPlacementService

    @PostMapping("/order")
    public ResponseEntity<?> placeManualOrder(@Valid @RequestBody ManualOrderRequest request) {
        log.info("接收到手动下单请求: {}", request);
        request.setClOrdId(OkxSignatureUtil.generateClOrdId(null));

        try {
            // Call the OrderPlacementService
            OkxResponse<?> response = orderPlacementService.placeManualOrder(request);

            if (!"0".equals(response.getCode())) {
                log.error("手动下单失败 (API Response Error): code={}, msg={}", response.getCode(), response.getMsg());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY) // Or map based on code
                        .body(Map.of("error", response.getMsg(), "okxCode", response.getCode()));
            }

            log.info("手动下单请求处理成功 (具体订单结果见日志): {}", response.getData());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (OkxApiException e) {
            // Handle exceptions thrown by OrderPlacementService or OkxApiService
            log.error("手动下单时捕获到 OKX API 异常: okxCode={}, httpStatus={}, message={}",
                    e.getOkxErrorCode(), e.getHttpStatusCode(), e.getMessage(), e);

            HttpStatus status;
            Integer httpCode = e.getHttpStatusCode();
            if (httpCode != null) {
                status = httpCode >= 400 && httpCode < 500
                        ? HttpStatus.BAD_REQUEST
                        : HttpStatus.INTERNAL_SERVER_ERROR;
            } else {
                // Business logic errors (like insufficient funds, invalid params caught by OKX)
                // often don't have HTTP error codes but have specific okxErrorCodes.
                // Map these to 422 Unprocessable Entity? Or 400 Bad Request? Let's use 422.
                status = (e.getOkxErrorCode() != null) ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.INTERNAL_SERVER_ERROR;
            }

            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("error", e.getMessage());
            if (e.getOkxErrorCode() != null) {
                errorBody.put("okxCode", e.getOkxErrorCode());
            }
            if (httpCode != null) {
                errorBody.put("httpCode", httpCode);
            }
            return ResponseEntity.status(status).body(errorBody);

        } catch (IllegalArgumentException e) {
            log.error("手动下单请求参数校验失败 (Service Level): {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("处理手动下单时发生未知内部错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "内部服务器错误，请联系管理员"));
        }
    }
}
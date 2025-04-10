package com.cp.aitg.okx.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // 方便创建
public class CancelAlgoOrderRequest {

    @NotBlank(message = "撤销策略委托时 instId 不能为空")
    @JsonProperty("instId")
    private String instId; // 产品ID

    @NotBlank(message = "撤销策略委托时 algoId 不能为空")
    @JsonProperty("algoId")
    private String algoId; // 策略委托ID
}
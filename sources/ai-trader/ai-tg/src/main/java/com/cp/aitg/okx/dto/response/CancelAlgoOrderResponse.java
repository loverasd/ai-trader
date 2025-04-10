package com.cp.aitg.okx.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelAlgoOrderResponse {

    /**
     * 策略委托单ID
     */
    @JsonProperty("algoId")
    private String algoId;

    /**
     * 结果代码，0 代表成功
     */
    @JsonProperty("sCode")
    private String sCode;

    /**
     * 结果消息
     */
    @JsonProperty("sMsg")
    private String sMsg;
}
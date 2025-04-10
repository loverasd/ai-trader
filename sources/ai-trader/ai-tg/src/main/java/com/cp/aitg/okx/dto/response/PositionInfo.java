package com.cp.aitg.okx.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionInfo {

    @JsonProperty("instType")
    private String instType; // 产品类型 SWAP, FUTURES, MARGIN, OPTION

    @JsonProperty("instId")
    private String instId;   // 产品ID

    @JsonProperty("posId")
    private String posId;    // 持仓ID

    @JsonProperty("posSide")
    private String posSide; // 持仓方向 long, short, net

    @JsonProperty("pos")
    private String pos;     // 持仓数量 (张或币)

    @JsonProperty("baseBal")
    private String baseBal; // 交易货币余额 (逐仓)

    @JsonProperty("quoteBal")
    private String quoteBal; // 保证金货币余额 (逐仓)

    @JsonProperty("availPos")
    private String availPos; // 可平仓数量

    @JsonProperty("avgPx")
    private String avgPx;   // 开仓均价

    @JsonProperty("upl")
    private String upl;     // 未实现收益

    @JsonProperty("uplRatio")
    private String uplRatio; // 未实现收益率

    @JsonProperty("lever")
    private String lever;   // 杠杆倍数

    @JsonProperty("liqPx")
    private String liqPx;   // 预估强平价

    @JsonProperty("imr")
    private String imr;     // 初始保证金

    @JsonProperty("mmr")
    private String mmr;     // 维持保证金

    @JsonProperty("mgnRatio")
    private String mgnRatio; // 保证金率

    @JsonProperty("mgnMode")
    private String mgnMode; // 保证金模式 cross, isolated

    @JsonProperty("cTime")
    private String cTime; // 创建时间 (毫秒时间戳)

    @JsonProperty("uTime")
    private String uTime; // 更新时间 (毫秒时间戳)

    // ... 可能还有其他字段，如利息、adl 等
}
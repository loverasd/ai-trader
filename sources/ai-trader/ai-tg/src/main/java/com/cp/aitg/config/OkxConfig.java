package com.cp.aitg.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank; // 使用 Jakarta EE 9+ 的 validation

@Configuration
//@ConfigurationProperties(prefix = "okx")
@PropertySource(value = "file:/Users/chenpan/.env/private.cfg", ignoreResourceNotFound = false)
@Data
@Validated // 开启 JSR-303 校验
public class OkxConfig {

    @NotBlank(message = "OKX API Key 不能为空")
    @Value("${okx.api.key}")
    private String apiKey;

    @NotBlank(message = "OKX API Secret 不能为空")
    @Value("${okx.api.secret}")
    private String apiSecret;

    @NotBlank(message = "OKX API Passphrase 不能为空")
    @Value("${okx.api.passphrase}")
    private String apiPassphrase;

    // OKX API V5 基础 URL (可以区分生产和模拟盘)
    private String baseUrl = "https://www.okx.com"; // 生产环境
    // private String baseUrl = "https://aws.okx.com"; // 备用或AWS环境
    // private String baseUrl = "https://www.okx.com"; // Demo盘需要在请求头加 OK-SIMULATED-TRADING

    // 是否启用模拟盘 (用于请求头)
    private boolean simulatedTrading = true; // 默认为 true，用于测试

    // 可以添加其他配置，如超时时间等
    private int connectTimeoutSeconds = 10;
    private int readTimeoutSeconds = 30;
}
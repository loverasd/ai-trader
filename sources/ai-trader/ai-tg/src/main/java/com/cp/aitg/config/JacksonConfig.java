package com.cp.aitg.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 忽略未知属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 注册 JavaTimeModule 以支持 Java 8 时间类型
        mapper.registerModule(new JavaTimeModule());
        
        // 其他通用配置（可选）
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // 日期格式化为字符串
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // 美化输出（仅用于调试）
        
        return mapper;
    }
}
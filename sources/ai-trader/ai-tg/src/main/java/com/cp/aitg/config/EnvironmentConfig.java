package com.cp.aitg.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@Component
public class EnvironmentConfig {

    private static final String CONFIG_FILE_PATH = "/Users/chenpan/.env/private.cfg";

    @PostConstruct
    public void loadEnvVariables() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists()) {
            System.err.println("配置文件不存在: " + CONFIG_FILE_PATH);
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(CONFIG_FILE_PATH))) {
            Properties properties = new Properties();
            properties.load(reader);

            // 遍历文件内容并存入 System 属性
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                System.setProperty(key, value);
            }
            System.out.println("环境变量加载完成！");
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage());
        }
    }
}

package com.cp.aitg.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置文件读取工具类
 */
public class ConfigUtils {

    static final String filePath = "/Users/chenpan/.ssh/bian_private.cfg";

    /**
     * 读取 .cfg 文件并转换为 Map<String, String>
     *
     * @param filePath 配置文件路径
     * @return 包含配置键值对的 Map
     */
    public static Map<String, String> readConfigToMap(String filePath) {
        Map<String, String> configMap = new HashMap<>();
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(filePath)) {
            // 加载配置文件
            properties.load(input);

            // 将 Properties 转换为 Map
            for (String key : properties.stringPropertyNames()) {
                configMap.put(key, properties.getProperty(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configMap;
    }
    public static Map<String, String> readConfigToMap() {
        return readConfigToMap(filePath);
    }

    /**
     * 获取指定配置项的值
     *
     * @param filePath 配置文件路径
     * @param key      配置项的键
     * @return 配置项的值，如果键不存在则返回 null
     */
    public static String getConfigValue(String filePath, String key) {
        Map<String, String> configMap = readConfigToMap(filePath);
        return configMap.get(key);
    }

    /**
     * 测试工具类
     */
    public static void main(String[] args) {
        String filePath = "/Users/chenpan/.ssh/bian_private.cfg";

        // 读取整个配置文件
        Map<String, String> configMap = readConfigToMap(filePath);
        System.out.println("配置文件内容：");
        configMap.forEach((k, v) -> System.out.println(k + " = " + v));

        // 获取指定配置项的值
        String username = getConfigValue(filePath, "username");
        System.out.println("username = " + username);
    }
}
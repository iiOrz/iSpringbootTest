package com.uaes.ocr.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 配置：忽略未知字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 配置日期格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 转换为 Map（最常用）
     */
    public static Map<String, Object> parseToMap(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            throw new RuntimeException("JSON字符串为空");
        }

        try {
            return objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败，原始数据：{}", jsonStr, e);
            throw new RuntimeException("JSON解析失败: " + e.getMessage());
        }
    }

    /**
     * 转换为 Map（安全模式，失败返回空Map）
     */
    public static Map<String, Object> parseToMapSafely(String jsonStr) {
        try {
            return parseToMap(jsonStr);
        } catch (Exception e) {
            log.error("JSON转换失败，返回空Map", e);
            return new HashMap<>();
        }
    }

    /**
     * 转换为 List<Map>
     */
    public static List<Map<String, Object>> parseToList(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            throw new RuntimeException("JSON字符串为空");
        }

        try {
            return objectMapper.readValue(jsonStr, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败，原始数据：{}", jsonStr, e);
            throw new RuntimeException("JSON解析失败: " + e.getMessage());
        }
    }

    /**
     * 转换为 List<Map>（安全模式，失败返回空List）
     */
    public static List<Map<String, Object>> parseToListSafely(String jsonStr) {
        try {
            return parseToList(jsonStr);
        } catch (Exception e) {
            log.error("JSON转换失败，返回空List", e);
            return new ArrayList<>();
        }
    }

    /**
     * 转换为 JsonNode（最灵活，可以任意取值）
     */
    public static JsonNode parseToJsonNode(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            throw new RuntimeException("JSON字符串为空");
        }

        try {
            return objectMapper.readTree(jsonStr);
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败，原始数据：{}", jsonStr, e);
            throw new RuntimeException("JSON解析失败: " + e.getMessage());
        }
    }

    /**
     * 从 JsonNode 中提取值（避免NPE）
     */
    public static String getString(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    public static Integer getInt(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asInt() : null;
    }

    public static Long getLong(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asLong() : null;
    }

    public static Boolean getBoolean(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asBoolean() : null;
    }




    private static int findIndexContaining(List<String> list, String keyword) {
        // 1. 精确匹配
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains(keyword)) {
                return i;
            }
        }

        // 2. 模糊匹配（相似度>50%）
        for (int i = 0; i < list.size(); i++) {
            if (calculateSimilarity(list.get(i), keyword) > 0.5) {
                return i;
            }
        }

        return -1;
    }

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

    public class StringUtils {

        // 第一页的方法
        private static int findIndexContaining(List<String> list, String keyword) {
            // 1. 精确匹配
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).contains(keyword)) {
                    return i;
                }
            }

            // 2. 模糊匹配（相似度>50%）
            for (int i = 0; i < list.size(); i++) {
                if (calculateSimilarity(list.get(i), keyword) > 0.5) {
                    return i;
                }
            }

            return -1;
        }

        // 第二页的方法
        private static double calculateSimilarity(String a, String b) {
            // C# string.IsNullOrEmpty 对应 Java 的 isEmpty() 需要先判空
            if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
                return 0;
            }

            int maxLen = Math.max(a.length(), b.length());
            if (maxLen == 0) return 1.0;

            int sameCount = 0;
            int minLen = Math.min(a.length(), b.length());

            // 计算相同字符数（逐位比较）
            for (int i = 0; i < minLen; i++) {
                if (a.charAt(i) == b.charAt(i)) {
                    sameCount++;
                }
            }

            return (double) sameCount / maxLen;
        }

        // 纯数字检查：^\\d+$
        private static boolean isPureNumber(String text) {
            if (text == null) return false;
            return Pattern.matches("^\\d+$", text);
        }

        // 日期模式检查：^\\d{4}\\.\\d{2}\\.\\d{2}$
        private static boolean isDatePattern(String text) {
            if (text == null) return false;
            return Pattern.matches("^\\d{4}\\.\\d{2}\\.\\d{2}$", text);
        }

        // 有效日期检查
        private static boolean isValidDate(String text) {
            return isDatePattern(text);
        }
    }


}
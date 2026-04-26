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
}
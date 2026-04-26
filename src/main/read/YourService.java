@Service
public class YourService {

    @Autowired
    private OkHttpUtil okHttpUtil;  // 你现有的 OkHttpUtil

    // 示例1：GET请求 - 直接转成Map
    public void testGet() {
        String url = "http://api.example.com/user/1";
        String responseStr = okHttpUtil.get(url);

        // 转成 Map
        Map<String, Object> result = JsonUtil.parseToMap(responseStr);
        System.out.println("code: " + result.get("code"));
        System.out.println("message: " + result.get("message"));

        // 获取嵌套的data
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        System.out.println("用户名: " + data.get("username"));
    }

    // 示例2：文件上传 - 直接取字段值
    public void testUpload(String filePath) {
        String url = "http://api.example.com/upload";
        String responseStr = okHttpUtil.uploadFile(url, filePath, null);

        // 方式1：转成 Map
        Map<String, Object> result = JsonUtil.parseToMap(responseStr);
        if (Integer.valueOf(200).equals(result.get("code"))) {
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String fileUrl = (String) data.get("fileUrl");
            System.out.println("文件URL: " + fileUrl);
        }

        // 方式2：转成 JsonNode（更安全，不需要强转）
        JsonNode rootNode = JsonUtil.parseToJsonNode(responseStr);
        int code = rootNode.path("code").asInt();
        if (code == 200) {
            String fileUrl = rootNode.path("data").path("fileUrl").asText();
            String fileName = rootNode.path("data").path("fileName").asText();
            System.out.println("文件URL: " + fileUrl);
            System.out.println("文件名: " + fileName);
        }
    }

    // 示例3：返回数组 - 转成 List<Map>
    public void testGetList() {
        String url = "http://api.example.com/users";
        String responseStr = okHttpUtil.get(url);

        // 直接转成 List
        List<Map<String, Object>> userList = JsonUtil.parseToList(responseStr);
        for (Map<String, Object> user : userList) {
            System.out.println("ID: " + user.get("id"));
            System.out.println("姓名: " + user.get("username"));
        }
    }

    // 示例4：处理带统一包装的数组
    public void testGetWrapperList() {
        String url = "http://api.example.com/users/list";
        String responseStr = okHttpUtil.get(url);

        // 先转成 Map
        Map<String, Object> result = JsonUtil.parseToMap(responseStr);
        if (Integer.valueOf(200).equals(result.get("code"))) {
            // data 是数组
            List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("data");
            users.forEach(user -> {
                System.out.println(user.get("username"));
            });
        }
    }

    // 示例5：使用 JsonNode 灵活处理复杂结构
    public void testComplexWithJsonNode() {
        String url = "http://api.example.com/complex/data";
        String responseStr = okHttpUtil.get(url);

        JsonNode rootNode = JsonUtil.parseToJsonNode(responseStr);

        // 获取嵌套值
        String username = rootNode.path("data").path("user").path("name").asText();
        int age = rootNode.path("data").path("user").path("age").asInt();

        // 遍历数组
        JsonNode listNode = rootNode.path("data").path("list");
        if (listNode.isArray()) {
            for (JsonNode item : listNode) {
                String id = item.path("id").asText();
                System.out.println("ID: " + id);
            }
        }

        // 判断字段是否存在
        if (rootNode.has("total")) {
            int total = rootNode.path("total").asInt();
            System.out.println("总数: " + total);
        }
    }

    // 示例6：安全模式（不抛异常）
    public void testSafeMode() {
        String url = "http://api.example.com/user/1";
        String responseStr = okHttpUtil.get(url);

        // 转换失败返回空Map，不会抛异常
        Map<String, Object> result = JsonUtil.parseToMapSafely(responseStr);
        if (!result.isEmpty()) {
            System.out.println("转换成功");
        }

        // 转换失败返回空List
        List<Map<String, Object>> list = JsonUtil.parseToListSafely(responseStr);
    }
}
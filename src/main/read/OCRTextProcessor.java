import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class OCRProcessor {

    /**
     * 主提取方法
     */
    public static ExtractedData extractFromOCR(List<String> textList) {
        ExtractedData result = new ExtractedData();

        int batchIndex = findIndexContaining(textList, "批次");
        if (batchIndex > 0) {
            result.CatUniqueCodeOCR = textList.get(batchIndex - 1);
        }
        if (batchIndex >= 0 && batchIndex + 1 < textList.size()) {
            result.CatBatchOCR = textList.get(batchIndex + 1);
        }

        int materialIndex = findIndexContaining(textList, "物料P");
        if (materialIndex >= 0 && materialIndex + 1 < textList.size()) {
            result.CatPartNoOCR = textList.get(materialIndex + 1);
        }

        int qtyIndex = findIndexContaining(textList, "数量QTYP");
        if (qtyIndex > 0 && qtyIndex + 1 < textList.size()) {
            result.QtyOCR = textList.get(qtyIndex + 1);
        }

        int supplierBatchIndex = findIndexContaining(textList, "供应商批次");
        int productDateIndex = findIndexContaining(textList, "Porduct Date");
        int outDateIndex = findIndexContaining(textList, "Expired Date");
        int productionLabelIndex = findIndexContaining(textList, "生产日期");
        int expiredLabelIndex = findIndexContaining(textList, "有效日期");
        int markIndex = findIndexContaining(textList, "标识");

        if (supplierBatchIndex >= 0 && productDateIndex > supplierBatchIndex) {
            for (int i = supplierBatchIndex + 1; i < productDateIndex; i++) {
                if (!textList.get(i).contains("Vendor") &&
                        !textList.get(i).contains("BN")) {
                    result.OriginBatchOCR = textList.get(i);
                    break;
                }
            }
        }

        if (productDateIndex >= 0 && productDateIndex + 1 < textList.size()) {
            result.ProductDateOCR = textList.get(productDateIndex + 1);
        }

        if (outDateIndex >= 0 && outDateIndex + 1 < textList.size()) {
            result.OutDateOCR = textList.get(outDateIndex + 1);
        }

        if (!IsValidDate(result.ProductDateOCR) && productionLabelIndex >= 0
                && expiredLabelIndex > productionLabelIndex) {
            for (int i = productionLabelIndex + 1; i < expiredLabelIndex; i++) {
                if (IsDatePattern(textList.get(i))) {
                    result.ProductDateOCR = textList.get(i);
                    break;
                }
            }
        }

        if (!IsValidDate(result.OutDateOCR) && expiredLabelIndex >= 0
                && markIndex > expiredLabelIndex) {
            for (int i = expiredLabelIndex + 1; i < markIndex; i++) {
                if (IsDatePattern(textList.get(i))) {
                    result.OutDateOCR = textList.get(i);
                    break;
                }
            }
        }

        int remarkIndex = findIndexContaining(textList, "备注");
        if (remarkIndex >= 0 && remarkIndex + 1 < textList.size()) {
            result.OriginPartNoOCR = textList.get(remarkIndex + 1);
        }

        return result;
    }

    /**
     * 查找包含关键词的索引
     */
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

    /**
     * 计算字符串相似度（简单字符匹配率）
     */
    private static double calculateSimilarity(String a, String b) {
        if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
            return 0;
        }

        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;

        int sameCount = 0;
        int minLen = Math.min(a.length(), b.length());

        // 计算相同字符数
        for (int i = 0; i < minLen; i++) {
            if (a.charAt(i) == b.charAt(i)) {
                sameCount++;
            }
        }

        return (double) sameCount / maxLen;
    }

    /**
     * 检查是否为纯数字
     */
    private static boolean isPureNumber(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return Pattern.matches("^\\d+$", text);
    }

    /**
     * 检查是否为日期格式（yyyy.MM.dd）
     */
    private static boolean isDatePattern(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return Pattern.matches("^\\d{4}\\.\\d{2}\\.\\d{2}$", text);
    }

    /**
     * 检查日期是否有效（非空且符合日期格式）
     */
    private static boolean IsValidDate(String text) {
        return isDatePattern(text);
    }
}

/**
 * 数据模型类（需要根据你的实际类调整）
 */
class ExtractedData {
    public String CatUniqueCodeOCR;
    public String CatBatchOCR;
    public String CatPartNoOCR;
    public String QtyOCR;
    public String OriginBatchOCR;
    public String ProductDateOCR;
    public String OutDateOCR;
    public String OriginPartNoOCR;

    // 构造函数
    public ExtractedData() {}
}
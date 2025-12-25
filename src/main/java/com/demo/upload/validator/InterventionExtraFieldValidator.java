package com.demo.upload.validator;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 干预补充字段验证器
 * 负责所有字段的格式验证逻辑
 * 参考 base_importer.py 中的清理方法
 */
@Component
public class InterventionExtraFieldValidator {
    
    /**
     * 判断值是否为空
     */
    private boolean isBlank(Object value) {
        if (value == null) {
            return true;
        }
        String str = String.valueOf(value).trim();
        return str.isEmpty() || str.equals("无") || str.equals("(空)") || str.equals("(跳过)");
    }
    
    /**
     * 清理文本数据
     * 参考 base_importer.py 中的 clean_text 方法
     */
    public String cleanText(Object value) {
        if (isBlank(value)) {
            return "";
        }
        return String.valueOf(value).trim();
    }
    
    /**
     * 清理整数数据
     * 参考 base_importer.py 中的 clean_int 方法
     */
    public Integer cleanInt(Object value) {
        if (isBlank(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            // 尝试从字符串中提取数字
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(String.valueOf(value));
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(0));
                } catch (NumberFormatException ex) {
                    return 0;
                }
            }
            return 0;
        }
    }
    
    /**
     * 清理百分比浮点数
     * 参考 base_importer.py 中的 clean_percent_float 方法
     * 返回 Float 或 null
     */
    public Float cleanPercentFloat(Object value) {
        if (isBlank(value)) {
            return null;
        }
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%?");
        Matcher matcher = pattern.matcher(String.valueOf(value));
        if (matcher.find()) {
            try {
                return Float.parseFloat(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 清理是/否数据，返回字符串
     * 参考 base_importer.py 中的 clean_yes_no 方法
     * 返回 "是" 或 "否"
     */
    public String cleanYesNo(Object value) {
        String str = cleanText(value);
        if (str.isEmpty()) {
            return "";
        }
        if (str.contains("是")) {
            return "是";
        }
        if (str.contains("否") || str.contains("无")) {
            return "否";
        }
        // 匹配英文 yes/no
        Pattern pattern = Pattern.compile("\\b(yes|no|y|n)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase().startsWith("y") ? "是" : "否";
        }
        return "否";
    }
    
    /**
     * 清理浮点数，提取第一个数字
     * 参考 base_importer.py 中的 clean_float_first_number 方法
     * 返回 Float 或 null
     */
    public Float cleanFloatFirstNumber(Object value) {
        if (isBlank(value)) {
            return null;
        }
        Pattern pattern = Pattern.compile("(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(String.valueOf(value));
        if (matcher.find()) {
            try {
                return Float.parseFloat(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}


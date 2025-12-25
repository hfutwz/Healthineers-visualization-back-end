package com.demo.upload.validator;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 患者入室信息字段验证器
 * 负责所有字段的格式验证逻辑
 * 参考 base_importer.py 中的清理方法
 */
@Component
public class PatientInfoOnAdmissionFieldValidator {
    
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
     */
    public String cleanText(Object value) {
        if (isBlank(value)) {
            return "";
        }
        return String.valueOf(value).trim();
    }
    
    /**
     * 清理整数数据
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
     * 清理浮点数数据
     */
    public Float cleanFloat(Object value) {
        if (isBlank(value)) {
            return 0.0f;
        }
        try {
            return Float.parseFloat(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            // 尝试从字符串中提取数字
            Pattern pattern = Pattern.compile("\\d+\\.?\\d*");
            Matcher matcher = pattern.matcher(String.valueOf(value));
            if (matcher.find()) {
                try {
                    return Float.parseFloat(matcher.group(0));
                } catch (NumberFormatException ex) {
                    return 0.0f;
                }
            }
            return 0.0f;
        }
    }
    
    /**
     * 清理体温数据
     * 参考 base_importer.py 中的 clean_temperature_data 方法
     */
    public Float cleanTemperatureData(Object value) {
        if (isBlank(value)) {
            return 0.0f;
        }
        
        String valueStr = String.valueOf(value).trim();
        // 将 @ 替换为 .
        if (valueStr.contains("@")) {
            valueStr = valueStr.replace("@", ".");
        }
        // 如果有逗号，取最后一个部分
        if (valueStr.contains(",")) {
            String[] parts = valueStr.split(",");
            valueStr = parts[parts.length - 1].trim();
        }
        
        // 提取所有数字
        Pattern pattern = Pattern.compile("\\d+\\.?\\d*");
        Matcher matcher = pattern.matcher(valueStr);
        if (matcher.find()) {
            try {
                float cleanedValue = Float.parseFloat(matcher.group(0));
                // 验证温度范围（30.0-45.0）
                if (cleanedValue >= 30.0f && cleanedValue <= 45.0f) {
                    return cleanedValue;
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        return 0.0f;
    }
    
    /**
     * 将Excel中的是/否转换为布尔值
     * 参考 base_importer.py 中的 clean_yes_no_bool 方法
     */
    public Boolean cleanYesNoBool(Object value) {
        String str = cleanText(value);
        if (str.isEmpty()) {
            return false;
        }
        if (str.contains("是")) {
            return true;
        }
        if (str.contains("否") || str.contains("无")) {
            return false;
        }
        // 匹配英文 yes/no
        Pattern pattern = Pattern.compile("\\b(yes|no|y|n)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase().startsWith("y");
        }
        return false;
    }
}


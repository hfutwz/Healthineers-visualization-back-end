package com.demo.upload.validator;

import org.springframework.stereotype.Component;

/**
 * RTS评分字段验证器
 * 负责所有字段的格式验证逻辑
 */
@Component
public class RtsScoreFieldValidator {
    
    /**
     * 验证评分值是否在有效范围内（0-4）
     * 
     * @param score 评分值
     * @param fieldName 字段名称（用于错误提示）
     * @return 验证结果，true表示有效，false表示无效
     */
    public boolean isValidScoreRange(Integer score, String fieldName) {
        if (score == null) {
            return false;
        }
        return score >= 0 && score <= 4;
    }
    
    /**
     * 清理文本数据（去除前后空格）
     * 
     * @param value 原始值
     * @return 清理后的值
     */
    public String cleanText(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().trim();
    }
    
    /**
     * 清理整数数据
     * 将字符串转换为整数，如果转换失败返回0
     * 
     * @param value 原始值
     * @return 清理后的整数值
     */
    public Integer cleanInt(Object value) {
        if (value == null) {
            return 0;
        }
        
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return 0;
        }
        
        try {
            // 尝试解析为整数
            if (str.contains(".")) {
                // 如果是小数，先转换为浮点数再转为整数
                return (int) Double.parseDouble(str);
            }
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}


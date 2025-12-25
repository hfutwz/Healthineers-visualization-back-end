package com.demo.upload.validator;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * GCS评分字段验证器
 * 负责所有字段的格式验证逻辑
 */
@Component
public class GcsScoreFieldValidator {
    
    /**
     * 睁眼评分映射
     */
    private static final Map<String, Integer> EYE_OPENING_MAP = new HashMap<String, Integer>() {{
        put("自动睁眼", 4);
        put("呼唤睁眼", 3);
        put("刺痛睁眼", 2);
        put("无反应", 1);
        put("肿胀不能睁眼", 0);
    }};
    
    /**
     * 言语评分映射
     */
    private static final Map<String, Integer> VERBAL_RESPONSE_MAP = new HashMap<String, Integer>() {{
        put("回答正确", 5);
        put("回答错误", 4);
        put("言语不清", 3);
        put("只能发音", 2);
        put("无反应", 1);
        put("气管插管或切开", 0);
        put("平素言语障碍", 0);
    }};
    
    /**
     * 运动评分映射
     */
    private static final Map<String, Integer> MOTOR_RESPONSE_MAP = new HashMap<String, Integer>() {{
        put("遵嘱", 6);
        put("定位", 5);
        put("逃避", 4);
        put("屈曲", 3);
        put("过伸", 2);
        put("无反应", 1);
        put("瘫痪", 0);
    }};
    
    /**
     * 获取睁眼评分
     */
    public Integer getEyeOpeningScore(String description) {
        if (description == null || description.trim().isEmpty()) {
            return 0;
        }
        return EYE_OPENING_MAP.getOrDefault(description.trim(), 0);
    }
    
    /**
     * 获取言语评分
     */
    public Integer getVerbalResponseScore(String description) {
        if (description == null || description.trim().isEmpty()) {
            return 0;
        }
        return VERBAL_RESPONSE_MAP.getOrDefault(description.trim(), 0);
    }
    
    /**
     * 获取运动评分
     */
    public Integer getMotorResponseScore(String description) {
        if (description == null || description.trim().isEmpty()) {
            return 0;
        }
        return MOTOR_RESPONSE_MAP.getOrDefault(description.trim(), 0);
    }
    
    /**
     * 根据总分判断意识状态
     */
    public String getConsciousnessLevel(Integer totalScore) {
        if (totalScore == null) {
            return "无法评估";
        }
        if (totalScore == 15) {
            return "意识清楚";
        } else if (totalScore >= 12 && totalScore <= 14) {
            return "轻度意识障碍";
        } else if (totalScore >= 9 && totalScore <= 11) {
            return "中度意识障碍";
        } else if (totalScore >= 3 && totalScore <= 8) {
            return "昏迷";
        } else {
            return "无法评估";
        }
    }
    
    /**
     * 清理文本数据
     */
    public String cleanText(Object value) {
        if (value == null) {
            return "";
        }
        String str = String.valueOf(value).trim();
        if (str.isEmpty() || str.equals("无") || str.equals("(空)") || str.equals("(跳过)")) {
            return "";
        }
        return str;
    }
    
    /**
     * 清理整数数据
     */
    public Integer cleanInt(Object value) {
        if (value == null) {
            return 0;
        }
        String str = String.valueOf(value).trim();
        if (str.isEmpty() || str.equals("无") || str.equals("(空)") || str.equals("(跳过)")) {
            return 0;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            // 尝试从字符串中提取数字
            String numbers = str.replaceAll("[^0-9]", "");
            if (!numbers.isEmpty()) {
                try {
                    return Integer.parseInt(numbers);
                } catch (NumberFormatException ex) {
                    return 0;
                }
            }
            return 0;
        }
    }
    
}


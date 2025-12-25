package com.demo.utils;

/**
 * 时间转换工具类
 * 用于处理各种时间格式的转换，将业务逻辑从 Mapper.xml 移到 Service 层
 */
public class TimeConversionUtils {
    
    /**
     * 将时间字符串转换为分钟数（从当天0:00开始计算）
     * 支持多种时间格式：
     * - HH:mm 或 H:mm (如 "23:26" 或 "9:30")
     * - HH:mm:ss 或 H:mm:ss (如 "23:26:00")
     * - HHmm (4位，如 "2326")
     * - Hmm (3位，如 "930")
     * - HHmmss (6位，如 "232600")
     * - HH (1-2位，仅小时，如 "23" 或 "9")
     * 
     * @param timeStr 时间字符串
     * @return 分钟数，如果格式不支持或解析失败返回 null
     */
    public static Integer convertTimeToMinutes(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        
        timeStr = timeStr.trim();
        
        try {
            // 格式1: HH:mm 或 H:mm 或 HH:mm:ss 或 H:mm:ss
            if (timeStr.matches("^[0-9]{1,2}:[0-9]{1,2}(:[0-9]{1,2})?$")) {
                String[] parts = timeStr.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return hour * 60 + minute;
                }
            }
            // 格式2: HHmm (4位) 或 Hmm (3位)
            else if (timeStr.matches("^[0-9]{3,4}$")) {
                int length = timeStr.length();
                int hour = Integer.parseInt(timeStr.substring(0, length - 2));
                int minute = Integer.parseInt(timeStr.substring(length - 2));
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return hour * 60 + minute;
                }
            }
            // 格式3: HHmmss (6位)
            else if (timeStr.matches("^[0-9]{6}$")) {
                int hour = Integer.parseInt(timeStr.substring(0, 2));
                int minute = Integer.parseInt(timeStr.substring(2, 4));
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return hour * 60 + minute;
                }
            }
            // 格式4: 仅小时 (1-2位)
            else if (timeStr.matches("^[0-9]{1,2}$")) {
                int hour = Integer.parseInt(timeStr);
                if (hour >= 0 && hour <= 23) {
                    return hour * 60;
                }
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            // 解析失败，返回 null
            return null;
        }
        
        return null;
    }
    
    /**
     * 将 HH:mm 格式的时间字符串转换为分钟数
     * 这是最常用的格式，单独提供方法以提高性能
     * 
     * @param timeStr HH:mm 格式的时间字符串（如 "23:26"）
     * @return 分钟数，如果格式不正确返回 null
     */
    public static Integer convertHHmmToMinutes(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            String[] parts = timeStr.trim().split(":");
            if (parts.length == 2) {
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return hour * 60 + minute;
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }
        
        return null;
    }
    
    /**
     * 判断时间字符串是否在指定范围内（分钟数）
     * 
     * @param timeStr 时间字符串（支持多种格式）
     * @param startMinutes 开始分钟数（包含）
     * @param endMinutes 结束分钟数（包含）
     * @return true 如果在范围内，false 如果不在范围内或解析失败
     */
    public static boolean isTimeInRange(String timeStr, Integer startMinutes, Integer endMinutes) {
        if (timeStr == null || startMinutes == null || endMinutes == null) {
            return false;
        }
        
        Integer minutes = convertTimeToMinutes(timeStr);
        if (minutes == null) {
            return false;
        }
        
        return minutes >= startMinutes && minutes <= endMinutes;
    }
}


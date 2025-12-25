package com.demo.utils;

import com.demo.entity.InjuryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 时间段计算工具类
 * 根据接诊时间（admissionTime）计算时间段（timePeriod）
 * 
 * <p>时间段定义：
 * <ul>
 *   <li>0：夜间（00:00-07:59）</li>
 *   <li>1：早高峰（08:00-09:59）</li>
 *   <li>2：午高峰（10:00-11:59）</li>
 *   <li>3：下午（12:00-16:59）</li>
 *   <li>4：晚高峰（17:00-19:59）</li>
 *   <li>5：晚上（20:00-23:59）</li>
 * </ul>
 * 
 * @author system
 */
public final class TimePeriodUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(TimePeriodUtils.class);
    
    /**
     * 时间格式正则：4位数字（如：1100表示11:00）
     */
    private static final Pattern TIME_PATTERN = Pattern.compile("^[0-9]{4}$");
    
    /**
     * 私有构造函数，防止实例化
     */
    private TimePeriodUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 批量更新记录的时间段字段
     * 
     * @param records 需要更新的记录列表，不能为null
     */
    public static void updateTimePeriod(List<InjuryRecord> records) {
        if (records == null || records.isEmpty()) {
            logger.debug("记录列表为空，跳过时间段更新");
            return;
        }
        
        int updatedCount = 0;
        for (InjuryRecord record : records) {
            if (record == null) {
                continue;
            }
            
            Integer timePeriod = calculateTimePeriod(record.getAdmissionTime());
            if (timePeriod != null) {
                record.setTimePeriod(timePeriod);
                updatedCount++;
            }
        }
        
        logger.info("时间段更新完成，共更新 {} 条记录，总记录数 {}", updatedCount, records.size());
    }
    
    /**
     * 根据接诊时间计算时间段
     * 
     * @param admissionTime 接诊时间（4位数字格式，如"1100"表示11:00），可以为null
     * @return 时间段（0-5），如果时间无效则返回null
     */
    public static Integer calculateTimePeriod(String admissionTime) {
        if (admissionTime == null || admissionTime.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = admissionTime.trim();
        
        // 验证格式：必须是4位数字
        if (trimmed.length() != 4 || !TIME_PATTERN.matcher(trimmed).matches()) {
            logger.debug("接诊时间格式无效: {}", admissionTime);
            return null;
        }
        
        try {
            // 提取前两位数字（小时）
            int hour = Integer.parseInt(trimmed.substring(0, 2));
            
            // 根据小时计算时间段
            if (hour >= 0 && hour <= 7) {
                return 0;  // 夜间
            } else if (hour == 8 || hour == 9) {
                return 1;  // 早高峰
            } else if (hour == 10 || hour == 11) {
                return 2;  // 午高峰
            } else if (hour >= 12 && hour <= 16) {
                return 3;  // 下午
            } else if (hour >= 17 && hour <= 19) {
                return 4;  // 晚高峰
            } else if (hour >= 20 && hour <= 23) {
                return 5;  // 晚上
            } else {
                logger.warn("接诊时间小时值超出范围: {}", hour);
                return null;  // 无效时间
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            logger.warn("无法解析接诊时间: {}, 错误: {}", admissionTime, e.getMessage());
            return null;
        }
    }
}


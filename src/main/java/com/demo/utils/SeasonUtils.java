package com.demo.utils;

import com.demo.entity.InjuryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * 季节计算工具类
 * 根据接诊日期（admissionDate）计算季节（season）
 * 
 * <p>季节定义：
 * <ul>
 *   <li>0：春季（3-5月）</li>
 *   <li>1：夏季（6-9月）</li>
 *   <li>2：秋季（10-12月）</li>
 *   <li>3：冬季（1-2月）</li>
 * </ul>
 * 
 * @author system
 */
public final class SeasonUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SeasonUtils.class);
    
    /**
     * 私有构造函数，防止实例化
     */
    private SeasonUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 根据接诊日期计算季节
     * 
     * @param admissionDate 接诊日期
     * @return 季节值（0-春季，1-夏季，2-秋季，3-冬季），如果日期为null则返回null
     */
    public static Integer calculateSeason(LocalDate admissionDate) {
        if (admissionDate == null) {
            logger.warn("接诊日期为null，无法计算季节");
            return null;
        }
        
        try {
            int month = admissionDate.getMonthValue();
            
            if (month >= 3 && month <= 5) {
                return 0; // 春季
            } else if (month >= 6 && month <= 9) {
                return 1; // 夏季
            } else if (month >= 10 && month <= 12) {
                return 2; // 秋季
            } else if (month >= 1 && month <= 2) {
                return 3; // 冬季
            } else {
                logger.warn("无效的月份值: {}，无法计算季节", month);
                return null;
            }
        } catch (Exception e) {
            logger.error("计算季节时发生异常，接诊日期: {}", admissionDate, e);
            return null;
        }
    }
    
    /**
     * 批量更新记录列表中的季节字段
     * 
     * @param records 记录列表
     */
    public static void updateSeason(List<InjuryRecord> records) {
        if (records == null || records.isEmpty()) {
            logger.warn("记录列表为空，跳过季节更新");
            return;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (InjuryRecord record : records) {
            if (record == null) {
                failCount++;
                continue;
            }
            
            try {
                Integer season = calculateSeason(record.getAdmissionDate());
                record.setSeason(season);
                
                if (season != null) {
                    successCount++;
                } else {
                    failCount++;
                    logger.debug("记录季节计算失败，患者ID: {}, 接诊日期: {}", 
                        record.getPatientId(), record.getAdmissionDate());
                }
            } catch (Exception e) {
                failCount++;
                logger.warn("更新记录季节时发生异常，患者ID: {}, 接诊日期: {}", 
                    record.getPatientId(), record.getAdmissionDate(), e);
            }
        }
        
        logger.info("季节更新完成，成功: {} 条，失败: {} 条，总计: {} 条", 
            successCount, failCount, records.size());
    }
}


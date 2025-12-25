package com.demo.upload.constants;

/**
 * 创伤病例信息表 Excel 列名常量
 * 对应 injury_record_importer.py 中的列名
 */
public class InjuryRecordColumnConstants {
    
    /**
     * 患者ID列名（序号）
     */
    public static final String PATIENT_ID = "序号";
    
    /**
     * 接诊日期列名
     */
    public static final String ADMISSION_DATE = "接诊日期：";
    
    /**
     * 接诊时间列名
     */
    public static final String ADMISSION_TIME = "接诊时间：";
    
    /**
     * 来院方式列名
     */
    public static final String ARRIVAL_METHOD = "来院方式";
    
    /**
     * 创伤发生地列名
     */
    public static final String INJURY_LOCATION = "(2)    创伤发生地：___（小区名，工厂名，商场名。如果是交通事故填写XX路上靠近XX路，或者XX路和XX路交叉口）";
    
    /**
     * 120分站站点名称列名
     */
    public static final String STATION_NAME = "(1)120分站站点名称：___";
    
    /**
     * 受伤原因列名
     */
    public static final String INJURY_CAUSE = "受伤原因:";
    
    /**
     * 所有必需的列名数组
     */
    public static final String[] REQUIRED_COLUMNS = {
        PATIENT_ID,
        ADMISSION_DATE,
        ADMISSION_TIME
    };
    
    private InjuryRecordColumnConstants() {
        // 工具类，禁止实例化
    }
}


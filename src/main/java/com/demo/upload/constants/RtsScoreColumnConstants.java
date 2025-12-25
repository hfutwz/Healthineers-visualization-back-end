package com.demo.upload.constants;

/**
 * RTS评分表 Excel 列名常量
 * 对应 rts_score_importer.py 中的列名
 */
public class RtsScoreColumnConstants {
    
    /**
     * 患者ID列名（序号）
     */
    public static final String PATIENT_ID = "序号";
    
    /**
     * RTS评分—GCS列名
     */
    public static final String GCS_SCORE = "RTS评分—GCS";
    
    /**
     * 收缩压列名
     */
    public static final String SBP_SCORE = "收缩压";
    
    /**
     * 呼吸频率列名
     */
    public static final String RR_SCORE = "呼吸频率";
    
    /**
     * 所有必需的列名数组
     */
    public static final String[] REQUIRED_COLUMNS = {
        PATIENT_ID
    };
    
    private RtsScoreColumnConstants() {
        // 工具类，禁止实例化
    }
}


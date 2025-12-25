package com.demo.upload.constants;

/**
 * GCS评分表 Excel 列名常量
 * 对应 gcs_score_importer.py 中的列名
 */
public class GcsScoreColumnConstants {
    
    /**
     * 患者ID列名（序号）
     */
    public static final String PATIENT_ID = "序号";
    
    /**
     * GCS评分：睁眼列名
     */
    public static final String EYE_OPENING = "GCS评分：睁眼";
    
    /**
     * GCS评分：言语列名
     */
    public static final String VERBAL_RESPONSE = "GCS评分：言语";
    
    /**
     * GCS评分：动作列名
     */
    public static final String MOTOR_RESPONSE = "GCS评分：动作";
    
    /**
     * GCS总分列名
     */
    public static final String TOTAL_SCORE = "GCS总分：";
    
    /**
     * 所有必需的列名数组
     */
    public static final String[] REQUIRED_COLUMNS = {
        PATIENT_ID
    };
    
    private GcsScoreColumnConstants() {
        // 工具类，禁止实例化
    }
}


package com.demo.upload.constants;

/**
 * ISS表 Excel 列名常量
 * 对应 iss_importer.py 中的列名
 */
public class IssColumnConstants {
    
    /**
     * 患者ID列名（序号）
     */
    public static final String PATIENT_ID = "序号";
    
    /**
     * 头颈部伤情等级列名
     */
    public static final String HEAD_NECK = "ISS评分矩阵—头颈部";
    
    /**
     * 面部伤情等级列名
     */
    public static final String FACE = "面部";
    
    /**
     * 胸部伤情等级列名
     */
    public static final String CHEST = "胸部";
    
    /**
     * 腹部伤情等级列名
     */
    public static final String ABDOMEN = "腹部";
    
    /**
     * 四肢伤情等级列名
     */
    public static final String LIMBS = "四肢";
    
    /**
     * 体表伤情等级列名
     */
    public static final String BODY = "体表";
    
    /**
     * ISS评分列名
     */
    public static final String ISS_SCORE = "ISS评分：";
    
    /**
     * 所有必需的列名数组
     */
    public static final String[] REQUIRED_COLUMNS = {
        PATIENT_ID
    };
    
    private IssColumnConstants() {
        // 工具类，禁止实例化
    }
}


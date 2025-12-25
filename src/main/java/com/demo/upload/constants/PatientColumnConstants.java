package com.demo.upload.constants;

/**
 * 患者信息表 Excel 列名常量
 * 对应 patient_importer.py 中的 COLUMN_MAPPING
 */
public class PatientColumnConstants {
    
    /**
     * 患者ID列名（序号）
     */
    public static final String PATIENT_ID = "序号";
    
    /**
     * 性别列名
     */
    public static final String GENDER = "患者性别：";
    
    /**
     * 年龄列名
     */
    public static final String AGE = "年龄：       ";
    
    /**
     * 是否绿色通道列名
     */
    public static final String IS_GREEN_CHANNEL = "是否绿色通道";
    
    /**
     * 身高列名
     */
    public static final String HEIGHT = "(1)身高：___";
    
    /**
     * 体重列名
     */
    public static final String WEIGHT = "(2)cm    体重：___kg";
    
    /**
     * 姓名列名
     */
    public static final String NAME = "姓名";
    
    /**
     * 所有必需的列名数组
     */
    public static final String[] REQUIRED_COLUMNS = {
        PATIENT_ID,
        GENDER,
        AGE,
        IS_GREEN_CHANNEL,
        HEIGHT,
        WEIGHT
    };
    
    private PatientColumnConstants() {
        // 工具类，禁止实例化
    }
}


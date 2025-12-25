package com.demo.upload.constants;

/**
 * 患者入室信息表 Excel 列名常量
 * 对应 patient_info_on_admission_importer.py 中的列名
 */
public class PatientInfoOnAdmissionColumnConstants {
    
    /**
     * 患者ID列名（序号）
     */
    public static final String PATIENT_ID = "序号";
    
    /**
     * 血压高压列名
     */
    public static final String SYSTOLIC_BP = "(1)血压：___";
    
    /**
     * 血压低压列名
     */
    public static final String DIASTOLIC_BP = "(2)/___mmHg";
    
    /**
     * 脉搏心率列名
     */
    public static final String HEART_RATE = "脉搏心率：              bpm";
    
    /**
     * 呼吸频率列名
     */
    public static final String RESPIRATORY_RATE = "呼吸频率：                   次/分";
    
    /**
     * 既往病史列名
     */
    public static final String MEDICAL_HISTORY = "既往病史：";
    
    /**
     * 入室体温列名
     */
    public static final String TEMPERATURE = "入室体温：             ℃";
    
    /**
     * 指脉氧列名
     */
    public static final String OXYGEN_SATURATION = "指脉氧：                       %";
    
    /**
     * 精神意识列名
     */
    public static final String CONSCIOUSNESS = "精神意识:";
    
    /**
     * 皮肤列名
     */
    public static final String SKIN = "皮肤:";
    
    /**
     * 醉酒列名
     */
    public static final String DRUNK = "醉酒:";
    
    /**
     * 瞳孔列名
     */
    public static final String PUPIL = "瞳孔:";
    
    /**
     * 对光反射列名
     */
    public static final String LIGHT_REFLEX = "对光反射:";
    
    /**
     * 所有必需的列名数组
     */
    public static final String[] REQUIRED_COLUMNS = {
        PATIENT_ID
    };
    
    private PatientInfoOnAdmissionColumnConstants() {
        // 工具类，禁止实例化
    }
}


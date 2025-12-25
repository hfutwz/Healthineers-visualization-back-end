package com.demo.upload.constants;

/**
 * 患者离室信息表 Excel 列名常量
 * 对应 patient_info_off_admission_importer.py 中的列名
 */
public class PatientInfoOffAdmissionColumnConstants {
    
    /**
     * 患者ID列名（序号）
     */
    public static final String PATIENT_ID = "序号";
    
    /**
     * 离开抢救室生命体征：体温列名
     */
    public static final String TEMPERATURE = "(1)离开抢救室生命体征：体温：___";
    
    /**
     * 呼吸列名
     */
    public static final String RESPIRATORY_RATE = "(2)℃呼吸：___";
    
    /**
     * 心率列名
     */
    public static final String HEART_RATE = "(3)次/分心率：___";
    
    /**
     * 血压高压列名
     */
    public static final String SYSTOLIC_BP = "(4)bpm血压：___";
    
    /**
     * 血压低压列名
     */
    public static final String DIASTOLIC_BP = "(5)/___";
    
    /**
     * 指脉氧列名
     */
    public static final String OXYGEN_SATURATION = "(6)mmHg指脉氧：___%";
    
    /**
     * 总补液量列名
     */
    public static final String TOTAL_FLUID_VOLUME = "(1)总补液量：___";
    
    /**
     * 生理盐水列名
     */
    public static final String SALINE_SOLUTION = "(2)ml         其中:  生理盐水：___";
    
    /**
     * 平衡液列名
     */
    public static final String BALANCED_SOLUTION = "(3)ml               平衡液：___";
    
    /**
     * 人工胶体列名
     */
    public static final String ARTIFICIAL_COLLOID = "(4)ml               人工胶体：___";
    
    /**
     * 其他补液列名
     */
    public static final String OTHER_FLUID = "(5)ml     其他：___";
    
    /**
     * 尿量列名
     */
    public static final String URINE_OUTPUT = "(1)尿量：___";
    
    /**
     * 其他引流量列名
     */
    public static final String OTHER_DRAINAGE = "(2)ml    其他引流量：___";
    
    /**
     * 出血量列名
     */
    public static final String BLOOD_LOSS = "(3)ml出血量：___ml";
    
    /**
     * 所有必需的列名数组
     */
    public static final String[] REQUIRED_COLUMNS = {
        PATIENT_ID
    };
    
    private PatientInfoOffAdmissionColumnConstants() {
        // 工具类，禁止实例化
    }
}


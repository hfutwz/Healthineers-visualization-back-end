package com.demo.upload.constants;

/**
 * 干预时间表 Excel 列名常量
 * 对应 intervention_time_importer.py 中的列名
 */
public class InterventionTimeColumnConstants {
    
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
     * 外周列名
     */
    public static final String PERIPHERAL = "外周:";
    
    /**
     * 深静脉列名
     */
    public static final String IV_LINE = "深静脉:";
    
    /**
     * 骨通道列名
     */
    public static final String CENTRAL_ACCESS = "骨通道:";
    
    /**
     * 鼻导管列名
     */
    public static final String NASAL_PIPE = "鼻导管:";
    
    /**
     * 面罩列名
     */
    public static final String FACE_MASK = "面罩:";
    
    /**
     * 气管插管列名
     */
    public static final String ENDOTRACHEAL_TUBE = "气管插管:";
    
    /**
     * 呼吸机列名
     */
    public static final String VENTILATOR = "呼吸机:";
    
    /**
     * 心肺复苏列名
     */
    public static final String CPR = "心肺复苏:";
    
    /**
     * 心肺复苏开始时间列名
     */
    public static final String CPR_START_TIME = "开始时间：";
    
    /**
     * 心肺复苏结束时间列名
     */
    public static final String CPR_END_TIME = "结束时间：";
    
    /**
     * B超列名
     */
    public static final String ULTRASOUND = "B超：";
    
    /**
     * CT列名
     */
    public static final String CT = "CT:";
    
    /**
     * 止血带列名
     */
    public static final String TOURNIQUET = "止血带:";
    
    /**
     * 采血列名
     */
    public static final String BLOOD_DRAW = "采血:";
    
    /**
     * 导尿列名
     */
    public static final String CATHETER = "导尿:";
    
    /**
     * 胃管列名
     */
    public static final String GASTRIC_TUBE = "胃管：";
    
    /**
     * 输血列名
     */
    public static final String TRANSFUSION = "输血:";
    
    /**
     * 输血开始时间列名
     */
    public static final String TRANSFUSION_START = "输血开始：";
    
    /**
     * 输血结束时间列名
     */
    public static final String TRANSFUSION_END = "输血结束：";
    
    /**
     * 离开抢救室时间列名
     */
    public static final String LEAVE_SURGERY_TIME = "离开抢救室时间：";
    
    /**
     * 病人去向列名
     */
    public static final String PATIENT_DESTINATION = "病人去向:";
    
    /**
     * 死亡列名
     */
    public static final String DEATH = "死亡:";
    
    /**
     * 死亡日期列名
     */
    public static final String DEATH_DATE = "死亡日期：";
    
    /**
     * 死亡时间列名
     */
    public static final String DEATH_TIME = "死亡时间：";
    
    /**
     * 所有必需的列名数组
     */
    public static final String[] REQUIRED_COLUMNS = {
        PATIENT_ID,
        ADMISSION_DATE,
        ADMISSION_TIME
    };
    
    private InterventionTimeColumnConstants() {
        // 工具类，禁止实例化
    }
}


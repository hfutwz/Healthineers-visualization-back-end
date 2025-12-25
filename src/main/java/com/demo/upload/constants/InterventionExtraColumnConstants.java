package com.demo.upload.constants;

/**
 * 干预补充表 Excel 列名常量
 * 对应 intervention_extra_importer.py 中的列名
 */
public class InterventionExtraColumnConstants {
    
    /**
     * 患者ID列名（序号）
     */
    public static final String PATIENT_ID = "序号";
    
    /**
     * 氧浓度列名
     */
    public static final String OXYGEN_CONCENTRATION = "(1)氧浓度：___ %   （最低）";
    
    /**
     * 除颤列名
     */
    public static final String DEFIBRILLATION = "除颤:";
    
    /**
     * 肢体离断列名
     */
    public static final String LIMB_AMPUTATION = "肢体离断:";
    
    /**
     * 输血反应列名
     */
    public static final String TRANSFUSION_REACTION = "输血反应:";
    
    /**
     * 悬红列名
     */
    public static final String SUSPENDED_RED_UNITS = "(1)悬红：___";
    
    /**
     * 血浆列名
     */
    public static final String PLASMA_UNITS = "(2) U       血浆：___";
    
    /**
     * 血小板列名
     */
    public static final String PLATELETS_AMOUNT = "(3)ml血小板：___";
    
    /**
     * 冷沉淀列名
     */
    public static final String CRYOPRECIPITATE_UNITS = "(4)U      冷沉淀：___";
    
    /**
     * 其他列名
     */
    public static final String OTHER_TRANSFUSION = "(5)U其他：___";
    
    /**
     * 治疗性操作列名
     */
    public static final String THERAPEUTIC_OPERATION = "治疗性操作：";
    
    /**
     * 会诊科室列名
     */
    public static final String CONSULTATION_DEPT = "会诊科室：";
    
    /**
     * 行政科室列名
     */
    public static final String ADMINISTRATIVE_DEPT = "行政科室：";
    
    /**
     * 所有必需的列名数组
     */
    public static final String[] REQUIRED_COLUMNS = {
        PATIENT_ID
    };
    
    private InterventionExtraColumnConstants() {
        // 工具类，禁止实例化
    }
}


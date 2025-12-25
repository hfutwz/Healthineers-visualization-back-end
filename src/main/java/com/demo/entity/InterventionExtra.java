package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 干预过程补充表实体类
 * 对应数据库表：intervention_extra
 */
@TableName("intervention_extra")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class InterventionExtra implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 患者ID（外键）
     */
    private Integer patientId;
    
    /**
     * 氧浓度（百分比）
     */
    private Float oxygenConcentration;
    
    /**
     * 除颤（1是/0否）
     */
    private String defibrillation;
    
    /**
     * 肢体离断（1是/0否）
     */
    private String limbAmputation;
    
    /**
     * 输血反应
     */
    private String transfusionReaction;
    
    /**
     * 悬红
     */
    private Float suspendedRedUnits;
    
    /**
     * 血浆
     */
    private Float plasmaUnits;
    
    /**
     * 血小板
     */
    private Float plateletsAmount;
    
    /**
     * 冷沉淀
     */
    private Float cryoprecipitateUnits;
    
    /**
     * 其他
     */
    private String otherTransfusion;
    
    /**
     * 治疗性操作
     */
    private String therapeuticOperation;
    
    /**
     * 会诊科室
     */
    private String consultationDept;
    
    /**
     * 行政科室
     */
    private String administrativeDept;
}


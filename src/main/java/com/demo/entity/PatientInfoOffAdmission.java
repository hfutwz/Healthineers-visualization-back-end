package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 患者离室后信息表实体类
 * 对应数据库表：patient_info_off_admission
 */
@TableName("patient_info_off_admission")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PatientInfoOffAdmission implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 患者ID
     */
    private Integer patientId;
    
    /**
     * 体温
     */
    private Float temperature;
    
    /**
     * 呼吸频率
     */
    private Integer respiratoryRate;
    
    /**
     * 心率
     */
    private Integer heartRate;
    
    /**
     * 血压高压
     */
    private Integer systolicBp;
    
    /**
     * 血压低压
     */
    private Integer diastolicBp;
    
    /**
     * 指脉氧
     */
    private Float oxygenSaturation;
    
    /**
     * 总补液量
     */
    private Float totalFluidVolume;
    
    /**
     * 生理盐水
     */
    private Float salineSolution;
    
    /**
     * 平衡液
     */
    private Float balancedSolution;
    
    /**
     * 人工胶体
     */
    private Float artificialColloid;
    
    /**
     * 其他补液
     */
    private String otherFluid;
    
    /**
     * 尿量
     */
    private Float urineOutput;
    
    /**
     * 其他引流量
     */
    private Float otherDrainage;
    
    /**
     * 出血量
     */
    private String bloodLoss;
}

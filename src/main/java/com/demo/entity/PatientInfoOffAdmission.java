package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
/**
 * 建表语句
 * CREATE TABLE patient_info_off_admission (
 *     id INT AUTO_INCREMENT PRIMARY KEY,
 *     patient_id INT NOT NULL,
 *     temperature FLOAT COMMENT '体温',
 *     respiratory_rate INT COMMENT '呼吸频率',
 *     heart_rate INT COMMENT '心率',
 *     systolic_bp INT COMMENT '血压高压',
 *     diastolic_bp INT COMMENT '血压低压',
 *     oxygen_saturation FLOAT COMMENT '指脉氧',
 *     total_fluid_volume FLOAT COMMENT '总补液量',
 *     saline_solution FLOAT COMMENT '生理盐水',
 *     balanced_solution FLOAT COMMENT '平衡液',
 *     artificial_colloid FLOAT COMMENT '人工胶体',
 *     other_fluid varchar(255) COMMENT '其他补液',
 *     urine_output FLOAT COMMENT '尿量',
 *     other_drainage FLOAT COMMENT '其他引流量',
 *     blood_loss varchar(255) COMMENT '出血量',
 *     FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE CASCADE
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者离开时信息表';
 */

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

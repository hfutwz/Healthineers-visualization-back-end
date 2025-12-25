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
 * CREATE TABLE patient_info_on_admission (
 *     id INT AUTO_INCREMENT PRIMARY KEY,
 *     patient_id INT NOT NULL,
 *     systolic_bp INT DEFAULT 0 COMMENT '血压高压',
 *     diastolic_bp INT DEFAULT 0 COMMENT '血压低压',
 *     heart_rate INT DEFAULT 0 COMMENT '心率',
 *     respiratory_rate INT DEFAULT 0 COMMENT '呼吸频率',
 *     medical_history VARCHAR(500) DEFAULT '' COMMENT '既往病史',
 *     temperature FLOAT DEFAULT 0.0 COMMENT '入室温度',
 *     oxygen_saturation INT DEFAULT 0 COMMENT '指脉氧',
 *     consciousness VARCHAR(100) DEFAULT '' COMMENT '精神意识',
 *     skin VARCHAR(100) DEFAULT '' COMMENT '皮肤',
 *     drunk BOOLEAN DEFAULT FALSE COMMENT '醉酒',
 *     pupil VARCHAR(100) DEFAULT '' COMMENT '瞳孔',
 *     light_reflex VARCHAR(100) DEFAULT '' COMMENT '对光反射',
 *     FOREIGN KEY (patient_id) REFERENCES patient(patient_id)
 * );
 */

/**
 * 患者入室前信息表实体类
 * 对应数据库表：patient_info_on_admission
 */
@TableName("patient_info_on_admission")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PatientInfoOnAdmission implements Serializable {
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
     * 血压高压
     */
    private Integer systolicBp;
    
    /**
     * 血压低压
     */
    private Integer diastolicBp;
    
    /**
     * 心率
     */
    private Integer heartRate;
    
    /**
     * 呼吸频率
     */
    private Integer respiratoryRate;
    
    /**
     * 既往病史
     */
    private String medicalHistory;
    
    /**
     * 入室温度
     */
    private Float temperature;
    
    /**
     * 指脉氧
     */
    private Integer oxygenSaturation;
    
    /**
     * 精神意识
     */
    private String consciousness;
    
    /**
     * 皮肤
     */
    private String skin;
    
    /**
     * 醉酒
     */
    private Boolean drunk;
    
    /**
     * 瞳孔
     */
    private String pupil;
    
    /**
     * 对光反射
     */
    private String lightReflex;
}

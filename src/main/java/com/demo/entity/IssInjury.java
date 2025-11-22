package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 建表语句：
 * CREATE TABLE ISS_Patient_Injury_Severity (
 *     injury_id INT PRIMARY KEY AUTO_INCREMENT, -- 创伤ID，自增主键
 *     patient_id INT NOT NULL, -- 患者ID
 *     head_neck INT CHECK (head_neck BETWEEN 0 AND 6), -- 头颈部伤情等级
 *     face INT CHECK (face BETWEEN 0 AND 6), -- 面部伤情等级
 *     chest INT CHECK (chest BETWEEN 0 AND 6), -- 胸部伤情等级
 *     abdomen INT CHECK (abdomen BETWEEN 0 AND 6), -- 腹部伤情等级
 *     limbs INT CHECK (limbs BETWEEN 0 AND 6), -- 四肢伤情等级
 *     body INT CHECK (body BETWEEN 0 AND 6), -- 体表伤情等级
 *     iss_score INT, -- ISS评分
 *     CONSTRAINT fk_iss_patient FOREIGN KEY (patient_id) REFERENCES Patient(patient_id) -- 假设患者表为Patients，外键关联
 * );
 */
@TableName("iss_patient_injury_severity")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class IssInjury implements Serializable {
    /**
     * 创伤ID，自增主键
     */
    @TableId(value = "injury_id", type = IdType.AUTO)
    private Integer injuryId;
    /**
     * 头颈部伤情等级 0-6, 0表示无伤情，1-6表示不同程度
     */
    private Integer headNeck;
    /**
     * 脸部
     */
    private Integer face;
    /**
     * 胸部
     */
    private Integer chest;
    /**
     * 腹部
     */
    private Integer abdomen;
    /**
     * 四肢
     */
    private Integer limbs;
    /**
     * 体表
     */
    private Integer body;
    /**
     * ISS评分
     */
    private Integer issScore; // ISS评分
}

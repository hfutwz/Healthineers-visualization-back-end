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
 CREATE TABLE iss_patient_injury_severity (
 injury_id INT PRIMARY KEY AUTO_INCREMENT,
 patient_id INT NOT NULL,
 head_neck VARCHAR(20) COMMENT '头颈部伤情等级，支持"2|3"格式',
 face VARCHAR(20) COMMENT '面部伤情等级，支持"2|3"格式',
 chest VARCHAR(20) COMMENT '胸部伤情等级，支持"2|3"格式',
 abdomen VARCHAR(20) COMMENT '腹部伤情等级，支持"2|3"格式',
 limbs VARCHAR(20) COMMENT '四肢伤情等级，支持"2|3"格式',
 body VARCHAR(20) COMMENT '体表伤情等级，支持"2|3"格式',
 iss_score INT COMMENT 'ISS评分',
 head_neck_details TEXT COMMENT '头颈部详细伤情',
 face_details TEXT COMMENT '面部详细伤情',
 chest_details TEXT COMMENT '胸部详细伤情',
 abdomen_details TEXT COMMENT '腹部详细伤情',
 limbs_details TEXT COMMENT '四肢详细伤情',
 body_details TEXT COMMENT '体表详细伤情',
 has_details BOOLEAN DEFAULT FALSE COMMENT '是否有详细伤情信息',
 CONSTRAINT fk_iss_patient FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)
 ) COMMENT='创伤等级ISS表';

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
     * 患者ID
     */
    private Integer patientId;
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
    
    /**
     * 头颈部详细伤情
     */
    private String headNeckDetails;
    
    /**
     * 面部详细伤情
     */
    private String faceDetails;
    
    /**
     * 胸部详细伤情
     */
    private String chestDetails;
    
    /**
     * 腹部详细伤情
     */
    private String abdomenDetails;
    
    /**
     * 四肢详细伤情
     */
    private String limbsDetails;
    
    /**
     * 体表详细伤情
     */
    private String bodyDetails;
    
    /**
     * 是否有详细伤情信息
     */
    private Boolean hasDetails;
}

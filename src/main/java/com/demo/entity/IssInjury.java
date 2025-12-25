package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
     * 头颈部伤情等级，支持"2|3"格式，单个数字或"1┋3┋4"格式（会被转换为"1|3|4"）
     */
    @TableField("head_neck")
    private String headNeck;
    /**
     * 面部伤情等级，支持"2|3"格式
     */
    private String face;
    /**
     * 胸部伤情等级，支持"2|3"格式
     */
    private String chest;
    /**
     * 腹部伤情等级，支持"2|3"格式
     */
    private String abdomen;
    /**
     * 四肢伤情等级，支持"2|3"格式
     */
    private String limbs;
    /**
     * 体表伤情等级，支持"2|3"格式
     */
    private String body;
    /**
     * ISS评分
     */
    @TableField("iss_score")
    private Integer issScore;
    
    /**
     * 头颈部详细伤情
     */
    @TableField("head_neck_details")
    private String headNeckDetails;
    
    /**
     * 面部详细伤情
     */
    @TableField("face_details")
    private String faceDetails;
    
    /**
     * 胸部详细伤情
     */
    @TableField("chest_details")
    private String chestDetails;
    
    /**
     * 腹部详细伤情
     */
    @TableField("abdomen_details")
    private String abdomenDetails;
    
    /**
     * 四肢详细伤情
     */
    @TableField("limbs_details")
    private String limbsDetails;
    
    /**
     * 体表详细伤情
     */
    @TableField("body_details")
    private String bodyDetails;
    
    /**
     * 是否有详细伤情信息
     */
    @TableField("has_details")
    private Boolean hasDetails;
}

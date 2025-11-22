package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 建表语句
 * CREATE TABLE InjuryRecord (
 *     injury_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '病例ID',
 *     patient_id INT NOT NULL COMMENT '患者ID',
 *     admission_date DATE NOT NULL COMMENT '接诊日期',
 *     season TINYINT DEFAULT NULL COMMENT '季节',
 *     admission_time VARCHAR(4) NOT NULL COMMENT '接诊时间',
 *     time_period TINYINT DEFAULT NULL COMMENT '时间段',
 *     arrival_method VARCHAR(50) NOT NULL COMMENT '来院方式',
 *     injury_location_desc VARCHAR(255) NOT NULL COMMENT '创伤发生地点描述',
 *     longitude DECIMAL(9,6) DEFAULT NULL COMMENT '经度',
 *     latitude DECIMAL(9,6) DEFAULT NULL COMMENT '纬度',
 *     -- 添加索引
 *     INDEX idx_log_lat_time (longitude, latitude, time_period),
 *     INDEX idx_log_lat_season (longitude, latitude, season),
 *     -- 添加外键约束
 *     CONSTRAINT fk_patient FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)
 * ) COMMENT='创伤病例详细信息';
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("injuryrecord")
public class InjuryRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 病例ID
     */
    @TableId(value = "injury_id", type = IdType.AUTO)
    private Integer injuryId;
    /**
     * 患者ID 外键 关联Patient表
     */
    private Integer patientId;
    /**
     * 接诊日期 2024-10-29
     */
    private LocalDate admissionDate;
    /**
     * 季节（0：春，1：夏，2：秋，3：冬）
     */
    private Integer season;
    /**
     * 接诊时间（hhmm格式，例如：1100）
     */
    private String admissionTime;
    /**
     * 时间段
     * 0：夜间 19:00-7:00
     * 1：早高峰 7:00-9:00
     * 2：早晨  9:00-11:00
     * 3：午高峰 11:00-13:00
     * 4：下午  13:00-17:00
     * 5：晚高峰 17:00-19:00
     */
    private Integer timePeriod;
    /**
     * 来院方式
     */
    private String arrivalMethod;
    /**
     * 创伤发生地点描述
     */
    private String injuryLocationDesc;
    /**
     * 经度
     */
    private Double longitude;
    /**
     * 纬度
     */
    private Double latitude;

}
/**
 * 建表语句
 * CREATE TABLE InterventionTime (
 *     intervention_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '干预方式ID',
 *     patient_id INT NOT NULL COMMENT '患者ID,外键',
 *     admission_date DATE NOT NULL COMMENT '接诊日期',
 *     admission_time VARCHAR(4) NOT NULL COMMENT '接诊时间',
 *     -- 各干预方式的时间点，存储为4位字符串（HHMM），为空表示无此干预
 *     peripheral VARCHAR(4) DEFAULT NULL COMMENT '外周',
 *     iv_line VARCHAR(4) DEFAULT NULL COMMENT '深静脉',
 *     central_access VARCHAR(4) DEFAULT NULL COMMENT '骨通道',
 *     nasal_pipe VARCHAR(4) DEFAULT NULL COMMENT '鼻导管',
 *     face_mask VARCHAR(4) DEFAULT NULL COMMENT '面罩',
 *     endotracheal_tube VARCHAR(4) DEFAULT NULL COMMENT '气管插管',
 *     ventilator VARCHAR(4) DEFAULT NULL COMMENT '呼吸机开始时间',
 *     cpr_start_time VARCHAR(4) DEFAULT NULL COMMENT '心肺复苏开始时间',
 *     cpr_end_time VARCHAR(4) DEFAULT NULL COMMENT '心肺复苏结束时间',
 *     ultrasound VARCHAR(4) DEFAULT NULL COMMENT 'B超',
 *     CT VARCHAR(4) DEFAULT NULL COMMENT 'CT',
 *     tourniquet VARCHAR(4) DEFAULT NULL COMMENT '止血带',
 *     blood_draw VARCHAR(4) DEFAULT NULL COMMENT '采血',
 *     catheter VARCHAR(4) DEFAULT NULL COMMENT '导尿',
 *     gastric_tube VARCHAR(4) DEFAULT NULL COMMENT '胃管',
 *     transfusion_start VARCHAR(4) DEFAULT NULL COMMENT '输血开始时间',
 *     transfusion_end VARCHAR(4) DEFAULT NULL COMMENT '输血结束时间',
 *     leave_surgery_time VARCHAR(4) DEFAULT NULL COMMENT '离开抢救室时间',
 *     CONSTRAINT fk_intervention_patient FOREIGN KEY (patient_id) REFERENCES Patient(patient_id) -- 假设患者表为Patients，外键关联
 *     )COMMENT='干预方式时间表';
 */
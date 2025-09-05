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
     * 0：门诊 1：住院
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

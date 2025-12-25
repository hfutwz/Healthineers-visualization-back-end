package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 建表语句
 CREATE TABLE InjuryRecord (
 injury_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '病例id（主键）',
 patient_id INT NOT NULL COMMENT '患者id（外键）',
 admission_date DATE COMMENT '接诊日期',
 season TINYINT COMMENT '季节（0-春季，1-夏季，2-秋季，3-冬季）',
 admission_time VARCHAR(4) COMMENT '接诊时间（四位数字格式，如1100）',
 time_period TINYINT COMMENT '时间段（0夜间0-7，1早高峰8-9，2午高峰10-11，3下午12-17，4晚高峰17-19，5晚上20-23',
 arrival_method VARCHAR(100) COMMENT '来院方式',
 injury_location VARCHAR(500) COMMENT '创伤发生地',
 longitude DECIMAL(10, 7) COMMENT '经度',
 latitude DECIMAL(10, 7) COMMENT '纬度',
 station_name VARCHAR(200) COMMENT '120分站站点名称',
 injury_cause_category TINYINT COMMENT '受伤原因分类（0-交通伤，1-高坠伤，2-机械伤，3-跌倒，4-其他）',
 injury_cause_detail VARCHAR(200) COMMENT '受伤原因具体描述（如：爆炸伤、玻璃划伤等）',
 FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
 INDEX idx_injury_cause_category (injury_cause_category),
 INDEX idx_season (season),
 INDEX idx_time_period (time_period)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病例发生记录表';

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
     * 0：夜间      00：00-07:59
     * 1：早高峰    8:00-9:59
     * 2：午高峰    10:00-11:59
     * 3：下午      12:00-16:59
     * 4：晚高峰    17:00-19:59
     * 5：晚上     20:00-23:59
     */
    private Integer timePeriod;
    /**
     * 来院方式
     */
    private String arrivalMethod;
    /**
     * 创伤发生地点描述
     */
    @TableField("injury_location")
    private String injuryLocationDesc;
    /**
     * 经度
     */
    private Double longitude;
    /**
     * 纬度
     */
    private Double latitude;
    /**
     * 120分站站点名称
     */
    @TableField("station_name")
    private String stationName;
    /**
     * 受伤原因分类（0-交通伤，1-高坠伤，2-机械伤，3-跌倒，4-其他）
     */
    @TableField("injury_cause_category")
    private Integer injuryCauseCategory;
    /**
     * 受伤原因具体描述（如：爆炸伤、玻璃划伤等）
     */
    @TableField("injury_cause_detail")
    private String injuryCauseDetail;

}
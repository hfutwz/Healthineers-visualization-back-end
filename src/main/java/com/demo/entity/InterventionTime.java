package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 建表语句
 CREATE TABLE interventiontime (
 intervention_id int AUTO_INCREMENT COMMENT '干预方式ID' PRIMARY KEY,
 patient_id int NOT NULL COMMENT '患者ID,外键',
 admission_date date NOT NULL COMMENT '接诊日期',
 admission_time varchar(4) NOT NULL COMMENT '接诊时间',
 peripheral varchar(4) NULL COMMENT '外周',
 iv_line varchar(4) NULL COMMENT '深静脉',
 central_access varchar(4) NULL COMMENT '骨通道',
 nasal_pipe varchar(4) NULL COMMENT '鼻导管',
 face_mask varchar(4) NULL COMMENT '面罩',
 endotracheal_tube varchar(4) NULL COMMENT '气管插管',
 ventilator varchar(4) NULL COMMENT '呼吸机',
 cpr varchar(2) NULL COMMENT '心肺复苏(是/否)',
 cpr_start_time varchar(4) NULL COMMENT '心肺复苏开始时间',
 cpr_end_time varchar(4) NULL COMMENT '心肺复苏结束时间',
 ultrasound varchar(4) NULL COMMENT 'B超',
 CT varchar(4) NULL COMMENT 'CT',
 tourniquet varchar(4) NULL COMMENT '止血带',
 blood_draw varchar(4) NULL COMMENT '采血',
 catheter varchar(4) NULL COMMENT '导尿',
 gastric_tube varchar(4) NULL COMMENT '胃管',
 transfusion varchar(2) NULL COMMENT '输血(是/否)',
 transfusion_start varchar(4) NULL COMMENT '输血开始时间',
 transfusion_end varchar(4) NULL COMMENT '输血结束时间',
 leave_surgery_time varchar(4) NULL COMMENT '离开抢救室时间',
 leave_surgery_date date NULL COMMENT '离开抢救室日期',
 patient_destination varchar(100) NULL COMMENT '病人去向',
 death varchar(2) NULL COMMENT '死亡(是/否)',
 death_date date NULL COMMENT '死亡日期',
 death_time varchar(4) NULL COMMENT '死亡时间',
 CONSTRAINT fk_intervention_patient FOREIGN KEY (patient_id) REFERENCES patient (patient_id)
 ) COMMENT '干预方式时间表';
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("interventiontime")
public class InterventionTime {
    /**
     * 干预方式ID 主键
     */
    @TableId(value = "intervention_id", type = IdType.AUTO)
    private Integer interventionId;
    /**
     * 患者ID 外键
     */
    private Integer patientId;
    /**
     * 接诊日期
     */
    private LocalDate admissionDate;
    /**
     * 接诊时间 / 入室时间
     */
    private String admissionTime;
    /**
     * 外周
     */
    private String peripheral;
    /**
     * 深静脉
     */
    private String ivLine;
    /**
     * 骨通道
     */
    private String centralAccess;
    /**
     * 鼻导管
     */
    private String nasalPipe;
    /**
     * 面罩
     */
    private String faceMask;
    /**
     * 气管插管
     */
    private String endotrachealTube;
    /**
     * 呼吸机开始时间
     */
    private String ventilator;
    /**
     * 心肺复苏开始时间
     */
    private String cprStartTime;
    /**
     * 心肺复苏结束时间
     */
    private String cprEndTime;
    /**
     * B超
     */
    private String ultrasound;
    /**
     * CT
     */
    @TableField("ct")
    @JsonProperty("CT")
    private String CT;
    /**
     * 止血带
     */
    private String tourniquet;
    /**
     * 采血
     * */
    private String bloodDraw;
    /**
     * 导尿
     */
    private String catheter;
    /**
     * 胃管
     */
    private String gastricTube;
    /**
     * 输血开始时间
     */
    private String transfusionStart;
    /**
     * 输血结束时间
     */
    private String transfusionEnd;
    /**
     * 离开抢救室时间
     */
    private String leaveSurgeryTime;
    /**
     * 心肺复苏(是/否)
     */
    private String cpr;
    /**
     * 输血(是/否)
     */
    private String transfusion;
    /**
     * 离开抢救室日期
     */
    private LocalDate leaveSurgeryDate;
    /**
     * 病人去向
     */
    private String patientDestination;
    /**
     * 死亡(是/否)
     */
    private String death;
    /**
     * 死亡日期
     */
    private LocalDate deathDate;
    /**
     * 死亡时间
     */
    private String deathTime;
}

package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

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
}

package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 患者伤情详情表
 * 存储患者的具体伤情信息
 */
@TableName("patient_injury_details")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PatientInjuryDetail implements Serializable {
    /**
     * 详情ID，自增主键
     */
    @TableId(value = "detail_id", type = IdType.AUTO)
    private Integer detailId;
    
    /**
     * 患者ID
     */
    private Integer patientId;
    
    /**
     * 伤情类型ID
     */
    private Integer injuryTypeId;
    
    /**
     * 伤情数量
     */
    private Integer injuryCount;
    
    /**
     * 备注信息
     */
    private String notes;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

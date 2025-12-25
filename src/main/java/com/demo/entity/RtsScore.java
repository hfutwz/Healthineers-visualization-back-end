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
 * 建表语句
 * CREATE TABLE rts_score (
 *     rts_id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'RTS评分ID',
 *     patient_id INT NOT NULL COMMENT '患者ID',
 *     gcs_score INT NOT NULL COMMENT 'GCS评分(0-4)',
 *     sbp_score INT NOT NULL COMMENT '收缩压评分(0-4)',
 *     rr_score INT NOT NULL COMMENT '呼吸频率评分(0-4)',
 *     INDEX idx_patient_id (patient_id),
 *     FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE CASCADE ON UPDATE CASCADE
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RTS评分表';
 */

/**
 * RTS评分表实体类
 * 对应数据库表：rts_score
 */
@TableName("rts_score")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RtsScore implements Serializable {
    /**
     * RTS评分ID，自增主键
     */
    @TableId(value = "rts_id", type = IdType.AUTO)
    private Integer rtsId;
    
    /**
     * 患者ID
     */
    private Integer patientId;
    
    /**
     * GCS评分 (0-4分)
     */
    private Integer gcsScore;
    
    /**
     * 收缩压评分 (0-4分)
     */
    private Integer sbpScore;
    
    /**
     * 呼吸频率评分 (0-4分)
     */
    private Integer rrScore;
    
    /**
     * RTS总分 (0-12分)
     * 注意：此字段不在数据库表中，仅用于计算，使用 @TableField(exist = false) 标记
     */
    @TableField(exist = false)
    private Integer totalScore;
}
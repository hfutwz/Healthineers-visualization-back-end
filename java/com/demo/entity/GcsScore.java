package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * GCS评分表实体类
 * 对应数据库表：gcs_score
 */
@TableName("gcs_score")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GcsScore implements Serializable {
    /**
     * GCS评分ID，自增主键
     */
    @TableId(value = "gcs_id", type = IdType.AUTO)
    private Integer gcsId;
    
    /**
     * 患者ID
     */
    private Integer patientId;
    
    /**
     * 睁眼评分 (1-4分)
     */
    private Integer eyeOpening;
    
    /**
     * 言语评分 (1-5分)
     */
    private Integer verbalResponse;
    
    /**
     * 运动评分 (1-6分)
     */
    private Integer motorResponse;
    
    /**
     * GCS总分 (3-15分)
     */
    private Integer totalScore;
    
    /**
     * 睁眼描述
     */
    private String eyeDescription;
    
    /**
     * 言语描述
     */
    private String verbalDescription;
    
    /**
     * 运动描述
     */
    private String motorDescription;
    
    /**
     * 意识状态
     */
    private String consciousnessLevel;
}

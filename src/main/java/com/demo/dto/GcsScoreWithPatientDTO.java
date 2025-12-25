package com.demo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * GCS评分与患者信息DTO
 */
@Data
public class GcsScoreWithPatientDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * GCS评分ID
     */
    private Integer gcsId;
    
    /**
     * 患者ID
     */
    private Integer patientId;
    
    /**
     * 患者年龄
     */
    private Integer age;
    
    /**
     * 患者性别
     */
    private String gender;
    
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


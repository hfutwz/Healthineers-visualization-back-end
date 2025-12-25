package com.demo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * RTS评分与患者信息DTO
 */
@Data
public class RtsScoreWithPatientDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * RTS评分ID
     */
    private Integer rtsId;
    
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
     */
    private Integer totalScore;
}


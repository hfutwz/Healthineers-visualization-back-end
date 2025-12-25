package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 患者查询条件DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientQueryDTO {
    /**
     * 患者ID
     */
    private Integer patientId;
    
    /**
     * 性别
     */
    private String gender;
    
    /**
     * 最小年龄
     */
    private Integer minAge;
    
    /**
     * 最大年龄
     */
    private Integer maxAge;
    
    /**
     * 当前页码，默认第1页
     */
    private Integer current = 1;
    
    /**
     * 每页大小，默认10条
     */
    private Integer size = 10;
}

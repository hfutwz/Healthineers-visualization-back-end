package com.demo.dto;

import com.demo.entity.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 患者分页查询结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientPageDTO {
    /**
     * 患者列表
     */
    private List<Patient> records;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Long current;
    
    /**
     * 每页大小
     */
    private Long size;
    
    /**
     * 总页数
     */
    private Long pages;
}

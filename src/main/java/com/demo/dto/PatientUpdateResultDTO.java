package com.demo.dto;

import com.demo.entity.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 患者更新结果DTO
 * 用于封装更新操作的返回结果，包含是否成功、消息和患者信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientUpdateResultDTO {
    /**
     * 操作是否成功
     */
    private Boolean success;
    
    /**
     * 消息（成功或错误信息）
     */
    private String message;
    
    /**
     * 患者信息（更新后的患者数据）
     */
    private Patient patient;
    
    /**
     * 创建成功结果
     */
    public static PatientUpdateResultDTO success(String message, Patient patient) {
        return new PatientUpdateResultDTO(true, message, patient);
    }
    
    /**
     * 创建失败结果
     */
    public static PatientUpdateResultDTO fail(String message) {
        return new PatientUpdateResultDTO(false, message, null);
    }
}


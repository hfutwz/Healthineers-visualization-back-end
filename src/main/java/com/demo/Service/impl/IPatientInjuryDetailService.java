package com.demo.Service.impl;

import com.demo.dto.PatientInjuryDetailDTO;

/**
 * 患者伤情详情服务接口
 */
public interface IPatientInjuryDetailService {
    
    /**
     * 根据患者ID获取详细伤情信息
     */
    PatientInjuryDetailDTO getInjuryDetailsByPatientId(Integer patientId);
}

package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.dto.PatientPageDTO;
import com.demo.dto.PatientQueryDTO;
import com.demo.dto.PatientUpdateResultDTO;
import com.demo.entity.Patient;

public interface IPatientService extends IService<Patient> {
    
    /**
     * 分页查询患者信息
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PatientPageDTO getPatientPage(PatientQueryDTO queryDTO);
    
    /**
     * 删除患者及其所有相关数据（级联删除）
     * @param patientId 患者ID
     * @return 是否删除成功
     */
    boolean deletePatientById(Integer patientId);
    
    /**
     * 更新患者基本信息
     * 包含业务逻辑验证和错误处理
     * @param patient 患者信息
     * @return 更新结果DTO，包含是否成功、消息和患者信息
     */
    PatientUpdateResultDTO updatePatient(Patient patient);
}

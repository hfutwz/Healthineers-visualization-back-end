package com.demo.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.PatientInfoOnAdmission;

/**
 * 患者入室前信息服务接口
 */
public interface IPatientInfoOnAdmissionService extends IService<PatientInfoOnAdmission> {
    
    /**
     * 根据患者ID查询入室前信息
     * @param patientId 患者ID
     * @return 入室前信息
     */
    PatientInfoOnAdmission getPatientInfoOnAdmissionByPatientId(Integer patientId);
}

package com.demo.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.PatientInfoOffAdmission;

/**
 * 患者离室后信息服务接口
 */
public interface IPatientInfoOffAdmissionService extends IService<PatientInfoOffAdmission> {
    
    /**
     * 根据患者ID查询离室后信息
     * @param patientId 患者ID
     * @return 离室后信息
     */
    PatientInfoOffAdmission getPatientInfoOffAdmissionByPatientId(Integer patientId);
}

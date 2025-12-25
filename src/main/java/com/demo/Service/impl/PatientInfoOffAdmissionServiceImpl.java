package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.Service.IPatientInfoOffAdmissionService;
import com.demo.entity.PatientInfoOffAdmission;
import com.demo.mapper.PatientInfoOffAdmissionMapper;
import org.springframework.stereotype.Service;

/**
 * 患者离室后信息服务实现类
 */
@Service
public class PatientInfoOffAdmissionServiceImpl extends ServiceImpl<PatientInfoOffAdmissionMapper, PatientInfoOffAdmission> implements IPatientInfoOffAdmissionService {

    @Override
    public PatientInfoOffAdmission getPatientInfoOffAdmissionByPatientId(Integer patientId) {
        return baseMapper.selectByPatientId(patientId);
    }
}

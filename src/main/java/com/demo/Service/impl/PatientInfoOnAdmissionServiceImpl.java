package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.Service.IPatientInfoOnAdmissionService;
import com.demo.entity.PatientInfoOnAdmission;
import com.demo.mapper.PatientInfoOnAdmissionMapper;
import org.springframework.stereotype.Service;

/**
 * 患者入室前信息服务实现类
 */
@Service
public class PatientInfoOnAdmissionServiceImpl extends ServiceImpl<PatientInfoOnAdmissionMapper, PatientInfoOnAdmission> implements IPatientInfoOnAdmissionService {

    @Override
    public PatientInfoOnAdmission getPatientInfoOnAdmissionByPatientId(Integer patientId) {
        return baseMapper.selectByPatientId(patientId);
    }
}

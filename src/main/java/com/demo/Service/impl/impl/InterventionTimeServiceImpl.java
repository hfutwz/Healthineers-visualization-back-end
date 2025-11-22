package com.demo.Service.impl.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.entity.InterventionTime;
import com.demo.mapper.InterventionTimeMapper;
import com.demo.Service.impl.IInterventionTimeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterventionTimeServiceImpl extends ServiceImpl<InterventionTimeMapper, InterventionTime> implements IInterventionTimeService {

    @Override
    public List<InterventionTime> getByPatientId(Integer patientId) {
        return baseMapper.selectByPatientId(patientId);
    }
}

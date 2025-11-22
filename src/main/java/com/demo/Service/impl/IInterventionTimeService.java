package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.InterventionTime;
import java.util.List;

public interface IInterventionTimeService extends IService<InterventionTime> {
    List<InterventionTime> getByPatientId(Integer patientId);
}


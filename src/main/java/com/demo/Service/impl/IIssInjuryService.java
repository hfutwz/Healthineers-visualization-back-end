package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.dto.IssInjuryDTO;
import com.demo.entity.IssInjury;

public interface IIssInjuryService extends IService<IssInjury> {
    /**
     * 根据ID,获取创伤信息（未使用）
     * @param patientId
     * @return
     */
    IssInjury getByPatientId(Integer patientId);

    /**
     * 根据ID,获取创伤信息（封装DTO对象，包含受伤等级）
     * @param patientId
     * @return
     */
    IssInjuryDTO getInjuryDTOByPatientId(Integer patientId);
}

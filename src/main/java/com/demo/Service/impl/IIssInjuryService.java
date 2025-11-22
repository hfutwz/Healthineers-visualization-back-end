package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.dto.IssInjuryDTO;
import com.demo.entity.IssInjury;

import java.util.List;

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

    /**
     * 根据经度、纬度、季节和时间段查询伤情信息
     */
    List<IssInjuryDTO> getInjuryByLocationAndFilters(Double longitude, Double latitude, List<Integer> seasons, List<Integer> timePeriods);

}

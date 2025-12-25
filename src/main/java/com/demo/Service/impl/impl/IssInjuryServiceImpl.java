package com.demo.Service.impl.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.Service.impl.IIssInjuryService;
import com.demo.dto.IssInjuryDTO;
import com.demo.entity.IssInjury;
import com.demo.mapper.IssInjuryMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IssInjuryServiceImpl extends ServiceImpl<IssInjuryMapper, IssInjury> implements IIssInjuryService {
    @Autowired
    private IssInjuryMapper issInjuryMapper;
    @Override
    public IssInjury getByPatientId(Integer patientId) {
        return baseMapper.selectByPatientId(patientId);
    }
    @Override
    public IssInjuryDTO getInjuryDTOByPatientId(Integer patientId) {
        IssInjury injury = getByPatientId(patientId);
        if (injury == null) {
            return null; // 或抛异常
        }

        // 计算伤情等级
        int severity;
        if (injury.getIssScore() <= 16) {
            severity = 0; // 轻伤
        } else if (injury.getIssScore() > 25) {
            severity = 2; // 严重
        } else {
            severity = 1; // 重伤
        }

        // 复制公共字段
        IssInjuryDTO dto = new IssInjuryDTO();
        BeanUtils.copyProperties(injury, dto);
        dto.setPatientId(patientId);
        dto.setInjurySeverity(severity);

        return dto;
    }

    @Override
    public List<IssInjuryDTO> getInjuryByLocationAndFilters(
            Double longitude, Double latitude, List<Integer> seasons, List<Integer> timePeriods) {
        // 调用Mapper，传递参数，返回数据
        List<IssInjuryDTO> injuries = issInjuryMapper.selectInjuryByLocationAndFilters(longitude, latitude, seasons, timePeriods);

        for (IssInjuryDTO injury : injuries) {
            int severity;
            if (injury.getIssScore() <= 16) {
                severity = 0; // 轻伤
            } else if (injury.getIssScore() > 25) {
                severity = 2; // 严重伤
            } else {
                severity = 1; // 重伤
            }
            injury.setInjurySeverity(severity);
        }
        return injuries;
    }

    @Override
    public List<Integer> getPatientIdsByLocationAndFilters(
            Double longitude, Double latitude, List<Integer> seasons, List<Integer> timePeriods) {
        return issInjuryMapper.selectPatientIdsByLocationAndFilters(longitude, latitude, seasons, timePeriods);
    }
}
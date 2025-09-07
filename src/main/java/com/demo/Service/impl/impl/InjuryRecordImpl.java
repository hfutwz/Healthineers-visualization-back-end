package com.demo.Service.impl.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.Service.impl.IInjuryRecordService;
import com.demo.dto.AddressCountDTO;
import com.demo.entity.InjuryRecord;
import com.demo.mapper.InjuryRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InjuryRecordImpl extends ServiceImpl<InjuryRecordMapper, InjuryRecord> implements IInjuryRecordService {
    @Autowired
    private InjuryRecordMapper injuryRecordMapper;

    @Override
    public List<AddressCountDTO> getAllLocations() {
        // 查询所有经纬度和病例数数据
        List<AddressCountDTO> allLocations = injuryRecordMapper.selectAllLocationCounts();
        // 过滤掉经纬度为null的记录
        return allLocations.stream()
                .filter(dto -> dto.getLatitude() != null && dto.getLongitude() != null)
                .collect(Collectors.toList());
    }
    @Override
    public List<AddressCountDTO> getLocationsByTimeRange(String startDate, String endDate, List<Integer> timePeriods) {
        return baseMapper.selectLocationsByTimeRange(startDate, endDate, timePeriods);
    }

    @Override
    public List<AddressCountDTO> getLocationsBySeasonsAndTime(List<Integer> seasons, List<Integer> timePeriods) {
        return baseMapper.selectLocationsBySeasonsAndTime(seasons, timePeriods);
    }

}

package com.demo.Service.impl.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.Service.impl.IInjuryRecordService;
import com.demo.dto.AddressCountDTO;
import com.demo.dto.HourlyStatisticsDTO;
import com.demo.dto.HourlyGroupDTO;
import com.demo.dto.HourlyGroupStatisticsDTO;
import com.demo.entity.InjuryRecord;
import com.demo.mapper.InjuryRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public List<AddressCountDTO> getLocationsBySeasonsAndTime(List<Integer> seasons, List<Integer> timePeriods, List<Integer> years) {
        return baseMapper.selectLocationsBySeasonsAndTime(seasons, timePeriods, years);
    }

    @Override
    public List<HourlyStatisticsDTO> getHourlyStatistics(Integer year, List<Integer> seasons, String startDate, String endDate) {
        return baseMapper.selectHourlyStatistics(year, seasons, startDate, endDate);
    }

    @Override
    public List<HourlyGroupStatisticsDTO> getHourlyStatisticsByGroups(Integer year, List<Integer> seasons, String startDate, String endDate, List<HourlyGroupDTO> groups) {
        if (groups == null || groups.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<HourlyGroupStatisticsDTO> result = new ArrayList<>();
        
        for (HourlyGroupDTO group : groups) {
            if (group.getHours() == null || group.getHours().isEmpty()) {
                continue;
            }
            
            // 查询该组的患者总数
            Integer count = baseMapper.selectPatientCountByHours(year, seasons, startDate, endDate, group.getHours());
            if (count == null) {
                count = 0;
            }
            
            // 生成组标签（如 "0-1,1-2,2-3"）
            String groupLabel = group.getHours().stream()
                    .sorted()
                    .map(h -> {
                        int nextHour = (h + 1) % 24;
                        return String.format("%02d:00-%02d:00", h, nextHour);
                    })
                    .collect(Collectors.joining(","));
            
            // 生成小时显示（如 "0,1,2"）
            String hoursDisplay = group.getHours().stream()
                    .sorted()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            
            HourlyGroupStatisticsDTO dto = new HourlyGroupStatisticsDTO();
            dto.setGroupIndex(group.getGroupIndex());
            dto.setGroupLabel(groupLabel);
            dto.setCount(count);
            dto.setHoursDisplay(hoursDisplay);
            
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public List<Integer> getAvailableYears() {
        return baseMapper.selectAvailableYears();
    }

}

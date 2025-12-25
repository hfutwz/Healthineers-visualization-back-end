package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.dto.AddressCountDTO;
import com.demo.dto.HourlyStatisticsDTO;
import com.demo.dto.HourlyGroupDTO;
import com.demo.dto.HourlyGroupStatisticsDTO;
import com.demo.entity.InjuryRecord;

import java.util.List;

public interface IInjuryRecordService extends IService<InjuryRecord> {
    List<AddressCountDTO> getAllLocations();
    List<AddressCountDTO> getLocationsByTimeRange(String startDate, String endDate, List<Integer> timePeriods);
    List<AddressCountDTO> getLocationsBySeasonsAndTime(List<Integer> seasons, List<Integer> timePeriods, List<Integer> years);
    List<HourlyStatisticsDTO> getHourlyStatistics(Integer year, List<Integer> seasons, String startDate, String endDate);

    /**
     * 根据时间段分组查询患者数量
     * @param year 年份（可选）
     * @param seasons 季节列表（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param groups 时间段分组列表
     * @return 分组统计结果列表
     */
    List<HourlyGroupStatisticsDTO> getHourlyStatisticsByGroups(Integer year, List<Integer> seasons, String startDate, String endDate, List<HourlyGroupDTO> groups);

    List<Integer> getAvailableYears();
}

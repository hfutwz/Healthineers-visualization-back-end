package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.dto.AddressCountDTO;
import com.demo.entity.InjuryRecord;

import java.util.List;

public interface IInjuryRecordService extends IService<InjuryRecord> {
    List<AddressCountDTO> getAllLocations();
    List<AddressCountDTO> getLocationsByTimeRange(String startDate, String endDate, List<Integer> timePeriods);
    List<AddressCountDTO> getLocationsBySeasonsAndTime(List<Integer> seasons, List<Integer> timePeriods);
}

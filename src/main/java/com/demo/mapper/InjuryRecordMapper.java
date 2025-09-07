package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.dto.AddressCountDTO;
import com.demo.entity.InjuryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InjuryRecordMapper extends BaseMapper<InjuryRecord> {
    @Select("SELECT latitude, longitude, count(*) as count FROM injuryrecord GROUP BY latitude, longitude")
    List<AddressCountDTO> selectAllLocationCounts();

    /**
     * 根据时间范围和时间段筛选
     */
    List<AddressCountDTO> selectLocationsByTimeRange(
            String startDate,
            String endDate,
            List<Integer> timePeriods
    );

    /**
     * 根据季节和时间段筛选
     */
    List<AddressCountDTO> selectLocationsBySeasonsAndTime(
            List<Integer> seasons,
            List<Integer> timePeriods
    );
}

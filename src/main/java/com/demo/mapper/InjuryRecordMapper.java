package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.dto.AddressCountDTO;
import com.demo.dto.HourlyStatisticsDTO;
import com.demo.entity.InjuryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("timePeriods") List<Integer> timePeriods
    );

    /**
     * 根据季节和时间段筛选
     */
    List<AddressCountDTO> selectLocationsBySeasonsAndTime(
            @Param("seasons") List<Integer> seasons,
            @Param("timePeriods") List<Integer> timePeriods,
            @Param("years") List<Integer> years
    );

    /**
     * 获取24小时统计数据
     */
    List<HourlyStatisticsDTO> selectHourlyStatistics(
            @Param("year") Integer year,
            @Param("seasons") List<Integer> seasons,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 查询可用年份（去重、升序）
     */
    List<Integer> selectAvailableYears();

    /**
     * 根据时间段分组查询患者数量
     * @param year 年份（可选）
     * @param seasons 季节列表（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param hours 小时列表（0-23）
     * @return 该时间段的患者总数
     */
    Integer selectPatientCountByHours(
            @Param("year") Integer year,
            @Param("seasons") List<Integer> seasons,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("hours") List<Integer> hours
    );
}

package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.dto.IssInjuryDTO;
import com.demo.entity.IssInjury;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IssInjuryMapper extends BaseMapper<IssInjury> {

    // 根据patientId查询创伤信息（如果有多条记录，返回最新的一条）
    @Select("SELECT * FROM iss_patient_injury_severity WHERE patient_id = #{patientId} ORDER BY injury_id DESC LIMIT 1")
    IssInjury selectByPatientId(Integer patientId);

    List<IssInjuryDTO> selectInjuryByLocationAndFilters(
            @Param("longitude") Double longitude,
            @Param("latitude") Double latitude,
            @Param("seasons") List<Integer> seasons,
            @Param("timePeriods") List<Integer> timePeriods);

    /**
     * 根据经度、纬度、季节和时间段查询患者ID列表（只返回ID，减少数据传输）
     */
    List<Integer> selectPatientIdsByLocationAndFilters(
            @Param("longitude") Double longitude,
            @Param("latitude") Double latitude,
            @Param("seasons") List<Integer> seasons,
            @Param("timePeriods") List<Integer> timePeriods);
}

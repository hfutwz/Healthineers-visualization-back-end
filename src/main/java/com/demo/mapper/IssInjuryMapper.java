package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.dto.IssInjuryDTO;
import com.demo.entity.IssInjury;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IssInjuryMapper extends BaseMapper<IssInjury> {

    // 根据patientId查询创伤信息
    @Select("SELECT * FROM iss_patient_injury_severity WHERE patient_id = #{patientId}")
    IssInjury selectByPatientId(Integer patientId);

    List<IssInjuryDTO> selectInjuryByLocationAndFilters(
            @Param("longitude") Double longitude,
            @Param("latitude") Double latitude,
            @Param("seasons") List<Integer> seasons,
            @Param("timePeriods") List<Integer> timePeriods);
}

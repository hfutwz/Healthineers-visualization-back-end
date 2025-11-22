package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.entity.InterventionTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InterventionTimeMapper extends BaseMapper<InterventionTime> {

    // 自定义查询：根据患者ID查询干预时间
    @Select("SELECT * FROM interventiontime WHERE patient_id = #{patientId}")
    List<InterventionTime> selectByPatientId(Integer patientId);
}
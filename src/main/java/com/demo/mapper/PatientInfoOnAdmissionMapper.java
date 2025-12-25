package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.entity.PatientInfoOnAdmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 患者入室前信息Mapper接口
 */
@Mapper
public interface PatientInfoOnAdmissionMapper extends BaseMapper<PatientInfoOnAdmission> {
    
    /**
     * 根据患者ID查询入室前信息
     * @param patientId 患者ID
     * @return 入室前信息
     */
    PatientInfoOnAdmission selectByPatientId(@Param("patientId") Integer patientId);
}

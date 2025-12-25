package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.entity.PatientInfoOffAdmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 患者离室后信息Mapper接口
 */
@Mapper
public interface PatientInfoOffAdmissionMapper extends BaseMapper<PatientInfoOffAdmission> {
    
    /**
     * 根据患者ID查询离室后信息
     * @param patientId 患者ID
     * @return 离室后信息
     */
    PatientInfoOffAdmission selectByPatientId(@Param("patientId") Integer patientId);
}

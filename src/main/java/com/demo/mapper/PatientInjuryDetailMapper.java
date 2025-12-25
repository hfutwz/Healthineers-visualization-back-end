package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.dto.InjuryDetailDTO;
import com.demo.entity.PatientInjuryDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PatientInjuryDetailMapper extends BaseMapper<PatientInjuryDetail> {
    
    /**
     * 根据患者ID和身体部位查询伤情详情
     */
    List<InjuryDetailDTO> selectInjuryDetailsByPatientIdAndBodyPart(
            @Param("patientId") Integer patientId, 
            @Param("bodyPart") String bodyPart);
}

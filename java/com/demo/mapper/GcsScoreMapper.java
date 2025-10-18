package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.entity.GcsScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * GCS评分Mapper接口
 */
@Mapper
public interface GcsScoreMapper extends BaseMapper<GcsScore> {
    
    /**
     * 根据患者ID查询GCS评分
     * @param patientId 患者ID
     * @return GCS评分信息
     */
    GcsScore selectByPatientId(@Param("patientId") Integer patientId);
}

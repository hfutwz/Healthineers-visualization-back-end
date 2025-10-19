package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.entity.RtsScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * RTS评分Mapper接口
 */
@Mapper
public interface RtsScoreMapper extends BaseMapper<RtsScore> {
    
    /**
     * 根据患者ID查询RTS评分
     * @param patientId 患者ID
     * @return RTS评分信息
     */
    RtsScore selectByPatientId(@Param("patientId") Integer patientId);
}
package com.demo.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.RtsScore;
import com.demo.dto.RtsScoreWithPatientDTO;

/**
 * RTS评分服务接口
 */
public interface IRtsScoreService extends IService<RtsScore> {
    
    /**
     * 根据患者ID查询RTS评分
     * @param patientId 患者ID
     * @return RTS评分信息
     */
    RtsScore getRtsScoreByPatientId(Integer patientId);
    
    /**
     * 根据患者ID查询RTS评分及患者基本信息
     * @param patientId 患者ID
     * @return RTS评分及患者信息
     */
    RtsScoreWithPatientDTO getRtsScoreWithPatientInfo(Integer patientId);
}
package com.demo.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.GcsScore;

/**
 * GCS评分服务接口
 */
public interface IGcsScoreService extends IService<GcsScore> {
    
    /**
     * 根据患者ID查询GCS评分
     * @param patientId 患者ID
     * @return GCS评分信息
     */
    GcsScore getGcsScoreByPatientId(Integer patientId);
}

package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.Service.IGcsScoreService;
import com.demo.entity.GcsScore;
import com.demo.mapper.GcsScoreMapper;
import org.springframework.stereotype.Service;

/**
 * GCS评分服务实现类
 */
@Service
public class GcsScoreServiceImpl extends ServiceImpl<GcsScoreMapper, GcsScore> implements IGcsScoreService {

    @Override
    public GcsScore getGcsScoreByPatientId(Integer patientId) {
        return baseMapper.selectByPatientId(patientId);
    }
}

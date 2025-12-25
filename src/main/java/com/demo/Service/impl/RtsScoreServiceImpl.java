package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.Service.IRtsScoreService;
import com.demo.entity.RtsScore;
import com.demo.dto.RtsScoreWithPatientDTO;
import com.demo.mapper.RtsScoreMapper;
import org.springframework.stereotype.Service;

/**
 * RTS评分服务实现类
 */
@Service
public class RtsScoreServiceImpl extends ServiceImpl<RtsScoreMapper, RtsScore> implements IRtsScoreService {

    @Override
    public RtsScore getRtsScoreByPatientId(Integer patientId) {
        RtsScore rtsScore = baseMapper.selectByPatientId(patientId);
        // 计算totalScore（数据库表中没有该字段，需要计算）
        if (rtsScore != null && rtsScore.getGcsScore() != null && 
            rtsScore.getSbpScore() != null && rtsScore.getRrScore() != null) {
            rtsScore.setTotalScore(rtsScore.getGcsScore() + rtsScore.getSbpScore() + rtsScore.getRrScore());
        }
        return rtsScore;
    }

    @Override
    public RtsScoreWithPatientDTO getRtsScoreWithPatientInfo(Integer patientId) {
        return baseMapper.selectWithPatientInfo(patientId);
    }
}
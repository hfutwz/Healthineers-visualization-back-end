package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.Service.IRtsScoreService;
import com.demo.entity.RtsScore;
import com.demo.mapper.RtsScoreMapper;
import org.springframework.stereotype.Service;

/**
 * RTS评分服务实现类
 */
@Service
public class RtsScoreServiceImpl extends ServiceImpl<RtsScoreMapper, RtsScore> implements IRtsScoreService {

    @Override
    public RtsScore getRtsScoreByPatientId(Integer patientId) {
        return baseMapper.selectByPatientId(patientId);
    }
}
package com.demo.Service.impl.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.Service.impl.IPatientService;
import com.demo.dto.PatientPageDTO;
import com.demo.dto.PatientQueryDTO;
import com.demo.entity.Patient;
import com.demo.mapper.PatientMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PatientImpl extends ServiceImpl<PatientMapper, Patient> implements IPatientService {

    @Override
    public PatientPageDTO getPatientPage(PatientQueryDTO queryDTO) {
        // 创建分页对象
        Page<Patient> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        // 构建查询条件
        LambdaQueryWrapper<Patient> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按患者ID查询
        if (queryDTO.getPatientId() != null) {
            queryWrapper.eq(Patient::getPatientId, queryDTO.getPatientId());
        }
        
        // 按性别查询
        if (StringUtils.hasText(queryDTO.getGender())) {
            queryWrapper.eq(Patient::getGender, queryDTO.getGender());
        }
        
        // 按年龄段查询
        if (queryDTO.getMinAge() != null) {
            queryWrapper.ge(Patient::getAge, queryDTO.getMinAge());
        }
        if (queryDTO.getMaxAge() != null) {
            queryWrapper.le(Patient::getAge, queryDTO.getMaxAge());
        }
        
        // 按患者ID排序
        queryWrapper.orderByAsc(Patient::getPatientId);
        
        // 执行分页查询
        Page<Patient> result = this.page(page, queryWrapper);
        
        // 构建返回结果
        PatientPageDTO pageDTO = new PatientPageDTO();
        pageDTO.setRecords(result.getRecords());
        pageDTO.setTotal(result.getTotal());
        pageDTO.setCurrent(result.getCurrent());
        pageDTO.setSize(result.getSize());
        pageDTO.setPages(result.getPages());
        
        return pageDTO;
    }
}

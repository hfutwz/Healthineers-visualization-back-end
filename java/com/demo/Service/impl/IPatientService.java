package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.dto.AddressCountDTO;
import com.demo.dto.PatientPageDTO;
import com.demo.dto.PatientQueryDTO;
import com.demo.entity.Patient;

import java.util.List;

public interface IPatientService extends IService<Patient> {
    
    /**
     * 分页查询患者信息
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PatientPageDTO getPatientPage(PatientQueryDTO queryDTO);
}

package com.demo.Service.impl.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.Service.impl.IInjuryRecordService;
import com.demo.Service.impl.IPatientService;
import com.demo.dto.AddressCountDTO;
import com.demo.entity.Patient;
import com.demo.mapper.InjuryRecordMapper;
import com.demo.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientImpl extends ServiceImpl<PatientMapper, Patient> implements IPatientService {

}

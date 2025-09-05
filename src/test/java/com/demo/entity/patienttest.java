package com.demo.entity;

import com.demo.mapper.InjuryRecordMapper;
import com.demo.mapper.PatientMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class patienttest {

    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private InjuryRecordMapper injuryRecordMapper;

    @Test
    public void testInsertAndSelect() {
        //查询id=1的用户，并打印
        Patient patient = patientMapper.selectById(1);
        System.out.println(patient);
        System.out.println(patientMapper.insert(new Patient(null, "女", 18, 1, 170.0, 70.0, "张三")));
    }
    @Test
    public void testUpdate() {
        Patient patient = new Patient();
        patient.setPatientId(1);
        patient.setGender("女");
        patient.setAge(18);
        patient.setIsGreenChannel(1);
        patient.setHeight(170.0);
        patient.setWeight(70.0);
        patient.setName("张三");
        System.out.println(patientMapper.updateById(patient));
    }
    //测试injuryrecord表数据
    @Test
    public void testInjuryRecord() {
        InjuryRecord injuryRecord = new InjuryRecord();
        injuryRecord.setPatientId(1);
        injuryRecord.setAdmissionDate(LocalDate.of(2023, 1, 1));
        injuryRecord.setAdmissionTime("0800");
        injuryRecord.setArrivalMethod("120");
        injuryRecord.setInjuryLocationDesc("上海大学");
        System.out.println(injuryRecordMapper.insert(injuryRecord));

    }
}

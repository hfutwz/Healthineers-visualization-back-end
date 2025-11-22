package com.demo.entity;

import com.demo.Service.impl.IPatientService;
import com.demo.mapper.InjuryRecordMapper;
import com.demo.mapper.PatientMapper;
import org.junit.jupiter.api.Test;
import org.redisson.api.GeoEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class patienttest {
    @Autowired
    private IPatientService patientService;
    @Test
    public void test() throws InterruptedException {
        //查询全部
        List<Patient> patients = patientService.list();
        for (Patient patient : patients) {
            System.out.println( patient.toString());
        }
    }
}

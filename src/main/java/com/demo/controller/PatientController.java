package com.demo.controller;

import com.demo.Service.impl.IPatientService;
import com.demo.dto.Result;
import com.demo.entity.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private IPatientService patientService;

    /**
     * 查询所有患者基本信息
     */
    @GetMapping("/list")
    public Result listPatients() {
        List<Patient> patients = patientService.list();
        return Result.ok(patients);
    }
}

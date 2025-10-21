package com.demo.controller;

import com.demo.Service.impl.IPatientService;
import com.demo.dto.PatientPageDTO;
import com.demo.dto.PatientQueryDTO;
import com.demo.dto.Result;
import com.demo.entity.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private IPatientService patientService;

    /**
     * 查询所有患者基本信息（保留原接口）
     */
    @GetMapping("/list")
    public Result listPatients() {
        List<Patient> patients = patientService.list();
        return Result.ok(patients);
    }

    /**
     * 分页查询患者信息
     */
    @PostMapping("/page")
    public Result getPatientPage(@RequestBody PatientQueryDTO queryDTO) {
        PatientPageDTO pageDTO = patientService.getPatientPage(queryDTO);
        return Result.ok(pageDTO);
    }

    /**
     * 分页查询患者信息（GET方式，用于简单查询）
     */
    @GetMapping("/page")
    public Result getPatientPageByGet(
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        PatientQueryDTO queryDTO = new PatientQueryDTO();
        queryDTO.setPatientId(patientId);
        queryDTO.setGender(gender);
        queryDTO.setMinAge(minAge);
        queryDTO.setMaxAge(maxAge);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        
        PatientPageDTO pageDTO = patientService.getPatientPage(queryDTO);
        return Result.ok(pageDTO);
    }
}

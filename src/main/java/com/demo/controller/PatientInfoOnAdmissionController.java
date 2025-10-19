package com.demo.controller;

import com.demo.Service.IPatientInfoOnAdmissionService;
import com.demo.entity.PatientInfoOnAdmission;
import com.demo.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 患者入室前信息控制器
 */
@RestController
@RequestMapping("/api/patient/on-admission")
@CrossOrigin(origins = "*")
public class PatientInfoOnAdmissionController {

    @Autowired
    private IPatientInfoOnAdmissionService patientInfoOnAdmissionService;

    /**
     * 根据患者ID获取入室前信息
     * @param patientId 患者ID
     * @return 入室前信息
     */
    @GetMapping("/{patientId}")
    public Result getPatientInfoOnAdmissionByPatientId(@PathVariable Integer patientId) {
        try {
            PatientInfoOnAdmission patientInfo = patientInfoOnAdmissionService.getPatientInfoOnAdmissionByPatientId(patientId);
            if (patientInfo != null) {
                return Result.ok(patientInfo);
            } else {
                return Result.fail("未找到该患者的入室前信息");
            }
        } catch (Exception e) {
            return Result.fail("获取入室前信息失败: " + e.getMessage());
        }
    }
}

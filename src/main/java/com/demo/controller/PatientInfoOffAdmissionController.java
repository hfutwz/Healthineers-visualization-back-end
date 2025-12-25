package com.demo.controller;

import com.demo.Service.IPatientInfoOffAdmissionService;
import com.demo.entity.PatientInfoOffAdmission;
import com.demo.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 患者离室后信息控制器
 */
@RestController
@RequestMapping("/api/patient/off-admission")
@CrossOrigin(origins = "*")
public class PatientInfoOffAdmissionController {

    @Autowired
    private IPatientInfoOffAdmissionService patientInfoOffAdmissionService;

    /**
     * 根据患者ID获取离室后信息
     * @param patientId 患者ID
     * @return 离室后信息
     */
    @GetMapping("/{patientId}")
    public Result getPatientInfoOffAdmissionByPatientId(@PathVariable Integer patientId) {
        try {
            PatientInfoOffAdmission patientInfo = patientInfoOffAdmissionService.getPatientInfoOffAdmissionByPatientId(patientId);
            // 数据不存在时返回空对象，而不是失败，允许前端显示空数据
            return Result.ok(patientInfo);
        } catch (Exception e) {
            return Result.fail("获取离室后信息失败: " + e.getMessage());
        }
    }
}

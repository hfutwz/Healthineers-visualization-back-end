package com.demo.controller;

import com.demo.Service.impl.IPatientInjuryDetailService;
import com.demo.dto.PatientInjuryDetailDTO;
import com.demo.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 患者伤情详情控制器
 */
@RestController
@RequestMapping("/api/iss/injury")
public class PatientInjuryDetailController {
    
    @Autowired
    private IPatientInjuryDetailService patientInjuryDetailService;
    
    /**
     * 根据患者ID获取详细伤情信息
     */
    @GetMapping("/{patientId}/details")
    public Result getInjuryDetailsByPatientId(@PathVariable("patientId") Integer patientId) {
        PatientInjuryDetailDTO details = patientInjuryDetailService.getInjuryDetailsByPatientId(patientId);
        if (details == null) {
            return Result.fail("未找到患者伤情信息");
        }
        return Result.ok(details);
    }
}

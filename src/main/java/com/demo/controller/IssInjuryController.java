package com.demo.controller;

import com.demo.Service.impl.IIssInjuryService;
import com.demo.dto.Result;
import com.demo.entity.IssInjury;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iss/injury")
public class IssInjuryController {
    @Autowired
    private IIssInjuryService injuryService;

    /**
     * 根据患者ID获取创伤信息
     */
    @GetMapping("/{patientId}")
    public Result getInjuryByPatientId(@PathVariable("patientId") Integer patientId) {
        return Result.ok(injuryService.getInjuryDTOByPatientId(patientId));
    }
}

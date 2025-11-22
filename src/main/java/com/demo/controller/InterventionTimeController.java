package com.demo.controller;

import com.demo.Service.impl.IInterventionTimeService;
import com.demo.dto.Result;
import com.demo.entity.InterventionTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/intervention")
public class InterventionTimeController {

    @Autowired
    private IInterventionTimeService interventionTimeService;

    /**
     * 根据患者ID查询干预时间
     */
    @GetMapping("/patient/{patientId}")
    public Result getInterventionByPatientId(@PathVariable Integer patientId) {
        List<InterventionTime> list = interventionTimeService.getByPatientId(patientId);
        return Result.ok(list);
    }
}
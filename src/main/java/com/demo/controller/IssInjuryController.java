package com.demo.controller;

import com.demo.Service.impl.IIssInjuryService;
import com.demo.dto.IssInjuryDTO;
import com.demo.dto.Result;
import com.demo.entity.IssInjury;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/iss/injury")
public class IssInjuryController {
    @Autowired
    private IIssInjuryService issInjuryService;

    /**
     * 根据患者ID获取创伤信息
     */
    @GetMapping("/{patientId}")
    public Result getInjuryByPatientId(@PathVariable("patientId") Integer patientId) {
        return Result.ok(issInjuryService.getInjuryDTOByPatientId(patientId));
    }

    /**
     * 根据经度、纬度、季节和时间段查询伤情信息
     * @param longitude
     * @param latitude
     * @param seasons
     * @param timePeriods
     * @return
     */
    @GetMapping("/search")
    public Result searchInjury(
            @RequestParam("longitude") Double longitude,
            @RequestParam("latitude") Double latitude,
            @RequestParam(name="seasons", required=false) List<Integer> seasons,
            @RequestParam(name="timePeriods", required=false) List<Integer> timePeriods) {
        List<IssInjuryDTO> injuries = issInjuryService.getInjuryByLocationAndFilters(longitude, latitude, seasons, timePeriods);
        return Result.ok(injuries);
    }
}

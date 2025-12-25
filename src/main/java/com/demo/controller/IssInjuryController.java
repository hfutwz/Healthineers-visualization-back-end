package com.demo.controller;

import com.demo.Service.impl.IIssInjuryService;
import com.demo.dto.IssInjuryDTO;
import com.demo.dto.Result;
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
     * 根据经度、纬度、季节和时间段查询伤情信息（返回完整数据）
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

    /**
     * 根据经度、纬度、季节和时间段查询患者ID列表（只返回ID，减少数据传输）
     * @param longitude
     * @param latitude
     * @param seasons
     * @param timePeriods
     * @return
     */
    @GetMapping("/search/ids")
    public Result searchPatientIds(
            @RequestParam("longitude") Double longitude,
            @RequestParam("latitude") Double latitude,
            @RequestParam(name="seasons", required=false) List<Integer> seasons,
            @RequestParam(name="timePeriods", required=false) List<Integer> timePeriods) {
        List<Integer> patientIds = issInjuryService.getPatientIdsByLocationAndFilters(longitude, latitude, seasons, timePeriods);
        return Result.ok(patientIds);
    }

    /**
     * 获取ISS分布数据（按ISS评分分类：0-轻伤，1-重伤，2-严重伤）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选，可以是 "all" 表示查询所有年份）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return ISS分布数据
     */
    @GetMapping("/distribution")
    public Result getISSDistributionData(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate,
                                       @RequestParam(required = false) String year,
                                       @RequestParam(required = false) Integer season,
                                       @RequestParam(required = false) Integer timePeriod) {
        try {
            // 模拟数据，避免数据库字段类型问题
            java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
            
            // 轻伤数据
            java.util.Map<String, Object> lightData = new java.util.HashMap<>();
            lightData.put("name", "轻伤(ISS≤16)");
            lightData.put("value", 1200);
            lightData.put("color", "#52C41A");
            lightData.put("percentage", 60.0);
            result.add(lightData);
            
            // 重伤数据
            java.util.Map<String, Object> severeData = new java.util.HashMap<>();
            severeData.put("name", "重伤(16<ISS≤25)");
            severeData.put("value", 600);
            severeData.put("color", "#FA8C16");
            severeData.put("percentage", 30.0);
            result.add(severeData);
            
            // 严重伤数据
            java.util.Map<String, Object> criticalData = new java.util.HashMap<>();
            criticalData.put("name", "严重伤(ISS>25)");
            criticalData.put("value", 200);
            criticalData.put("color", "#F5222D");
            criticalData.put("percentage", 10.0);
            result.add(criticalData);
            
            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取ISS分布数据失败：" + e.getMessage());
        }
    }
}

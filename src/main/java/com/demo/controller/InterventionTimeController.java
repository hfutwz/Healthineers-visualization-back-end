package com.demo.controller;

import com.demo.Service.impl.IInterventionTimeService;
import com.demo.dto.Result;
import com.demo.dto.TimelineEventDTO;
import com.demo.dto.TimelineStatisticsDTO;
import com.demo.dto.AllKeyEventsStatisticsDTO;
import com.demo.entity.InterventionTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intervention")
public class InterventionTimeController {

    @Autowired
    private IInterventionTimeService interventionTimeService;

    private Integer parsePatientId(String patientIdStr) {
        if (patientIdStr == null) {
            return null;
        }
        String trimmed = patientIdStr.trim();
        if (trimmed.isEmpty() || "undefined".equalsIgnoreCase(trimmed) || "null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        try {
            return Integer.valueOf(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 根据患者ID查询干预时间（原始数据）
     */
    @GetMapping("/patient/{patientId}")
    public Result getInterventionByPatientId(@PathVariable String patientId) {
        Integer parsed = parsePatientId(patientId);
        if (parsed == null || parsed <= 0) {
            return Result.error("患者ID无效");
        }
        List<InterventionTime> list = interventionTimeService.getByPatientId(parsed);
        return Result.ok(list);
    }

    /**
     * 获取患者时间线事件
     */
    @GetMapping("/timeline/{patientId}")
    public Result getTimelineEvents(@PathVariable String patientId) {
        Integer parsed = parsePatientId(patientId);
        if (parsed == null || parsed <= 0) {
            return Result.error("患者ID无效");
        }
        List<TimelineEventDTO> events = interventionTimeService.getTimelineEvents(parsed);
        return Result.ok(events);
    }

    /**
     * 获取关键事件
     */
    @GetMapping("/key-events/{patientId}")
    public Result getKeyEvents(@PathVariable String patientId) {
        Integer parsed = parsePatientId(patientId);
        if (parsed == null || parsed <= 0) {
            return Result.error("患者ID无效");
        }
        List<TimelineEventDTO> events = interventionTimeService.getKeyEvents(parsed);
        return Result.ok(events);
    }

    /**
     * 获取非关键事件
     */
    @GetMapping("/non-key-events/{patientId}")
    public Result getNonKeyEvents(@PathVariable String patientId) {
        Integer parsed = parsePatientId(patientId);
        if (parsed == null || parsed <= 0) {
            return Result.error("患者ID无效");
        }
        List<TimelineEventDTO> events = interventionTimeService.getNonKeyEvents(parsed);
        return Result.ok(events);
    }

    /**
     * 获取事件统计信息（用于绘制曲线）
     */
    @GetMapping("/statistics/{eventType}")
    public Result getEventStatistics(@PathVariable String eventType, 
                                    @RequestParam(required = false) String patientId) {
        Integer parsed = parsePatientId(patientId);
        TimelineStatisticsDTO statistics = interventionTimeService.getEventStatistics(eventType, parsed);
        return Result.ok(statistics);
    }
    
    /**
     * 获取所有关键事件的正态分布统计信息
     * 包括错误数据过滤和记录
     */
    @GetMapping("/all-key-events-statistics")
    public Result getAllKeyEventsStatistics(@RequestParam(required = false) String patientId) {
        Integer parsed = parsePatientId(patientId);
        AllKeyEventsStatisticsDTO statistics;
        if (parsed != null && parsed > 0) {
            statistics = interventionTimeService.getAllKeyEventsStatistics(parsed);
        } else {
            statistics = interventionTimeService.getAllKeyEventsStatistics();
        }
        return Result.ok(statistics);
    }
    
    /**
     * 根据患者ID查询干预时间记录（用于编辑回显）
     * @param patientId 患者ID
     * @return 干预时间记录
     */
    @GetMapping("/edit/{patientId}")
    public Result getInterventionTimeForEdit(@PathVariable String patientId) {
        Integer parsed = parsePatientId(patientId);
        if (parsed == null || parsed <= 0) {
            return Result.error("患者ID无效");
        }
        InterventionTime interventionTime = interventionTimeService.getOneByPatientId(parsed);
        if (interventionTime == null) {
            return Result.ok(null); // 返回null表示该患者还没有干预时间记录
        }
        return Result.ok(interventionTime);
    }
    
    /**
     * 更新干预时间记录
     * @param interventionTime 干预时间记录
     * @return 更新结果
     */
    @RequestMapping(value = "/update", method = {RequestMethod.POST, RequestMethod.PUT})
    @CrossOrigin(origins = "*")
    public Result updateInterventionTime(@RequestBody InterventionTime interventionTime) {
        try {
            if (interventionTime == null || interventionTime.getPatientId() == null) {
                return Result.fail("患者ID不能为空");
            }
            
            boolean success = interventionTimeService.updateInterventionTime(interventionTime);
            if (success) {
                return Result.ok("更新成功");
            } else {
                return Result.fail("更新失败");
            }
        } catch (Exception e) {
            return Result.fail("更新失败：" + e.getMessage());
        }
    }
}
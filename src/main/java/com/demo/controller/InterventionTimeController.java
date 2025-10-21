package com.demo.controller;

import com.demo.Service.impl.IInterventionTimeService;
import com.demo.dto.Result;
import com.demo.dto.TimelineEventDTO;
import com.demo.dto.TimelineStatisticsDTO;
import com.demo.entity.InterventionTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intervention")
public class InterventionTimeController {

    @Autowired
    private IInterventionTimeService interventionTimeService;

    /**
     * 根据患者ID查询干预时间（原始数据）
     */
    @GetMapping("/patient/{patientId}")
    public Result getInterventionByPatientId(@PathVariable Integer patientId) {
        if (patientId == null || patientId <= 0) {
            return Result.error("患者ID无效");
        }
        List<InterventionTime> list = interventionTimeService.getByPatientId(patientId);
        return Result.ok(list);
    }

    /**
     * 获取患者时间线事件
     */
    @GetMapping("/timeline/{patientId}")
    public Result getTimelineEvents(@PathVariable Integer patientId) {
        if (patientId == null || patientId <= 0) {
            return Result.error("患者ID无效");
        }
        List<TimelineEventDTO> events = interventionTimeService.getTimelineEvents(patientId);
        return Result.ok(events);
    }

    /**
     * 获取关键事件
     */
    @GetMapping("/key-events/{patientId}")
    public Result getKeyEvents(@PathVariable Integer patientId) {
        if (patientId == null || patientId <= 0) {
            return Result.error("患者ID无效");
        }
        List<TimelineEventDTO> events = interventionTimeService.getKeyEvents(patientId);
        return Result.ok(events);
    }

    /**
     * 获取非关键事件
     */
    @GetMapping("/non-key-events/{patientId}")
    public Result getNonKeyEvents(@PathVariable Integer patientId) {
        if (patientId == null || patientId <= 0) {
            return Result.error("患者ID无效");
        }
        List<TimelineEventDTO> events = interventionTimeService.getNonKeyEvents(patientId);
        return Result.ok(events);
    }

    /**
     * 获取事件统计信息（用于绘制曲线）
     */
    @GetMapping("/statistics/{eventType}")
    public Result getEventStatistics(@PathVariable String eventType, 
                                    @RequestParam(required = false) Integer patientId) {
        TimelineStatisticsDTO statistics = interventionTimeService.getEventStatistics(eventType, patientId);
        return Result.ok(statistics);
    }
}
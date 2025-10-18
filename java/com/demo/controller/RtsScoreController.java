package com.demo.controller;

import com.demo.Service.IRtsScoreService;
import com.demo.entity.RtsScore;
import com.demo.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * RTS评分控制器
 */
@RestController
@RequestMapping("/api/rts")
@CrossOrigin(origins = "*")
public class RtsScoreController {

    @Autowired
    private IRtsScoreService rtsScoreService;

    /**
     * 根据患者ID获取RTS评分
     * @param patientId 患者ID
     * @return RTS评分信息
     */
    @GetMapping("/score/{patientId}")
    public Result getRtsScoreByPatientId(@PathVariable Integer patientId) {
        try {
            RtsScore rtsScore = rtsScoreService.getRtsScoreByPatientId(patientId);
            if (rtsScore != null) {
                return Result.success(rtsScore);
            } else {
                return Result.error("未找到该患者的RTS评分信息");
            }
        } catch (Exception e) {
            return Result.error("获取RTS评分失败: " + e.getMessage());
        }
    }
}
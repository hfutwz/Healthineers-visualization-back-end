package com.demo.controller;

import com.demo.Service.IRtsScoreService;
import com.demo.entity.RtsScore;
import com.demo.dto.RtsScoreWithPatientDTO;
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
     * 根据患者ID获取RTS评分（包含患者基本信息）
     * @param patientId 患者ID
     * @return RTS评分及患者信息
     */
    @GetMapping("/score/{patientId}")
    public Result getRtsScoreByPatientId(@PathVariable Integer patientId) {
        try {
            RtsScoreWithPatientDTO rtsScore = rtsScoreService.getRtsScoreWithPatientInfo(patientId);
            if (rtsScore != null) {
                return Result.success(rtsScore);
            } else {
                // 如果新方法返回null，尝试使用旧方法
                RtsScore oldRtsScore = rtsScoreService.getRtsScoreByPatientId(patientId);
                if (oldRtsScore != null) {
                    return Result.success(oldRtsScore);
                } else {
                    return Result.error("未找到该患者的RTS评分信息");
                }
            }
        } catch (Exception e) {
            return Result.error("获取RTS评分失败: " + e.getMessage());
        }
    }
}
package com.demo.controller;

import com.demo.Service.IGcsScoreService;
import com.demo.entity.GcsScore;
import com.demo.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * GCS评分控制器
 */
@RestController
@RequestMapping("/api/gcs")
@CrossOrigin(origins = "*")
public class GcsScoreController {

    @Autowired
    private IGcsScoreService gcsScoreService;

    /**
     * 根据患者ID获取GCS评分
     * @param patientId 患者ID
     * @return GCS评分信息
     */
    @GetMapping("/score/{patientId}")
    public Result getGcsScoreByPatientId(@PathVariable Integer patientId) {
        try {
            GcsScore gcsScore = gcsScoreService.getGcsScoreByPatientId(patientId);
            if (gcsScore != null) {
                return Result.success(gcsScore);
            } else {
                return Result.error("未找到该患者的GCS评分信息");
            }
        } catch (Exception e) {
            return Result.error("获取GCS评分失败: " + e.getMessage());
        }
    }
}

package com.demo.controller;

import com.demo.Service.impl.IInjuryRecordService;
import com.demo.dto.AddressCountDTO;
import com.demo.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@Slf4j
public class InjuryRecordController {
    @Autowired
    private IInjuryRecordService injuryRecordService;

    /**
     * 查看系统中全部时间的地点
     * @return
     */
    @GetMapping("/alltime")
    public Object getAllLocations() {
        List<AddressCountDTO> allLocations = injuryRecordService.getAllLocations();
        return Result.ok(allLocations);
    }

    /**
     * 根据日期查询地点
     *      * 前端请求
     *      * GET /api/map/location-filtered
     *      * {
     *      *   "startDate": "2025-09-01", // 必须传递
     *      *   "endDate": "2025-09-30",   // 必须传递
     *      *   "timePeriods": [0,1,2],  // 可选，空列表 为全天
     *      * }
     * @param startDate
     * @param endDate
     * @param timePeriods
     * @return
     */
    @GetMapping("/location-filtered")
    public Result getLocationsByTimeRange(@RequestParam String startDate,
                                          @RequestParam String endDate,
                                          @RequestParam(required = false) List<Integer> timePeriods) {
        log.error("seasons = " + startDate.toString() + ",    timePeriods = " + endDate.toString());
        return Result.ok(injuryRecordService.getLocationsByTimeRange(startDate, endDate, timePeriods));
    }

    /**
     * 根据季节和时间段查询地点
     *      * 前端请求
     *      * GET /api/map/season-time-filtered
     *      * {
     *      *   "seasons": [0,2],   //  必须传递，0：春 2：秋
     *      *   "timePeriods": [3], // // 可选，空列表 为全天
     *      * }
     * @param seasons
     * @param timePeriods
     * @return
     */
    @GetMapping("/season-time-filtered")
    public Result getLocationsBySeasonsAndTime(@RequestParam List<Integer> seasons,
                                               @RequestParam(required = false) List<Integer> timePeriods) {
        // 打印接受的请求参数
        log.error("seasons = " + seasons.toString() + ",    timePeriods = " + timePeriods.toString());
        // 参数校验省略
        return Result.ok(injuryRecordService.getLocationsBySeasonsAndTime(seasons, timePeriods));
    }
}

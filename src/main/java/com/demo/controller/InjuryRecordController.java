package com.demo.controller;

import com.demo.Service.impl.IInjuryRecordService;
import com.demo.dto.AddressCountDTO;
import com.demo.dto.HourlyStatisticsDTO;
import com.demo.dto.HourlyGroupDTO;
import com.demo.dto.HourlyGroupStatisticsDTO;
import com.demo.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@CrossOrigin(origins = "*")
@Slf4j
public class InjuryRecordController {
    @Autowired
    private IInjuryRecordService injuryRecordService;
    /**
     * 根据季节和时间段查询地点
     *      * 前端请求
     *      * GET /api/map/locations
     *      * {
     *      *   "seasons": [0,2],       //  可选，0：春 2：秋
     *      *   "timePeriods": [3],     // 可选，空列表 为全天
     *      * }
     * @param seasons
     * @param timePeriods
     * @return
     */
    @GetMapping("/locations")
    public Result getLocations(@RequestParam(required = false) Integer[] seasons,
                               @RequestParam(required = false, name = "season") Integer season,
                               @RequestParam(required = false) Integer[] timePeriods,
                               @RequestParam(required = false) Integer[] years) {
        // 归一化参数：数组、多值与单值均支持
        java.util.List<Integer> seasonList = new java.util.ArrayList<>();
        if (seasons != null) {
            for (Integer s : seasons) { if (s != null) seasonList.add(s); }
        }
        if (season != null) {
            seasonList.add(season);
        }

        java.util.List<Integer> timePeriodList = new java.util.ArrayList<>();
        if (timePeriods != null) {
            for (Integer tp : timePeriods) { if (tp != null) timePeriodList.add(tp); }
        }

        java.util.List<Integer> yearList = new java.util.ArrayList<>();
        if (years != null) {
            for (Integer y : years) { if (y != null) yearList.add(y); }
        }

        log.info("/api/map/locations params -> seasons={}, timePeriods={}, years={}", seasonList, timePeriodList, yearList);

        List<AddressCountDTO> locations = injuryRecordService.getLocationsBySeasonsAndTime(
                seasonList.isEmpty() ? null : seasonList,
                timePeriodList.isEmpty() ? null : timePeriodList,
                yearList.isEmpty() ? null : yearList
        );
        return Result.ok(locations);
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
//    /**
//     * 查看系统中全部时间的地点
//     * @return
//     */
//    @GetMapping("/alltime")
//    public Object getAllLocations() {
//        List<AddressCountDTO> allLocations = injuryRecordService.getAllLocations();
//        return Result.ok(allLocations);
//    }

//    /**
//     * 根据季节和时间段查询地点
//     *      * 前端请求
//     *      * GET /api/map/season-time-filtered
//     *      * {
//     *      *   "seasons": [0,2],   //  必须传递，0：春 2：秋
//     *      *   "timePeriods": [3], // // 可选，空列表 为全天
//     *      * }
//     * @param seasons
//     * @param timePeriods
//     * @return
//     */
//    @GetMapping("/season-time-filtered")
//    public Result getLocationsBySeasonsAndTime(@RequestParam List<Integer> seasons,
//                                               @RequestParam(required = false) List<Integer> timePeriods) {
//        // 打印接受的请求参数
//        log.error("seasons = " + seasons.toString() + ",    timePeriods = " + timePeriods.toString());
//        // 参数校验省略
//        return Result.ok(injuryRecordService.getLocationsBySeasonsAndTime(seasons, timePeriods));
//    }

    /**
     * 获取24小时病例统计
     * @param year 年份（可选，可以是 "all" 表示查询所有年份）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @return 24小时统计数据
     */
    @GetMapping("/hourly-statistics")
    public Result getHourlyStatistics(@RequestParam(required = false) String year,
                                      @RequestParam(required = false) List<Integer> seasons,
                                      @RequestParam(required = false, name = "season") Integer season,
                                      @RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate) {
        // 处理年份参数：将 "all" 或 "NaN" 转换为 null
        Integer yearInt = null;
        if (year != null && !year.equals("all") && !year.isEmpty() && !year.equalsIgnoreCase("NaN")) {
            try {
                yearInt = Integer.parseInt(year);
            } catch (NumberFormatException e) {
                // 如果转换失败，保持为null
                log.warn("Invalid year parameter: {}", year);
            }
        }
        
        // 统一单选/多选入参：如果传了单个 season，合并到 seasons 列表中
        if (season != null) {
            if (seasons == null || seasons.isEmpty()) {
                seasons = new java.util.ArrayList<>();
            }
            seasons.add(season);
        }
        List<HourlyStatisticsDTO> statistics = injuryRecordService.getHourlyStatistics(yearInt, seasons, startDate, endDate);
        return Result.ok(statistics);
    }

    /**
     * 可用年份下拉
     */
    @GetMapping("/years")
    public Result getAvailableYears() {
        return Result.ok(injuryRecordService.getAvailableYears());
    }

    /**
     * 根据时间段分组查询患者数量
     * @param year 年份（可选，可以是 "all" 表示查询所有年份）
     * @param seasons 季节列表（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param requestBody 请求体，包含groups字段（时间段分组列表）
     * @return 分组统计结果列表
     */
    @PostMapping("/hourly-statistics-by-groups")
    public Result getHourlyStatisticsByGroups(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) List<Integer> seasons,
            @RequestParam(required = false, name = "season") Integer season,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestBody(required = false) HourlyGroupRequest requestBody) {
        
        // 处理年份参数：将 "all" 或 "NaN" 转换为 null
        Integer yearInt = null;
        if (year != null && !year.equals("all") && !year.isEmpty() && !year.equalsIgnoreCase("NaN")) {
            try {
                yearInt = Integer.parseInt(year);
            } catch (NumberFormatException e) {
                // 如果转换失败，保持为null
                log.warn("Invalid year parameter: {}", year);
            }
        }
        
        log.info("收到请求: /api/map/hourly-statistics-by-groups, year={}, seasons={}, season={}, startDate={}, endDate={}, requestBody={}", 
                yearInt, seasons, season, startDate, endDate, requestBody);
        
        // 统一单选/多选入参：如果传了单个 season，合并到 seasons 列表中
        if (season != null) {
            if (seasons == null || seasons.isEmpty()) {
                seasons = new java.util.ArrayList<>();
            }
            seasons.add(season);
        }
        
        // 获取分组信息
        List<HourlyGroupDTO> groups = null;
        if (requestBody != null && requestBody.getGroups() != null) {
            groups = requestBody.getGroups();
        }
        
        log.info("解析后的参数: year={}, seasons={}, startDate={}, endDate={}, groups={}", 
                yearInt, seasons, startDate, endDate, groups);
        
        if (groups == null || groups.isEmpty()) {
            log.warn("时间段分组信息为空");
            return Result.fail("时间段分组信息不能为空");
        }
        
        List<HourlyGroupStatisticsDTO> statistics = injuryRecordService.getHourlyStatisticsByGroups(
                yearInt, seasons, startDate, endDate, groups);
        log.info("查询结果: 返回{}条统计数据", statistics != null ? statistics.size() : 0);
        return Result.ok(statistics);
    }

    /**
     * 请求体类，用于接收分组信息
     */
    public static class HourlyGroupRequest {
        private List<HourlyGroupDTO> groups;

        public List<HourlyGroupDTO> getGroups() {
            return groups;
        }

        public void setGroups(List<HourlyGroupDTO> groups) {
            this.groups = groups;
        }
    }

}

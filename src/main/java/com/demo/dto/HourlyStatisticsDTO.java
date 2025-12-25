package com.demo.dto;

import lombok.Data;

/**
 * 24小时统计DTO
 */
@Data
public class HourlyStatisticsDTO {
    private Integer hour;        // 小时 (0-23)
    private Integer count;       // 病例数量
    private String timeLabel;    // 时间标签 (如 "00:00-01:00")

    public HourlyStatisticsDTO() {}

    public HourlyStatisticsDTO(Integer hour, Integer count) {
        this.hour = hour;
        this.count = count;
    }

    public String getTimeLabel() {
        if (timeLabel != null) {
            return timeLabel;
        }
        if (hour == null) {
            return null;
        }
        int nextHour = (hour + 1) % 24;
        return String.format("%02d:00-%02d:00", hour, nextHour);
    }
}

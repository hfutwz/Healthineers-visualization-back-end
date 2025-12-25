package com.demo;

import com.demo.Service.impl.IInterventionTimeService;
import com.demo.dto.TimelineEventDTO;
import com.demo.dto.TimelineStatisticsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class InterventionTimelineTest {

    @Autowired
    private IInterventionTimeService interventionTimeService;

    @Test
    public void testGetKeyEvents() {
        // 测试获取关键事件
        List<TimelineEventDTO> keyEvents = interventionTimeService.getKeyEvents(1);
        System.out.println("关键事件数量: " + keyEvents.size());
        keyEvents.forEach(event -> {
            System.out.println("事件: " + event.getEventName() + 
                             ", 时间: " + event.getEventTime() + 
                             ", 类型: " + event.getEventType());
        });
    }

    @Test
    public void testGetNonKeyEvents() {
        // 测试获取非关键事件
        List<TimelineEventDTO> nonKeyEvents = interventionTimeService.getNonKeyEvents(1);
        System.out.println("非关键事件数量: " + nonKeyEvents.size());
        nonKeyEvents.forEach(event -> {
            System.out.println("事件: " + event.getEventName() + 
                             ", 时间: " + event.getEventTime() + 
                             ", 类型: " + event.getEventType());
        });
    }

    @Test
    public void testGetEventStatistics() {
        // 测试获取统计信息
        TimelineStatisticsDTO statistics = interventionTimeService.getEventStatistics("treatment");
        System.out.println("事件类型: " + statistics.getEventType());
        System.out.println("平均时间: " + statistics.getMeanTime());
        System.out.println("中位时间: " + statistics.getMedianTime());
        System.out.println("标准差: " + statistics.getStandardDeviation());
        System.out.println("当前患者时间: " + statistics.getCurrentPatientTime());
        System.out.println("质控标准线: " + statistics.getQualityControlLine());
    }
}

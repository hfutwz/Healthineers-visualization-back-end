package com.demo;

import com.demo.Service.impl.IInterventionTimeService;
import com.demo.dto.TimelineEventDTO;
import com.demo.dto.TimelineStatisticsDTO;
import com.demo.entity.InterventionTime;
import com.demo.mapper.InterventionTimeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class InterventionTimelineIntegrationTest {

    @Autowired
    private IInterventionTimeService interventionTimeService;
    
    @Autowired
    private InterventionTimeMapper interventionTimeMapper;

    @Test
    public void testCreateSampleData() {
        // 创建测试数据
        InterventionTime sampleData = new InterventionTime();
        sampleData.setPatientId(3015);
        sampleData.setAdmissionDate(LocalDate.of(2024, 1, 15));
        sampleData.setAdmissionTime("1450"); // 14:50
        sampleData.setCT("1500"); // 15:00
        sampleData.setEndotrachealTube("1505"); // 15:05
        sampleData.setTransfusionStart("1505"); // 15:05
        sampleData.setTransfusionEnd("1520"); // 15:20
        sampleData.setLeaveSurgeryDate(LocalDate.of(2024, 1, 15));
        sampleData.setLeaveSurgeryTime("1534"); // 15:34
        sampleData.setPeripheral("1455"); // 14:55
        sampleData.setIvLine("1500"); // 15:00
        sampleData.setNasalPipe("1455"); // 14:55
        sampleData.setFaceMask("1500"); // 15:00
        sampleData.setVentilator("1505"); // 15:05
        sampleData.setCprStartTime("1500"); // 15:00
        sampleData.setCprEndTime("1505"); // 15:05
        sampleData.setUltrasound("1502"); // 15:02
        sampleData.setTourniquet("1500"); // 15:00
        sampleData.setBloodDraw("1500"); // 15:00
        sampleData.setCatheter("1500"); // 15:00
        sampleData.setGastricTube("1500"); // 15:00
        
        // 保存测试数据
        interventionTimeMapper.insert(sampleData);
        
        System.out.println("测试数据创建成功");
    }

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
        TimelineStatisticsDTO statistics = interventionTimeService.getEventStatistics("ct");
        System.out.println("事件类型: " + statistics.getEventType());
        System.out.println("平均时间: " + statistics.getMeanTime());
        System.out.println("中位时间: " + statistics.getMedianTime());
        System.out.println("标准差: " + statistics.getStandardDeviation());
        System.out.println("当前患者时间: " + statistics.getCurrentPatientTime());
        System.out.println("质控标准线: " + statistics.getQualityControlLine());
    }
}

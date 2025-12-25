package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.InterventionTime;
import com.demo.dto.TimelineEventDTO;
import com.demo.dto.TimelineStatisticsDTO;
import com.demo.dto.AllKeyEventsStatisticsDTO;
import java.util.List;

public interface IInterventionTimeService extends IService<InterventionTime> {
    List<InterventionTime> getByPatientId(Integer patientId);
    
    /**
     * 获取患者时间线事件
     */
    List<TimelineEventDTO> getTimelineEvents(Integer patientId);
    
    /**
     * 获取事件统计信息
     */
    TimelineStatisticsDTO getEventStatistics(String eventType);
    
    /**
     * 获取事件统计信息（支持指定当前患者ID）
     */
    TimelineStatisticsDTO getEventStatistics(String eventType, Integer currentPatientId);
    
    /**
     * 获取关键事件
     */
    List<TimelineEventDTO> getKeyEvents(Integer patientId);
    
    /**
     * 获取非关键事件
     */
    List<TimelineEventDTO> getNonKeyEvents(Integer patientId);
    
    /**
     * 获取所有关键事件的正态分布统计信息
     * 包括错误数据过滤和记录
     */
    AllKeyEventsStatisticsDTO getAllKeyEventsStatistics();
    
    /**
     * 获取所有关键事件的正态分布统计信息（支持指定当前患者ID）
     * 包括错误数据过滤和记录
     * @param currentPatientId 当前患者ID，如果传入则会在统计信息中包含该患者的时间
     */
    AllKeyEventsStatisticsDTO getAllKeyEventsStatistics(Integer currentPatientId);
    
    /**
     * 根据患者ID查询单条干预时间记录（用于编辑回显）
     * @param patientId 患者ID
     * @return 干预时间记录，如果不存在则返回null
     */
    InterventionTime getOneByPatientId(Integer patientId);
    
    /**
     * 更新干预时间记录
     * @param interventionTime 干预时间记录
     * @return 更新结果，true表示成功，false表示失败
     */
    boolean updateInterventionTime(InterventionTime interventionTime);
}


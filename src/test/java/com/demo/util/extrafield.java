package com.demo.util;

import com.demo.Service.impl.IInjuryRecordService;
import com.demo.entity.InjuryRecord;
import com.demo.mapper.InjuryRecordMapper;
import com.demo.utils.ExtraFieldUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class extrafield {
    @Autowired
    private InjuryRecordMapper injuryRecordMapper;
    @Autowired
    private IInjuryRecordService injuryRecordService;
    @Test
    public void test() throws InterruptedException {
        // 直接查询全部
        List<InjuryRecord> records = injuryRecordService.list();
        for (InjuryRecord record : records) {
//            // 计算季节
//            if (record.getAdmissionDate() != null) {
//                int season = ExtraFieldUtils.getSeason(record.getAdmissionDate());
//                record.setSeason(season);
//            }
//
//            // 计算时间段
//            if (record.getAdmissionTime() != null) {
//                Integer timePeriod = ExtraFieldUtils.getTimePeriod(record.getAdmissionTime());
//                if (timePeriod != null) {
//                    record.setTimePeriod(timePeriod);
//                }
//                // 否则跳过不设置
//            }
            // 获取地点描述
            String address = record.getInjuryLocationDesc();

            // 如果经纬度已存在则跳过不调用API
            if (record.getLongitude() == null && record.getLatitude() == null) {
                // 调用API获取
                double[] lngLat = ExtraFieldUtils.getLngLatFromAddress(address);
                if (lngLat != null) {
                    System.out.println("ID: " + record.getInjuryId() +
                            " 地址: " + address +
                            " 查询到经纬度： 经度=" + lngLat[0] +
                            " 纬度=" + lngLat[1]);
                    record.setLongitude(lngLat[0]);
                    record.setLatitude(lngLat[1]);
                } else {
                    System.out.println("ID: " + record.getInjuryId() +
                            " 地址: " + address +
                            " 未查询到有效经纬度");
                }
                // 更新此记录
                injuryRecordMapper.updateById(record);
                Thread.sleep(1000);
            }
        }
    }
}

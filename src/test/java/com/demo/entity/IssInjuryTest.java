package com.demo.entity;

import com.demo.Service.impl.IIssInjuryService;
import com.demo.dto.IssInjuryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class IssInjuryTest {
    @Autowired
    private IIssInjuryService injuryService;
    @Test
    public void testGetByPatientId() {
        Integer testPatientId = 1;  // 替换为存在的patientId，或确保此ID调用会返回数据
        IssInjuryDTO injury = injuryService.getInjuryDTOByPatientId(testPatientId);
        System.out.println("查询到的伤情信息：" + injury);
    }
}

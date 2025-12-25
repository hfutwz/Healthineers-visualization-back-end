package com.demo;

import com.demo.Service.impl.IPatientService;
import com.demo.dto.PatientPageDTO;
import com.demo.dto.PatientQueryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PatientPageTest {
    
    @Autowired
    private IPatientService patientService;
    
    @Test
    public void testPatientPage() {
        // 测试分页查询
        PatientQueryDTO queryDTO = new PatientQueryDTO();
        queryDTO.setCurrent(1);
        queryDTO.setSize(10);
        
        PatientPageDTO result = patientService.getPatientPage(queryDTO);
        System.out.println("总记录数: " + result.getTotal());
        System.out.println("当前页: " + result.getCurrent());
        System.out.println("每页大小: " + result.getSize());
        System.out.println("总页数: " + result.getPages());
        System.out.println("记录数: " + result.getRecords().size());
    }
    
    @Test
    public void testPatientPageWithFilter() {
        // 测试带筛选条件的分页查询
        PatientQueryDTO queryDTO = new PatientQueryDTO();
        queryDTO.setCurrent(1);
        queryDTO.setSize(5);
        queryDTO.setGender("男");
        queryDTO.setMinAge(20);
        queryDTO.setMaxAge(50);
        
        PatientPageDTO result = patientService.getPatientPage(queryDTO);
        System.out.println("筛选后总记录数: " + result.getTotal());
        System.out.println("筛选后记录数: " + result.getRecords().size());
    }
}

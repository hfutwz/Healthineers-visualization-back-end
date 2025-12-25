package com.demo.controller;

import com.demo.Service.impl.IPatientService;
import com.demo.dto.PatientPageDTO;
import com.demo.dto.PatientQueryDTO;
import com.demo.dto.PatientUpdateResultDTO;
import com.demo.dto.Result;
import com.demo.entity.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private IPatientService patientService;

    /**
     * 查询所有患者基本信息（保留原接口）
     */
    @GetMapping("/list")
    public Result listPatients() {
        List<Patient> patients = patientService.list();
        return Result.ok(patients);
    }

    /**
     * 分页查询患者信息
     */
    @PostMapping("/page")
    public Result getPatientPage(@RequestBody PatientQueryDTO queryDTO) {
        PatientPageDTO pageDTO = patientService.getPatientPage(queryDTO);
        return Result.ok(pageDTO);
    }

    /**
     * 分页查询患者信息（GET方式，用于简单查询）
     */
    @GetMapping("/page")
    public Result getPatientPageByGet(
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        PatientQueryDTO queryDTO = new PatientQueryDTO();
        queryDTO.setPatientId(patientId);
        queryDTO.setGender(gender);
        queryDTO.setMinAge(minAge);
        queryDTO.setMaxAge(maxAge);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        
        PatientPageDTO pageDTO = patientService.getPatientPage(queryDTO);
        return Result.ok(pageDTO);
    }

    /**
     * 删除患者及其所有相关数据
     * @param patientId 患者ID
     * @return 删除结果
     */
    @DeleteMapping("/{patientId}")
    public Result deletePatient(@PathVariable Integer patientId) {
        if (patientId == null) {
            return Result.fail("患者ID不能为空");
        }
        
        try {
            boolean success = patientService.deletePatientById(patientId);
            if (success) {
                return Result.ok("删除成功");
            } else {
                return Result.fail("删除失败：患者不存在或已被删除");
            }
        } catch (Exception e) {
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新患者基本信息
     * @param patient 患者信息
     * @return 更新结果
     */
    @RequestMapping(value = "/update", method = {RequestMethod.POST, RequestMethod.PUT})
    @CrossOrigin(origins = "*")
    public Result updatePatient(@RequestBody Patient patient) {
        try {
            // 调用 Service 层处理业务逻辑，返回封装好的结果DTO
            PatientUpdateResultDTO resultDTO = patientService.updatePatient(patient);
            
            // 根据 Service 返回的结果构建响应
            // 直接返回整个 DTO，包含 success、message 和 patient 信息
            if (resultDTO.getSuccess()) {
                return Result.ok(resultDTO);
            } else {
                return Result.fail(resultDTO.getMessage());
            }
        } catch (Exception e) {
            return Result.fail("更新失败：" + e.getMessage());
        }
    }
}

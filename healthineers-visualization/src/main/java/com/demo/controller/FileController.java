package com.demo.controller;

import com.demo.Service.impl.IInjuryRecordService;
import com.demo.Service.impl.IPatientService;
import com.demo.dto.Result;
import com.demo.entity.InjuryRecord;
import com.demo.entity.Patient;
import com.demo.utils.ExcelImportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/filed")
public class FileController {
    @Autowired
    private IPatientService patientService;
    @Autowired
    private IInjuryRecordService injuryRecordService;
    @PostMapping("/uploadInjuryRecordExcel")
    public Result uploadInjuryRecordExcel(@RequestParam("file") MultipartFile file) {
        try {
            ExcelImportUtil.importExcelToDb(file, injuryRecordService, InjuryRecord.class);
            return Result.ok("导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("导入失败：" + e.getMessage());
        }
    }
    @PostMapping("/uploadPatientExcel")
    public Result uploadPatientExcel(@RequestParam("file") MultipartFile file) {
        try {
            ExcelImportUtil.importExcelToDb(file, patientService, Patient.class);
            return Result.ok("导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("导入失败：" + e.getMessage());
        }
    }
}

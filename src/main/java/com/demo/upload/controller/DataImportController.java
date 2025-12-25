package com.demo.upload.controller;

import com.demo.dto.Result;
import com.demo.upload.service.InjuryRecordImportService;
import com.demo.upload.service.InterventionTimeImportService;
import com.demo.upload.service.PatientDataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.demo.upload.dto.ValidationErrorDTO;
import com.demo.upload.dto.ValidationResultDTO;

/**
 * 数据导入控制器
 */
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class DataImportController {
    
    private static final Logger logger = LoggerFactory.getLogger(DataImportController.class);
    
    // 文件上传目录
    private static final String UPLOAD_DIR = "uploads/";
    
    @Autowired
    private PatientDataImportService patientDataImportService;
    
    @Autowired
    private InjuryRecordImportService injuryRecordImportService;
    
    @Autowired
    private com.demo.upload.service.IssPatientInjurySeverityImportService issPatientInjurySeverityImportService;
    
    @Autowired
    private InterventionTimeImportService interventionTimeImportService;
    
    @Autowired
    private com.demo.upload.service.GcsScoreImportService gcsScoreImportService;
    
    @Autowired
    private com.demo.upload.service.RtsScoreImportService rtsScoreImportService;
    
    @Autowired
    private com.demo.upload.service.PatientInfoOnAdmissionImportService patientInfoOnAdmissionImportService;
    
    @Autowired
    private com.demo.upload.service.PatientInfoOffAdmissionImportService patientInfoOffAdmissionImportService;
    
    @Autowired
    private com.demo.upload.service.InterventionExtraImportService interventionExtraImportService;
    
    /**
     * 上传文件（仅保存文件，不执行导入）
     * 
     * @param file 上传的Excel文件
     * @return 文件路径
     */
    @PostMapping("/upload")
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String savedFilePath = saveUploadedFile(file);
            logger.info("文件已保存: {}", savedFilePath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("filePath", savedFilePath);
            result.put("fileName", file.getOriginalFilename());
            
            return Result.ok(result);
            
        } catch (Exception e) {
            logger.error("上传文件时发生异常", e);
            return Result.fail("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入患者数据（验证并导入一步完成）
     * 先扫描所有数据行进行验证，如果有错误则不导入数据库并返回错误信息
     * 如果没有错误则导入数据库并返回成功信息和成功记录条数
     * 
     * @param filePath 已上传的文件路径
     * @return 验证和导入结果
     */
    @PostMapping("/import/patient")
    public Result importPatientData(@RequestParam("filePath") String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return Result.fail("请提供文件路径");
            }
            
            logger.info("开始导入患者数据，文件路径: {}", filePath);
            
            // 执行验证并导入（Service层会先验证，全部通过才插入）
            Map<String, Object> result = patientDataImportService.validateAndImportPatientData(filePath);
            attachErrorExportIfAny(result);
            return Result.ok(result);
            
        } catch (Exception e) {
            logger.error("导入患者数据时发生异常", e);
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入创伤病例数据（验证并导入一步完成）
     * 先扫描所有数据行进行验证，如果有错误则不导入数据库并返回错误信息
     * 如果没有错误则导入数据库并返回成功信息和成功记录条数
     * 
     * @param filePath 已上传的文件路径
     * @return 验证和导入结果
     */
    @PostMapping("/import/injury")
    public Result importInjuryRecordData(@RequestParam("filePath") String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return Result.fail("请提供文件路径");
            }
            
            logger.info("开始导入创伤病例数据，文件路径: {}", filePath);
            
            // 执行验证并导入（Service层会先验证，全部通过才插入）
            Map<String, Object> result = injuryRecordImportService.validateAndImportInjuryRecordData(filePath);
            attachErrorExportIfAny(result);
            return Result.ok(result);
            
        } catch (Exception e) {
            logger.error("导入创伤病例数据时发生异常", e);
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入ISS数据（验证并导入一步完成）
     * 先扫描所有数据行进行验证，如果有错误则不导入数据库并返回错误信息
     * 如果没有错误则导入数据库并返回成功信息和成功记录条数
     * 
     * @param filePath 已上传的文件路径
     * @return 验证和导入结果
     */
    @PostMapping("/import/iss")
    public Result importIssData(@RequestParam("filePath") String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return Result.fail("请提供文件路径");
            }
            
            logger.info("开始导入ISS数据，文件路径: {}", filePath);
            
            // 执行验证并导入（Service层会先验证，全部通过才插入）
            Map<String, Object> result = issPatientInjurySeverityImportService.validateAndImportIssData(filePath);
            attachErrorExportIfAny(result);
            return Result.ok(result);
            
        } catch (Exception e) {
            logger.error("导入ISS数据时发生异常", e);
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入干预时间数据（验证并导入一步完成）
     * 先扫描所有数据行进行验证，如果有错误则不导入数据库并返回错误信息
     * 如果没有错误则导入数据库并返回成功信息和成功记录条数
     * 
     * @param filePath 已上传的文件路径
     * @return 验证和导入结果
     */
    @PostMapping("/import/intervention-time")
    public Result importInterventionTimeData(@RequestParam("filePath") String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return Result.fail("请提供文件路径");
            }
            
            logger.info("开始导入干预时间数据，文件路径: {}", filePath);
            
            // 执行验证并导入（Service层会先验证，全部通过才插入）
            Map<String, Object> result = interventionTimeImportService.validateAndImportInterventionTimeData(filePath);
            attachErrorExportIfAny(result);
            return Result.ok(result);
            
        } catch (Exception e) {
            logger.error("导入干预时间数据时发生异常", e);
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入GCS评分数据（验证并导入一步完成）
     * 先扫描所有数据行进行验证，如果有错误则不导入数据库并返回错误信息
     * 如果没有错误则导入数据库并返回成功信息和成功记录条数
     * 
     * @param filePath 已上传的文件路径
     * @return 验证和导入结果
     */
    @PostMapping("/import/gcs-score")
    public Result importGcsScoreData(@RequestParam("filePath") String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return Result.fail("请提供文件路径");
            }
            
            logger.info("开始导入GCS评分数据，文件路径: {}", filePath);
            
            // 执行验证并导入（Service层会先验证，全部通过才插入）
            Map<String, Object> result = gcsScoreImportService.validateAndImportGcsScoreData(filePath);
            attachErrorExportIfAny(result);
            return Result.ok(result);
            
        } catch (Exception e) {
            logger.error("导入GCS评分数据时发生异常", e);
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入RTS评分数据（验证并导入一步完成）
     * 先扫描所有数据行进行验证，如果有错误则不导入数据库并返回错误信息
     * 如果没有错误则导入数据库并返回成功信息和成功记录条数
     * 
     * @param filePath 已上传的文件路径
     * @return 验证和导入结果
     */
    @PostMapping("/import/rts-score")
    public Result importRtsScoreData(@RequestParam("filePath") String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return Result.fail("请提供文件路径");
            }
            
            logger.info("开始导入RTS评分数据，文件路径: {}", filePath);
            
            // 执行验证并导入（Service层会先验证，全部通过才插入）
            Map<String, Object> result = rtsScoreImportService.validateAndImportRtsScoreData(filePath);
            attachErrorExportIfAny(result);
            return Result.ok(result);
            
        } catch (Exception e) {
            logger.error("导入RTS评分数据时发生异常", e);
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入患者入室信息数据（验证并导入一步完成）
     * 先扫描所有数据行进行验证，如果有错误则不导入数据库并返回错误信息
     * 如果没有错误则导入数据库并返回成功信息和成功记录条数
     * 
     * @param filePath 已上传的文件路径
     * @return 验证和导入结果
     */
    @PostMapping("/import/patient-info-on-admission")
    public Result importPatientInfoOnAdmissionData(@RequestParam("filePath") String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return Result.fail("请提供文件路径");
            }
            
            logger.info("开始导入患者入室信息数据，文件路径: {}", filePath);
            
            // 执行验证并导入（Service层会先验证，全部通过才插入）
            Map<String, Object> result = patientInfoOnAdmissionImportService.validateAndImportPatientInfoOnAdmissionData(filePath);
            attachErrorExportIfAny(result);
            return Result.ok(result);
            
        } catch (Exception e) {
            logger.error("导入患者入室信息数据时发生异常", e);
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入患者离室信息数据（验证并导入一步完成）
     * 先扫描所有数据行进行验证，如果有错误则不导入数据库并返回错误信息
     * 如果没有错误则导入数据库并返回成功信息和成功记录条数
     * 
     * @param filePath 已上传的文件路径
     * @return 验证和导入结果
     */
    @PostMapping("/import/patient-info-off-admission")
    public Result importPatientInfoOffAdmissionData(@RequestParam("filePath") String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return Result.fail("请提供文件路径");
            }
            
            logger.info("开始导入患者离室信息数据，文件路径: {}", filePath);
            
            // 执行验证并导入（Service层会先验证，全部通过才插入）
            Map<String, Object> result = patientInfoOffAdmissionImportService.validateAndImportPatientInfoOffAdmissionData(filePath);
            attachErrorExportIfAny(result);
            return Result.ok(result);
            
        } catch (Exception e) {
            logger.error("导入患者离室信息数据时发生异常", e);
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入干预补充数据（验证并导入一步完成）
     * 先扫描所有数据行进行验证，如果有错误则不导入数据库并返回错误信息
     * 如果没有错误则导入数据库并返回成功信息和成功记录条数
     * 
     * @param filePath 已上传的文件路径
     * @return 验证和导入结果
     */
    @PostMapping("/import/intervention-extra")
    public Result importInterventionExtraData(@RequestParam("filePath") String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return Result.fail("请提供文件路径");
            }
            
            logger.info("开始导入干预补充数据，文件路径: {}", filePath);
            
            // 执行验证并导入（Service层会先验证，全部通过才插入）
            Map<String, Object> result = interventionExtraImportService.validateAndImportInterventionExtraData(filePath);
            attachErrorExportIfAny(result);
            return Result.ok(result);
            
        } catch (Exception e) {
            logger.error("导入干预补充数据时发生异常", e);
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存上传的文件到服务器
     * 
     * @param file 上传的文件
     * @return 保存后的文件路径
     * @throws IOException
     */
    private String saveUploadedFile(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // 保存文件
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);
        
        // 返回绝对路径
        return filePath.toAbsolutePath().toString();
    }

    /**
     * 如果存在验证错误，则生成错误CSV导出，并将导出信息附加到结果Map
     */
    @SuppressWarnings("unchecked")
    private void attachErrorExportIfAny(Map<String, Object> result) {
        if (result == null) return;
        Object validationObj = result.get("validation");
        try {
            List<ValidationErrorDTO> errors = null;
            if (validationObj instanceof Map) {
                Map<String, Object> validation = (Map<String, Object>) validationObj;
                Object errorsObj = validation.get("errors");
                if (errorsObj instanceof List) {
                    errors = (List<ValidationErrorDTO>) errorsObj;
                }
            } else if (validationObj instanceof ValidationResultDTO) {
                errors = ((ValidationResultDTO) validationObj).getErrors();
            }
            if (errors != null && !errors.isEmpty()) {
                String fileName = writeErrorsCsv(errors);
                result.put("errorExportFile", fileName);
                result.put("errorExportUrl", "/api/file/downloadErrorReport?file=" + fileName);
            }
        } catch (Exception e) {
            logger.warn("附加错误导出信息失败: {}", e.getMessage());
        }
    }

    /**
     * 将错误列表写入CSV文件，返回生成的文件名
     */
    private String writeErrorsCsv(List<ValidationErrorDTO> errors) throws IOException {
        Path exportDir = Paths.get(UPLOAD_DIR, "error-exports");
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }
        String fileName = "import-errors-" + System.currentTimeMillis() + ".csv";
        Path csvPath = exportDir.resolve(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8)) {
            // 写入UTF-8 BOM，便于Excel正确识别编码
            writer.write('\uFEFF');
            // 表头
            writer.write("行号,患者ID,字段,错误值,错误原因");
            writer.newLine();
            if (errors != null) {
                for (ValidationErrorDTO err : errors) {
                    String row = err.getRow() == null ? "" : String.valueOf(err.getRow());
                    String patientId = err.getPatientId() == null ? "" : String.valueOf(err.getPatientId());
                    String field = safeCsv(err.getField());
                    String value = err.getValue() == null ? "" : safeCsv(String.valueOf(err.getValue()));
                    String message = safeCsv(err.getMessage());
                    writer.write(String.join(",", quote(row), quote(patientId), quote(field), quote(value), quote(message)));
                    writer.newLine();
                }
            }
        }
        return fileName;
    }

    private String safeCsv(String s) {
        if (s == null) return "";
        return s.replace("\r", " ").replace("\n", " ").trim();
    }

    private String quote(String s) {
        if (s == null) return "\"\"";
        boolean needQuote = s.contains(",") || s.contains("\"");
        String escaped = s.replace("\"", "\"\"");
        return needQuote ? "\"" + escaped + "\"" : escaped;
    }
}


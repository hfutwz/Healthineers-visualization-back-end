package com.demo.upload.service;

import com.demo.upload.dto.ImportResultDTO;
import com.demo.upload.dto.ValidationErrorDTO;
import com.demo.upload.dto.ValidationResultDTO;
import com.demo.upload.exception.DataValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 综合数据导入服务
 * 整合所有9张表的验证和导入逻辑
 * 先验证所有表的数据，收集所有错误，只有全部通过才插入数据库
 */
@Service
public class ComprehensiveDataImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveDataImportService.class);
    
    @Autowired
    private PatientDataImportService patientDataImportService;
    
    @Autowired
    private InjuryRecordImportService injuryRecordImportService;
    
    @Autowired
    private GcsScoreImportService gcsScoreImportService;
    
    @Autowired
    private RtsScoreImportService rtsScoreImportService;
    
    @Autowired
    private PatientInfoOnAdmissionImportService patientInfoOnAdmissionImportService;
    
    @Autowired
    private PatientInfoOffAdmissionImportService patientInfoOffAdmissionImportService;
    
    @Autowired
    private InterventionTimeImportService interventionTimeImportService;
    
    @Autowired
    private InterventionExtraImportService interventionExtraImportService;
    
    @Autowired
    private IssPatientInjurySeverityImportService issPatientInjurySeverityImportService;
    
    /**
     * 表信息配置
     */
    private static class TableInfo {
        String name;
        String label;
        ImportService service;
        
        TableInfo(String name, String label, ImportService service) {
            this.name = name;
            this.label = label;
            this.service = service;
        }
    }
    
    /**
     * 导入服务接口
     */
    private interface ImportService {
        Map<String, Object> validateAndImport(String excelFilePath);
    }
    
    /**
     * 批量验证并导入所有表的数据
     * 1. 先验证所有表的数据（不插入数据库）
     * 2. 收集所有错误
     * 3. 如果全部通过，才插入数据库
     * 4. 返回所有表的验证结果
     * 
     * @param excelFilePath Excel文件路径
     * @return 包含所有表验证和导入结果的Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> validateAndImportAllTables(String excelFilePath) {
        logger.info("开始批量导入所有表的数据，文件路径: {}", excelFilePath);
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> tableResults = new LinkedHashMap<>();
        
        // 定义所有表的信息
        List<TableInfo> tables = Arrays.asList(
            new TableInfo("patient", "患者基本信息", excelFilePath1 -> patientDataImportService.validateAndImportPatientData(excelFilePath1)),
            new TableInfo("injury_record", "受伤记录", excelFilePath1 -> injuryRecordImportService.validateAndImportInjuryRecordData(excelFilePath1)),
            new TableInfo("gcs_score", "GCS评分", excelFilePath1 -> gcsScoreImportService.validateAndImportGcsScoreData(excelFilePath1)),
            new TableInfo("rts_score", "RTS评分", excelFilePath1 -> rtsScoreImportService.validateAndImportRtsScoreData(excelFilePath1)),
            new TableInfo("patient_info_on_admission", "患者入室信息", excelFilePath1 -> patientInfoOnAdmissionImportService.validateAndImportPatientInfoOnAdmissionData(excelFilePath1)),
            new TableInfo("patient_info_off_admission", "患者离室信息", excelFilePath1 -> patientInfoOffAdmissionImportService.validateAndImportPatientInfoOffAdmissionData(excelFilePath1)),
            new TableInfo("intervention_time", "干预时间", excelFilePath1 -> interventionTimeImportService.validateAndImportInterventionTimeData(excelFilePath1)),
            new TableInfo("intervention_extra", "干预补充数据", excelFilePath1 -> interventionExtraImportService.validateAndImportInterventionExtraData(excelFilePath1)),
            new TableInfo("iss", "ISS数据", excelFilePath1 -> issPatientInjurySeverityImportService.validateAndImportIssData(excelFilePath1))
        );
        
        // 第一步：验证所有表的数据（不插入数据库）
        // 由于现有的导入服务会在验证通过后立即插入数据库，我们需要通过事务控制
        // 如果验证失败，事务会回滚，不会插入数据
        logger.info("第一步：开始验证所有表的数据");
        boolean allValid = true;
        int totalErrorCount = 0;
        List<ValidationErrorDTO> allErrors = new ArrayList<>();
        
        // 存储每个表的验证结果（不插入数据库）
        Map<String, ValidationResultDTO> validationResults = new HashMap<>();
        
        for (TableInfo table : tables) {
            logger.info("正在验证表: {} ({})", table.name, table.label);
            
            Map<String, Object> tableResult = new HashMap<>();
            try {
                // 调用导入服务进行验证
                // 注意：由于导入服务会在验证通过后立即插入数据库，我们需要通过事务控制
                // 如果验证失败，会返回错误信息，不会插入数据库
                // 如果验证通过，会插入数据库，但如果后续表验证失败，整个事务会回滚
                Map<String, Object> importResult = table.service.validateAndImport(excelFilePath);
                
                // 提取验证结果
                ValidationResultDTO validationResult = (ValidationResultDTO) importResult.get("validation");
                ImportResultDTO importResultDTO = (ImportResultDTO) importResult.get("import");
                Boolean success = (Boolean) importResult.get("success");
                
                if (validationResult == null) {
                    validationResult = new ValidationResultDTO();
                    validationResult.setSuccess(false);
                    validationResult.setValid(false);
                    validationResult.setErrorCount(1);
                    validationResult.setMessage("验证失败：无法获取验证结果");
                    validationResult.setErrors(new ArrayList<>());
                }
                
                // 检查是否有错误
                boolean tableValid = validationResult.getValid() != null && validationResult.getValid();
                int errorCount = validationResult.getErrorCount() != null ? validationResult.getErrorCount() : 0;
                
                // 保存验证结果（不包含导入结果，因为如果验证失败，不应该有导入结果）
                validationResults.put(table.name, validationResult);
                
                if (!tableValid || errorCount > 0) {
                    allValid = false;
                    totalErrorCount += errorCount;
                    
                    // 为错误添加表名标识
                    if (validationResult.getErrors() != null) {
                        for (ValidationErrorDTO error : validationResult.getErrors()) {
                            // 在错误消息前添加表名
                            String originalMessage = error.getMessage() != null ? error.getMessage() : "";
                            error.setMessage("[" + table.label + "] " + originalMessage);
                            allErrors.add(error);
                        }
                    }
                    
                    // 如果验证失败，导入结果应该也是失败的
                    if (importResultDTO == null) {
                        importResultDTO = new ImportResultDTO();
                        importResultDTO.setSuccess(false);
                        importResultDTO.setMessage("验证失败，未导入数据");
                    }
                }
                
                tableResult.put("tableName", table.name);
                tableResult.put("tableLabel", table.label);
                tableResult.put("validation", validationResult);
                tableResult.put("import", importResultDTO);
                tableResult.put("success", success);
                tableResult.put("valid", tableValid);
                tableResult.put("errorCount", errorCount);
                
                logger.info("表 {} ({}) 验证完成: valid={}, errorCount={}", 
                    table.name, table.label, tableValid, errorCount);
                
            } catch (Exception e) {
                logger.error("验证表 {} ({}) 时发生异常", table.name, table.label, e);
                allValid = false;
                
                ValidationResultDTO validationResult = new ValidationResultDTO();
                validationResult.setSuccess(false);
                validationResult.setValid(false);
                validationResult.setErrorCount(1);
                validationResult.setMessage("验证异常: " + e.getMessage());
                
                ValidationErrorDTO error = new ValidationErrorDTO();
                error.setRow(0);
                error.setPatientId(0);
                error.setField("系统错误");
                error.setValue("");
                error.setMessage("[" + table.label + "] 验证时发生异常: " + e.getMessage());
                validationResult.setErrors(Arrays.asList(error));
                
                ImportResultDTO importResultDTO = new ImportResultDTO();
                importResultDTO.setSuccess(false);
                importResultDTO.setMessage("验证异常: " + e.getMessage());
                
                tableResult.put("tableName", table.name);
                tableResult.put("tableLabel", table.label);
                tableResult.put("validation", validationResult);
                tableResult.put("import", importResultDTO);
                tableResult.put("success", false);
                tableResult.put("valid", false);
                tableResult.put("errorCount", 1);
                
                validationResults.put(table.name, validationResult);
                allErrors.add(error);
                totalErrorCount += 1;
            }
            
            tableResults.put(table.name, tableResult);
        }
        
        // 第二步：如果所有表验证通过，数据已经插入数据库（因为导入服务在验证通过后会立即插入）
        // 如果验证失败，事务会回滚，不会插入任何数据
        if (!allValid || totalErrorCount > 0) {
            logger.warn("验证未通过，事务将回滚，不执行导入。总错误数: {}", totalErrorCount);
            
            // 构建失败结果
            result.put("success", false);
            result.put("allValid", false);
            result.put("totalErrorCount", totalErrorCount);
            result.put("allErrors", allErrors);
            result.put("tables", tableResults);
            result.put("message", String.format("验证失败：共发现 %d 个错误，数据未导入", totalErrorCount));
            
            logger.info("批量导入完成: success=false, totalErrorCount={}", totalErrorCount);
            
            // 抛出自定义异常以触发事务回滚（确保不插入任何数据），同时携带错误信息
            throw new DataValidationException(
                "数据验证失败，共发现 " + totalErrorCount + " 个错误，数据未导入",
                result
            );
        }
        
        logger.info("所有表验证通过，数据已成功导入数据库");
        
        // 构建最终结果
        result.put("success", true);
        result.put("allValid", true);
        result.put("totalErrorCount", 0);
        result.put("allErrors", new ArrayList<>());
        result.put("tables", tableResults);
        result.put("message", "所有表的数据验证通过并成功导入");
        
        logger.info("批量导入完成: success=true, totalErrorCount=0");
        
        return result;
    }

    // 写一个模拟方法
    public Map<String, Object> testGit(String excelFilePath) {
        // 返回一个模拟结果
        System.out.println("aaa");
        return new HashMap<String, Object>() {};
    }
    // 写一个模拟方法
    public Map<String, Object> testGit2(String excelFilePath) {
        // 返回一个模拟结果
        System.out.println("bbb");
        return new HashMap<String, Object>() {};
    }
}


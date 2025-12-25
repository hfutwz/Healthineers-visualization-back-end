package com.demo.upload.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.entity.InterventionExtra;
import com.demo.entity.Patient;
import com.demo.mapper.InterventionExtraMapper;
import com.demo.mapper.PatientMapper;
import com.demo.upload.constants.InterventionExtraColumnConstants;
import com.demo.upload.dto.ImportResultDTO;
import com.demo.upload.dto.ValidationErrorDTO;
import com.demo.upload.dto.ValidationResultDTO;
import com.demo.upload.validator.InterventionExtraFieldValidator;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 干预补充数据导入服务
 * 参考 intervention_extra_importer.py 的逻辑
 */
@Service
public class InterventionExtraImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(InterventionExtraImportService.class);
    
    @Autowired
    private InterventionExtraMapper interventionExtraMapper;
    
    @Autowired
    private InterventionExtraFieldValidator fieldValidator;
    
    @Autowired
    private PatientMapper patientMapper;
    
    /**
     * 验证并导入干预补充数据（一步完成）
     * 读取Excel全部数据，验证所有数据并记录所有错误
     * 错误数为0才插入数据库，否则返回所有错误信息
     * 
     * @param excelFilePath Excel文件路径
     * @return 包含验证结果和导入结果的Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> validateAndImportInterventionExtraData(String excelFilePath) {
        Map<String, Object> result = new HashMap<>();
        Workbook workbook = null;
        
        try {
            // 检查Excel文件是否存在
            File excelFile = new File(excelFilePath);
            if (!excelFile.exists()) {
                logger.error("Excel文件不存在: {}", excelFilePath);
                ValidationResultDTO validationResult = createErrorResult("Excel文件不存在: " + excelFilePath);
                ImportResultDTO importResult = createImportErrorResult("Excel文件不存在: " + excelFilePath);
                result.put("validation", validationResult);
                result.put("import", importResult);
                result.put("success", false);
                result.put("message", "Excel文件不存在: " + excelFilePath);
                return result;
            }
            
            // 使用Apache POI读取Excel文件
            workbook = WorkbookFactory.create(excelFile);
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet == null || sheet.getLastRowNum() < 0) {
                ValidationResultDTO validationResult = createErrorResult("Excel文件中没有数据");
                ImportResultDTO importResult = createImportErrorResult("Excel文件中没有数据");
                result.put("validation", validationResult);
                result.put("import", importResult);
                result.put("success", false);
                result.put("message", "Excel文件中没有数据");
                return result;
            }
            
            // 读取标题行（第0行是列名）
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                ValidationResultDTO validationResult = createErrorResult("Excel文件中没有标题行（第0行应为列名）");
                ImportResultDTO importResult = createImportErrorResult("Excel文件中没有标题行（第0行应为列名）");
                result.put("validation", validationResult);
                result.put("import", importResult);
                result.put("success", false);
                result.put("message", "Excel文件中没有标题行（第0行应为列名）");
                return result;
            }
            
            // 检查是否有数据行（第1行应该是第一条数据）
            if (sheet.getLastRowNum() < 1) {
                ValidationResultDTO validationResult = createErrorResult("Excel文件中没有数据行（第1行应为第一条数据）");
                ImportResultDTO importResult = createImportErrorResult("Excel文件中没有数据行（第1行应为第一条数据）");
                result.put("validation", validationResult);
                result.put("import", importResult);
                result.put("success", false);
                result.put("message", "Excel文件中没有数据行（第1行应为第一条数据）");
                return result;
            }
            
            // 构建列名到列索引的映射
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell).trim();
                    columnIndexMap.put(cellValue, cell.getColumnIndex());
                }
            }
            
            // 检查必需的列是否存在
            List<String> missingColumns = new ArrayList<>();
            for (String requiredColumn : InterventionExtraColumnConstants.REQUIRED_COLUMNS) {
                String trimmedRequiredColumn = requiredColumn.trim();
                if (!columnIndexMap.containsKey(trimmedRequiredColumn)) {
                    missingColumns.add(requiredColumn);
                }
            }
            
            if (!missingColumns.isEmpty()) {
                ValidationResultDTO validationResult = new ValidationResultDTO();
                validationResult.setSuccess(false);
                validationResult.setValid(false);
                validationResult.setErrorCount(1);
                validationResult.setMessage("缺少必需的列: " + String.join(", ", missingColumns));
                
                ValidationErrorDTO error = new ValidationErrorDTO();
                error.setRow(0);
                error.setPatientId(0);
                error.setField("Excel列");
                error.setValue("");
                error.setMessage("缺少必需的列: " + String.join(", ", missingColumns));
                
                List<ValidationErrorDTO> errors = new ArrayList<>();
                errors.add(error);
                validationResult.setErrors(errors);
                
                ImportResultDTO importResult = createImportErrorResult("缺少必需的列: " + String.join(", ", missingColumns));
                result.put("validation", validationResult);
                result.put("import", importResult);
                result.put("success", false);
                result.put("message", "缺少必需的列: " + String.join(", ", missingColumns));
                return result;
            }
            
            // 读取并验证所有数据行，记录所有错误
            List<ValidationErrorDTO> allErrors = new ArrayList<>();
            List<InterventionExtra> validRecords = new ArrayList<>();
            
            // 从第1行开始读取数据（索引从0开始：第0行是列名，第1行是第一条数据）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                int excelRowNumber = rowIndex + 1; // Excel行号（从1开始：第1行是列名，第2行是第一条数据）
                
                // 1. 验证患者ID（序号）
                Integer patientId = getCellValueAsInt(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.PATIENT_ID));
                if (patientId == null || patientId == 0) {
                    // 根据 Python 代码，如果 patient_id == 0，跳过该行
                    continue;
                }
                
                // 检查患者ID是否在患者基本信息表中存在
                LambdaQueryWrapper<Patient> patientQueryWrapper = new LambdaQueryWrapper<>();
                patientQueryWrapper.eq(Patient::getPatientId, patientId);
                Patient existingPatient = patientMapper.selectOne(patientQueryWrapper);
                if (existingPatient == null) {
                    allErrors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "序号",
                        patientId,
                        "患者序号 " + patientId + " 在患者基本信息表中不存在"
                    ));
                    continue;
                }
                
                // 2. 读取并清理各字段数据（参考 Python 代码）
                Float oxygenConcentration = fieldValidator.cleanPercentFloat(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.OXYGEN_CONCENTRATION)));
                String defibrillation = fieldValidator.cleanYesNo(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.DEFIBRILLATION)));
                String limbAmputation = fieldValidator.cleanYesNo(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.LIMB_AMPUTATION)));
                String transfusionReaction = fieldValidator.cleanText(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.TRANSFUSION_REACTION)));
                Float suspendedRedUnits = fieldValidator.cleanFloatFirstNumber(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.SUSPENDED_RED_UNITS)));
                Float plasmaUnits = fieldValidator.cleanFloatFirstNumber(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.PLASMA_UNITS)));
                Float plateletsAmount = fieldValidator.cleanFloatFirstNumber(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.PLATELETS_AMOUNT)));
                Float cryoprecipitateUnits = fieldValidator.cleanFloatFirstNumber(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.CRYOPRECIPITATE_UNITS)));
                String otherTransfusion = fieldValidator.cleanText(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.OTHER_TRANSFUSION)));
                String therapeuticOperation = fieldValidator.cleanText(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.THERAPEUTIC_OPERATION)));
                String consultationDept = fieldValidator.cleanText(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.CONSULTATION_DEPT)));
                String administrativeDept = fieldValidator.cleanText(getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionExtraColumnConstants.ADMINISTRATIVE_DEPT)));
                
                // 创建 InterventionExtra 对象
                InterventionExtra interventionExtra = new InterventionExtra();
                interventionExtra.setPatientId(patientId);
                interventionExtra.setOxygenConcentration(oxygenConcentration);
                interventionExtra.setDefibrillation(defibrillation);
                interventionExtra.setLimbAmputation(limbAmputation);
                interventionExtra.setTransfusionReaction(transfusionReaction);
                interventionExtra.setSuspendedRedUnits(suspendedRedUnits);
                interventionExtra.setPlasmaUnits(plasmaUnits);
                interventionExtra.setPlateletsAmount(plateletsAmount);
                interventionExtra.setCryoprecipitateUnits(cryoprecipitateUnits);
                interventionExtra.setOtherTransfusion(otherTransfusion);
                interventionExtra.setTherapeuticOperation(therapeuticOperation);
                interventionExtra.setConsultationDept(consultationDept);
                interventionExtra.setAdministrativeDept(administrativeDept);
                
                validRecords.add(interventionExtra);
            }
            
            // 构建验证结果
            ValidationResultDTO validationResult = new ValidationResultDTO();
            validationResult.setSuccess(allErrors.isEmpty());
            validationResult.setValid(allErrors.isEmpty());
            validationResult.setErrorCount(allErrors.size());
            validationResult.setErrors(allErrors);
            
            if (allErrors.isEmpty()) {
                validationResult.setMessage("数据验证通过，共 " + validRecords.size() + " 条有效记录");
            } else {
                validationResult.setMessage("数据验证失败，共发现 " + allErrors.size() + " 个错误");
            }
            
            // 如果验证失败，不插入数据库
            if (!allErrors.isEmpty()) {
                ImportResultDTO importResult = new ImportResultDTO();
                importResult.setSuccess(false);
                importResult.setStatus("failed");
                importResult.setSuccessCount(0);
                importResult.setInsertCount(0);
                importResult.setUpdateCount(0);
                importResult.setFailedCount(validRecords.size());
                importResult.setMessage("数据验证失败，未导入任何数据。共发现 " + allErrors.size() + " 个错误");
                
                result.put("validation", validationResult);
                result.put("import", importResult);
                result.put("success", false);
                result.put("message", "数据验证失败，未导入任何数据");
                return result;
            }
            
            // 验证通过，导入数据
            ImportResultDTO importResult = importInterventionExtraData(validRecords);
            
            result.put("validation", validationResult);
            result.put("import", importResult);
            result.put("success", importResult.getSuccess());
            result.put("message", importResult.getMessage());
            
            return result;
            
        } catch (Exception e) {
            logger.error("验证并导入数据时发生异常", e);
            ValidationResultDTO validationResult = createErrorResult("验证并导入数据时发生异常: " + e.getMessage());
            ImportResultDTO importResult = createImportErrorResult("验证并导入数据时发生异常: " + e.getMessage());
            result.put("validation", validationResult);
            result.put("import", importResult);
            result.put("success", false);
            result.put("message", "验证并导入数据时发生异常: " + e.getMessage());
            return result;
        } finally {
            // 关闭工作簿
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    logger.warn("关闭工作簿时发生异常", e);
                }
            }
        }
    }
    
    /**
     * 导入干预补充数据（纯Java实现）
     * 注意：此方法假设数据已经通过验证，直接插入数据库
     * 使用 ON DUPLICATE KEY UPDATE 逻辑（通过检查是否存在来实现）
     * 
     * @param interventionExtraList 已验证的干预补充记录列表
     * @return 导入结果
     */
    private ImportResultDTO importInterventionExtraData(List<InterventionExtra> interventionExtraList) {
        try {
            int insertCount = 0;  // 新插入的记录数
            int updateCount = 0;  // 更新的记录数
            int totalCount = interventionExtraList.size();
            
            if (!interventionExtraList.isEmpty()) {
                try {
                    // 使用批量插入方法（使用 ON DUPLICATE KEY UPDATE）
                    for (InterventionExtra record : interventionExtraList) {
                        // 检查是否已存在相同的 patient_id 记录
                        LambdaQueryWrapper<InterventionExtra> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(InterventionExtra::getPatientId, record.getPatientId());
                        List<InterventionExtra> existingList = interventionExtraMapper.selectList(queryWrapper);
                        
                        if (existingList != null && !existingList.isEmpty()) {
                            // 如果存在多条记录，抛出错误
                            if (existingList.size() > 1) {
                                throw new RuntimeException("重复了");
                            }
                            
                            // 更新现有记录（只有一条）
                            InterventionExtra existing = existingList.get(0);
                            record.setId(existing.getId());
                            interventionExtraMapper.updateById(record);
                            updateCount++;
                            logger.debug("更新已存在的干预补充记录，患者ID: {}", record.getPatientId());
                        } else {
                            // 插入新记录
                            interventionExtraMapper.insert(record);
                            insertCount++;
                            logger.debug("插入新的干预补充记录，患者ID: {}", record.getPatientId());
                        }
                    }
                    
                    logger.info("批量插入干预补充数据完成，新插入: {} 条，更新: {} 条", insertCount, updateCount);
                } catch (RuntimeException e) {
                    // 重新抛出RuntimeException，保持错误信息
                    throw e;
                } catch (Exception e) {
                    logger.error("批量插入干预补充数据时发生异常", e);
                    // 检查是否是TooManyResultsException
                    if (e.getMessage() != null && e.getMessage().contains("TooManyResultsException")) {
                        throw new RuntimeException("重复了", e);
                    }
                    throw new RuntimeException("批量插入数据时发生异常: " + e.getMessage(), e);
                }
            }
            
            // 构建导入结果
            ImportResultDTO result = new ImportResultDTO();
            int successCount = insertCount + updateCount;  // 总成功数 = 插入数 + 更新数
            int failedCount = totalCount - successCount;
            
            result.setSuccess(failedCount == 0);
            result.setSuccessCount(successCount);
            result.setInsertCount(insertCount);
            result.setUpdateCount(updateCount);
            result.setFailedCount(failedCount);
            result.setStatus(failedCount == 0 ? "success" : (successCount > 0 ? "partial" : "failed"));
            
            // 根据插入和更新的数量，生成更准确的消息
            if (failedCount == 0) {
                if (insertCount > 0 && updateCount > 0) {
                    result.setMessage("导入成功！共导入 " + successCount + " 条记录（新插入 " + insertCount + " 条，更新 " + updateCount + " 条）");
                } else if (insertCount > 0) {
                    result.setMessage("导入成功！共导入 " + insertCount + " 条新记录");
                } else if (updateCount > 0) {
                    result.setMessage("导入成功！共更新 " + updateCount + " 条记录");
                } else {
                    result.setMessage("导入完成，但没有数据需要导入");
                }
            } else {
                result.setMessage("导入部分成功：成功 " + successCount + " 条，失败 " + failedCount + " 条");
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("导入干预补充数据时发生异常", e);
            ImportResultDTO result = new ImportResultDTO();
            result.setSuccess(false);
            result.setStatus("failed");
            result.setSuccessCount(0);
            result.setInsertCount(0);
            result.setUpdateCount(0);
            result.setFailedCount(interventionExtraList.size());
            result.setMessage("导入失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取列索引
     */
    private Integer getColumnIndex(Map<String, Integer> columnIndexMap, String columnName) {
        return columnIndexMap.get(columnName);
    }
    
    /**
     * 获取单元格的字符串值
     */
    private String getCellValueAsString(Row row, Integer columnIndex) {
        if (row == null || columnIndex == null) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        return getCellValueAsString(cell);
    }
    
    /**
     * 获取单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理数字，避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }
    
    /**
     * 获取单元格的整数值
     */
    private Integer getCellValueAsInt(Row row, Integer columnIndex) {
        String stringValue = getCellValueAsString(row, columnIndex);
        if (stringValue == null || stringValue.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(stringValue.trim());
        } catch (NumberFormatException e) {
            // 尝试从字符串中提取数字
            try {
                return fieldValidator.cleanInt(stringValue);
            } catch (Exception ex) {
                return null;
            }
        }
    }
    
    /**
     * 创建验证错误
     */
    private ValidationErrorDTO createValidationError(int row, int patientId, String field, Object value, String message) {
        ValidationErrorDTO error = new ValidationErrorDTO();
        error.setRow(row);
        error.setPatientId(patientId);
        error.setField(field);
        error.setValue(value != null ? value.toString() : "");
        error.setMessage(message);
        return error;
    }
    
    /**
     * 创建错误验证结果
     */
    private ValidationResultDTO createErrorResult(String message) {
        ValidationResultDTO result = new ValidationResultDTO();
        result.setSuccess(false);
        result.setValid(false);
        result.setErrorCount(1);
        result.setMessage(message);
        result.setErrors(Collections.singletonList(createValidationError(0, 0, "系统", "", message)));
        return result;
    }
    
    /**
     * 创建错误导入结果
     */
    private ImportResultDTO createImportErrorResult(String message) {
        ImportResultDTO result = new ImportResultDTO();
        result.setSuccess(false);
        result.setStatus("failed");
        result.setSuccessCount(0);
        result.setInsertCount(0);
        result.setUpdateCount(0);
        result.setFailedCount(0);
        result.setMessage(message);
        return result;
    }
}


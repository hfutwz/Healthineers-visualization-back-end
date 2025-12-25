package com.demo.upload.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.entity.InterventionTime;
import com.demo.entity.Patient;
import com.demo.mapper.InterventionTimeMapper;
import com.demo.mapper.PatientMapper;
import com.demo.upload.constants.InterventionTimeColumnConstants;
import com.demo.upload.dto.ImportResultDTO;
import com.demo.upload.dto.ValidationErrorDTO;
import com.demo.upload.dto.ValidationResultDTO;
import com.demo.upload.validator.InterventionTimeFieldValidator;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * 干预时间数据导入服务
 */
@Service
public class InterventionTimeImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(InterventionTimeImportService.class);
    
    @Autowired
    private InterventionTimeMapper interventionTimeMapper;
    
    @Autowired
    private InterventionTimeFieldValidator fieldValidator;
    
    @Autowired
    private PatientMapper patientMapper;
    
    /**
     * 验证并导入干预时间数据（一步完成）
     * 读取Excel全部数据，验证所有数据并记录所有错误
     * 错误数为0才插入数据库，否则返回所有错误信息
     * 
     * @param excelFilePath Excel文件路径
     * @return 包含验证结果和导入结果的Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> validateAndImportInterventionTimeData(String excelFilePath) {
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
            for (String requiredColumn : InterventionTimeColumnConstants.REQUIRED_COLUMNS) {
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
            List<InterventionTime> validRecords = new ArrayList<>();
            
            // 从第1行开始读取数据（索引从0开始：第0行是列名，第1行是第一条数据）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                int excelRowNumber = rowIndex + 1; // Excel行号（从1开始：第1行是列名，第2行是第一条数据）
                
                // 1. 验证患者ID（序号）
                Integer patientId = getCellValueAsInt(row, getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.PATIENT_ID));
                if (patientId == null || patientId <= 0) {
                    allErrors.add(createValidationError(
                        excelRowNumber,
                        0,
                        "序号",
                        getCellRawValue(row, getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.PATIENT_ID)),
                        "患者序号不能为空或无效"
                    ));
                    continue; // 跳过无效的患者ID
                }
                
                // 检查患者ID是否在患者基本信息表中存在
                LambdaQueryWrapper<Patient> patientQueryWrapper = new LambdaQueryWrapper<>();
                patientQueryWrapper.eq(Patient::getPatientId, patientId);
                Patient existingPatient = patientMapper.selectOne(patientQueryWrapper);
                if (existingPatient == null) {
                    allErrors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "患者ID",
                        String.valueOf(patientId),
                        "患者ID " + patientId + " 在患者基本信息表中不存在，请先导入患者基本信息"
                    ));
                    continue; // 跳过不存在的患者ID
                }
                
                // 2. 验证接诊日期
                LocalDate admissionDate = fieldValidator.validateAdmissionDate(
                    row, 
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.ADMISSION_DATE),
                    excelRowNumber,
                    patientId,
                    allErrors
                );
                
                // 3. 验证接诊时间
                String admissionTime = fieldValidator.validateAdmissionTime(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.ADMISSION_TIME),
                    excelRowNumber,
                    patientId,
                    allErrors
                );
                
                // 如果接诊日期或时间验证失败，跳过该行（错误已记录）
                if (admissionDate == null || admissionTime == null) {
                    continue;
                }
                
                // 创建 InterventionTime 对象
                InterventionTime interventionTime = new InterventionTime();
                interventionTime.setPatientId(patientId);
                interventionTime.setAdmissionDate(admissionDate);
                interventionTime.setAdmissionTime(admissionTime);
                
                // 4. 验证并解析外周
                String peripheral = fieldValidator.validateTimeValueField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.PERIPHERAL),
                    excelRowNumber,
                    patientId,
                    "外周",
                    allErrors
                );
                interventionTime.setPeripheral(applyCrossDayOffset(admissionTime, peripheral));
                
                // 5. 验证并解析深静脉
                String ivLine = fieldValidator.validateTimeValueField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.IV_LINE),
                    excelRowNumber,
                    patientId,
                    "深静脉",
                    allErrors
                );
                interventionTime.setIvLine(applyCrossDayOffset(admissionTime, ivLine));
                
                // 6. 验证并解析骨通道
                String centralAccess = fieldValidator.validateTimeValueField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.CENTRAL_ACCESS),
                    excelRowNumber,
                    patientId,
                    "骨通道",
                    allErrors
                );
                interventionTime.setCentralAccess(applyCrossDayOffset(admissionTime, centralAccess));
                
                // 7. 验证并解析鼻导管
                String nasalPipe = fieldValidator.validateTimeValueField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.NASAL_PIPE),
                    excelRowNumber,
                    patientId,
                    "鼻导管",
                    allErrors
                );
                interventionTime.setNasalPipe(applyCrossDayOffset(admissionTime, nasalPipe));
                
                // 8. 验证并解析面罩
                String faceMask = fieldValidator.validateTimeValueField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.FACE_MASK),
                    excelRowNumber,
                    patientId,
                    "面罩",
                    allErrors
                );
                interventionTime.setFaceMask(applyCrossDayOffset(admissionTime, faceMask));
                
                // 9. 验证并解析气管插管
                String endotrachealTube = fieldValidator.validateTimeValueField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.ENDOTRACHEAL_TUBE),
                    excelRowNumber,
                    patientId,
                    "气管插管",
                    allErrors
                );
                interventionTime.setEndotrachealTube(applyCrossDayOffset(admissionTime, endotrachealTube));
                
                // 10. 验证并解析呼吸机
                String ventilator = fieldValidator.validateVentilatorField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.VENTILATOR),
                    excelRowNumber,
                    patientId,
                    allErrors
                );
                interventionTime.setVentilator(applyCrossDayOffset(admissionTime, ventilator));
                
                // 11. 验证心肺复苏（是/否）
                String cpr = fieldValidator.validateYesNoField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.CPR),
                    excelRowNumber,
                    patientId,
                    "心肺复苏",
                    allErrors
                );
                interventionTime.setCpr(cpr);
                
                // 12. 验证心肺复苏开始时间
                String cprStartTime = fieldValidator.validate4DigitTimeField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.CPR_START_TIME),
                    excelRowNumber,
                    patientId,
                    "心肺复苏开始时间",
                    allErrors
                );
                interventionTime.setCprStartTime(applyCrossDayOffset(admissionTime, cprStartTime));
                
                // 13. 验证心肺复苏结束时间
                String cprEndTime = fieldValidator.validate4DigitTimeField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.CPR_END_TIME),
                    excelRowNumber,
                    patientId,
                    "心肺复苏结束时间",
                    allErrors
                );
                interventionTime.setCprEndTime(applyCrossDayOffset(admissionTime, cprEndTime));
                
                // 14. 验证B超
                String ultrasound = fieldValidator.validateYesNoTimeField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.ULTRASOUND),
                    excelRowNumber,
                    patientId,
                    "B超",
                    allErrors
                );
                interventionTime.setUltrasound(applyCrossDayOffset(admissionTime, ultrasound));
                
                // 15. 验证CT
                String ct = fieldValidator.validateYesNoTimeField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.CT),
                    excelRowNumber,
                    patientId,
                    "CT",
                    allErrors
                );
                interventionTime.setCT(applyCrossDayOffset(admissionTime, ct));
                
                // 16. 验证止血带
                String tourniquet = fieldValidator.validateTimeValueField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.TOURNIQUET),
                    excelRowNumber,
                    patientId,
                    "止血带",
                    allErrors
                );
                interventionTime.setTourniquet(applyCrossDayOffset(admissionTime, tourniquet));
                
                // 17. 验证采血
                String bloodDraw = fieldValidator.validateYesNoTimeField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.BLOOD_DRAW),
                    excelRowNumber,
                    patientId,
                    "采血",
                    allErrors
                );
                interventionTime.setBloodDraw(applyCrossDayOffset(admissionTime, bloodDraw));
                
                // 18. 验证导尿
                String catheter = fieldValidator.validateYesNoTimeField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.CATHETER),
                    excelRowNumber,
                    patientId,
                    "导尿",
                    allErrors
                );
                interventionTime.setCatheter(applyCrossDayOffset(admissionTime, catheter));
                
                // 19. 验证胃管
                String gastricTube = fieldValidator.validateYesNoTimeField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.GASTRIC_TUBE),
                    excelRowNumber,
                    patientId,
                    "胃管",
                    allErrors
                );
                interventionTime.setGastricTube(applyCrossDayOffset(admissionTime, gastricTube));
                
                // 20. 验证输血（是/否）
                String transfusion = fieldValidator.validateYesNoField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.TRANSFUSION),
                    excelRowNumber,
                    patientId,
                    "输血",
                    allErrors
                );
                interventionTime.setTransfusion(transfusion);
                
                // 21. 验证输血开始时间
                String transfusionStart = fieldValidator.validate4DigitTimeField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.TRANSFUSION_START),
                    excelRowNumber,
                    patientId,
                    "输血开始时间",
                    allErrors
                );
                interventionTime.setTransfusionStart(applyCrossDayOffset(admissionTime, transfusionStart));
                
                // 22. 验证输血结束时间
                String transfusionEnd = fieldValidator.validate4DigitTimeField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.TRANSFUSION_END),
                    excelRowNumber,
                    patientId,
                    "输血结束时间",
                    allErrors
                );
                interventionTime.setTransfusionEnd(applyCrossDayOffset(admissionTime, transfusionEnd));
                
                // 23. 验证并解析离开抢救室时间
                Object[] leaveSurgeryResult = fieldValidator.parseLeaveSurgeryTime(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.LEAVE_SURGERY_TIME),
                    excelRowNumber,
                    patientId,
                    admissionDate,
                    admissionTime,
                    allErrors
                );
                interventionTime.setLeaveSurgeryTime((String) leaveSurgeryResult[1]);
                interventionTime.setLeaveSurgeryDate((LocalDate) leaveSurgeryResult[0]);
                
                // 24. 病人去向（文本字段，不需要验证）
                String patientDestination = getCellValueAsString(row, getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.PATIENT_DESTINATION));
                interventionTime.setPatientDestination(cleanText(patientDestination));
                
                // 25. 验证死亡（是/否）
                String death = fieldValidator.validateYesNoField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.DEATH),
                    excelRowNumber,
                    patientId,
                    "死亡",
                    allErrors
                );
                interventionTime.setDeath(death);
                
                // 26. 验证死亡日期
                LocalDate deathDate = fieldValidator.validateDeathDate(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.DEATH_DATE),
                    excelRowNumber,
                    patientId,
                    allErrors
                );
                interventionTime.setDeathDate(deathDate);
                
                // 27. 验证死亡时间
                String deathTime = fieldValidator.validate4DigitTimeField(
                    row,
                    getColumnIndex(columnIndexMap, InterventionTimeColumnConstants.DEATH_TIME),
                    excelRowNumber,
                    patientId,
                    "死亡时间",
                    allErrors
                );
                interventionTime.setDeathTime(deathTime);
                
                validRecords.add(interventionTime);
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
            ImportResultDTO importResult = importInterventionTimeData(validRecords);
            
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
     * 对无日期的事件时间应用跨天偏移逻辑：
     * 如果事件时间早于入室时间，则存储为 2400 + 事件时间（字符串保持4位）
     */
    private String applyCrossDayOffset(String admissionTime, String eventTime) {
        if (admissionTime == null || eventTime == null) {
            return eventTime;
        }
        if (admissionTime.length() != 4 || eventTime.length() != 4) {
            return eventTime;
        }
        try {
            int admission = Integer.parseInt(admissionTime);
            int event = Integer.parseInt(eventTime);
            if (event < admission) {
                int adjusted = 2400 + event;
                return String.format("%04d", adjusted);
            }
        } catch (NumberFormatException ignored) {
        }
        return eventTime;
    }
    
    /**
     * 导入干预时间数据（纯Java实现）
     * 注意：此方法假设数据已经通过验证，直接插入数据库
     * 
     * @param interventionTimes 已验证的干预时间记录列表
     * @return 导入结果
     */
    private ImportResultDTO importInterventionTimeData(List<InterventionTime> interventionTimes) {
        try {
            int insertCount = 0;  // 新插入的记录数
            int updateCount = 0;  // 更新的记录数
            int totalCount = interventionTimes.size();
            
            if (!interventionTimes.isEmpty()) {
                try {
                    // 使用批量插入方法（使用 ON DUPLICATE KEY UPDATE）
                    for (InterventionTime record : interventionTimes) {
                        // 检查是否已存在相同的 patient_id 记录
                        LambdaQueryWrapper<InterventionTime> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(InterventionTime::getPatientId, record.getPatientId());
                        List<InterventionTime> existingList = interventionTimeMapper.selectList(queryWrapper);
                        
                        if (existingList != null && !existingList.isEmpty()) {
                            // 如果存在多条记录，抛出错误
                            if (existingList.size() > 1) {
                                throw new RuntimeException("重复了");
                            }
                            
                            // 更新现有记录（只有一条）
                            InterventionTime existing = existingList.get(0);
                            record.setInterventionId(existing.getInterventionId());
                            interventionTimeMapper.updateById(record);
                            updateCount++;
                            logger.debug("更新已存在的干预时间记录，患者ID: {}", record.getPatientId());
                        } else {
                            // 插入新记录
                            interventionTimeMapper.insert(record);
                            insertCount++;
                            logger.debug("插入新的干预时间记录，患者ID: {}", record.getPatientId());
                        }
                    }
                    
                    logger.info("批量插入干预时间数据完成，新插入: {} 条，更新: {} 条", insertCount, updateCount);
                } catch (RuntimeException e) {
                    // 重新抛出RuntimeException，保持错误信息
                    throw e;
                } catch (Exception e) {
                    logger.error("批量插入干预时间数据时发生异常", e);
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
                    result.setMessage("数据导入成功，共处理 " + successCount + " 条记录（新插入 " + insertCount + " 条，更新 " + updateCount + " 条）");
                } else if (insertCount > 0) {
                    result.setMessage("数据导入成功，共新插入 " + insertCount + " 条记录");
                } else if (updateCount > 0) {
                    result.setMessage("所有记录已存在，已更新 " + updateCount + " 条记录（未插入新数据）");
                } else {
                    result.setMessage("数据导入完成，共处理 " + successCount + " 条记录");
                }
            } else {
                StringBuilder message = new StringBuilder("数据导入完成，成功 " + successCount + " 条");
                if (insertCount > 0) {
                    message.append("（新插入 ").append(insertCount).append(" 条");
                }
                if (updateCount > 0) {
                    if (insertCount > 0) {
                        message.append("，更新 ").append(updateCount).append(" 条");
                    } else {
                        message.append("（更新 ").append(updateCount).append(" 条");
                    }
                }
                if (insertCount > 0 || updateCount > 0) {
                    message.append("）");
                }
                message.append("，失败 ").append(failedCount).append(" 条");
                result.setMessage(message.toString());
            }
            
            logger.info("干预时间数据导入完成: 新插入 {} 条，更新 {} 条，失败 {} 条", insertCount, updateCount, failedCount);
            
            return result;
            
        } catch (Exception e) {
            logger.error("导入数据时发生异常", e);
            return createImportErrorResult("导入数据时发生异常: " + e.getMessage());
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 从列名映射中获取列索引（自动处理trim）
     */
    private Integer getColumnIndex(Map<String, Integer> columnIndexMap, String columnName) {
        if (columnIndexMap == null || columnName == null) {
            return null;
        }
        return columnIndexMap.get(columnName.trim());
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
                        return String.valueOf((long) cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
            case BLANK:
                return null;
            default:
                return null;
        }
    }
    
    /**
     * 获取单元格的整数值
     */
    private Integer getCellValueAsInt(Row row, Integer columnIndex) {
        if (row == null || columnIndex == null) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return (int) (long) numericValue;
                    } else {
                        return (int) numericValue;
                    }
                case STRING:
                    String strValue = cell.getStringCellValue().trim();
                    if (strValue.isEmpty()) {
                        return null;
                    }
                    strValue = strValue.replaceAll("[^0-9.-]", "");
                    if (strValue.isEmpty()) {
                        return null;
                    }
                    double doubleValue = Double.parseDouble(strValue);
                    return (int) doubleValue;
                case BOOLEAN:
                    return cell.getBooleanCellValue() ? 1 : 0;
                case FORMULA:
                    try {
                        return (int) cell.getNumericCellValue();
                    } catch (Exception e) {
                        return null;
                    }
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 清理文本
     */
    private String cleanText(String text) {
        if (text == null) {
            return null;
        }
        String cleaned = text.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
    
    /**
     * 获取单元格的原始值（用于错误报告）
     */
    private Object getCellRawValue(Row row, Integer columnIndex) {
        if (row == null || columnIndex == null) {
            return "";
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        double numericValue = cell.getNumericCellValue();
                        if (numericValue == (long) numericValue) {
                            return (long) numericValue;
                        } else {
                            return numericValue;
                        }
                    }
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case FORMULA:
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception e) {
                        try {
                            return cell.getNumericCellValue();
                        } catch (Exception ex) {
                            return cell.getCellFormula();
                        }
                    }
                case BLANK:
                    return "";
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * 创建验证错误对象
     */
    private ValidationErrorDTO createValidationError(int row, int patientId, String field, Object value, String message) {
        ValidationErrorDTO error = new ValidationErrorDTO();
        error.setRow(row);
        error.setPatientId(patientId);
        error.setField(field);
        error.setValue(value);
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
        
        ValidationErrorDTO error = new ValidationErrorDTO();
        error.setRow(0);
        error.setPatientId(0);
        error.setField("系统错误");
        error.setValue("");
        error.setMessage(message);
        
        List<ValidationErrorDTO> errors = new ArrayList<>();
        errors.add(error);
        result.setErrors(errors);
        
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


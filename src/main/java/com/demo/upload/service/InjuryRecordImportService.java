package com.demo.upload.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.entity.InjuryRecord;
import com.demo.entity.Patient;
import com.demo.mapper.InjuryRecordMapper;
import com.demo.mapper.PatientMapper;
import com.demo.upload.constants.InjuryRecordColumnConstants;
import com.demo.upload.dto.ImportResultDTO;
import com.demo.upload.dto.ValidationErrorDTO;
import com.demo.upload.dto.ValidationResultDTO;
import com.demo.upload.validator.InjuryRecordFieldValidator;
import com.demo.utils.TimePeriodUtils;
import com.demo.utils.LongitudeLatitudeUtils;
import com.demo.utils.SeasonUtils;
import com.demo.config.AmapConfig;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 创伤病例数据导入服务
 */
@Service
public class InjuryRecordImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(InjuryRecordImportService.class);
    
    @Autowired
    private InjuryRecordMapper injuryRecordMapper;
    
    @Autowired
    private InjuryRecordFieldValidator fieldValidator;
    
    @Autowired
    private PatientMapper patientMapper;
    
    @Autowired
    private AmapConfig amapConfig;
    
    /**
     * 受伤原因分类映射
     */
    private static final Map<String, Integer> INJURY_CAUSE_MAPPING = new HashMap<>();
    static {
        INJURY_CAUSE_MAPPING.put("交通伤", 0);
        INJURY_CAUSE_MAPPING.put("高坠伤", 1);
        INJURY_CAUSE_MAPPING.put("机械伤", 2);
        INJURY_CAUSE_MAPPING.put("跌倒", 3);
        INJURY_CAUSE_MAPPING.put("其他", 4);
    }
    
    /**
     * 验证并导入创伤病例数据（一步完成）
     * 读取Excel全部数据，验证所有数据并记录所有错误
     * 错误数为0才插入数据库，否则返回所有错误信息
     * 
     * @param excelFilePath Excel文件路径
     * @return 包含验证结果和导入结果的Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> validateAndImportInjuryRecordData(String excelFilePath) {
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
            for (String requiredColumn : InjuryRecordColumnConstants.REQUIRED_COLUMNS) {
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
                error.setField("系统错误");
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
            
            // 验证所有数据行
            List<ValidationErrorDTO> allErrors = new ArrayList<>();
            List<InjuryRecord> validRecords = new ArrayList<>();
            
            // 从第1行开始读取数据（索引从0开始：第0行是列名，第1行是第一条数据）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                // 读取患者ID
                Integer patientId = getCellValueAsInt(row, getColumnIndex(columnIndexMap, InjuryRecordColumnConstants.PATIENT_ID));
                int excelRowNumber = rowIndex + 1; // Excel行号（从1开始）
                
                // 验证患者ID是否有效
                if (patientId == null || patientId <= 0) {
                    allErrors.add(createValidationError(
                        excelRowNumber,
                        0,
                        "患者ID",
                        getCellValueAsString(row, getColumnIndex(columnIndexMap, InjuryRecordColumnConstants.PATIENT_ID)),
                        "患者ID不能为空或无效"
                    ));
                    continue; // 跳过无效的患者ID
                }
                
                // 验证患者ID是否存在于patient表中
                // 使用 LambdaQueryWrapper 确保查询正确
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
                
                // 验证接诊日期
                LocalDate admissionDate = fieldValidator.validateAdmissionDate(
                    row, 
                    getColumnIndex(columnIndexMap, InjuryRecordColumnConstants.ADMISSION_DATE),
                    excelRowNumber,
                    patientId,
                    allErrors
                );
                
                // 验证接诊时间
                String admissionTime = fieldValidator.validateAdmissionTime(
                    row,
                    getColumnIndex(columnIndexMap, InjuryRecordColumnConstants.ADMISSION_TIME),
                    excelRowNumber,
                    patientId,
                    allErrors
                );
                
                // 如果验证失败，跳过该行（错误已记录）
                if (admissionDate == null || admissionTime == null) {
                    continue;
                }
                
                // 创建 InjuryRecord 对象
                InjuryRecord injuryRecord = new InjuryRecord();
                injuryRecord.setPatientId(patientId);
                injuryRecord.setAdmissionDate(admissionDate);
                injuryRecord.setAdmissionTime(admissionTime);
                
                // 处理其他字段（按照 injury_record_importer.py 的逻辑）
                // 来院方式
                String arrivalMethod = getCellValueAsString(row, getColumnIndex(columnIndexMap, InjuryRecordColumnConstants.ARRIVAL_METHOD));
                injuryRecord.setArrivalMethod(cleanText(arrivalMethod));
                
                // 创伤发生地
                String injuryLocation = getCellValueAsString(row, getColumnIndex(columnIndexMap, InjuryRecordColumnConstants.INJURY_LOCATION));
                injuryRecord.setInjuryLocationDesc(cleanText(injuryLocation));
                
                // 120分站站点名称
                String stationName = getCellValueAsString(row, getColumnIndex(columnIndexMap, InjuryRecordColumnConstants.STATION_NAME));
                injuryRecord.setStationName(cleanText(stationName));
                
                // 受伤原因分类
                String injuryCauseRaw = getCellValueAsString(row, getColumnIndex(columnIndexMap, InjuryRecordColumnConstants.INJURY_CAUSE));
                String injuryCauseText = cleanText(injuryCauseRaw);
                if (injuryCauseText != null && !injuryCauseText.isEmpty()) {
                    // 分类受伤原因
                    Integer category = null;
                    String detail = null;
                    
                    // 检查是否包含已知的分类关键词
                    for (Map.Entry<String, Integer> entry : INJURY_CAUSE_MAPPING.entrySet()) {
                        if (injuryCauseText.contains(entry.getKey())) {
                            category = entry.getValue();
                            if (category == 4) {
                                // "其他"类别，提取具体描述
                                detail = extractOtherCauseDetail(injuryCauseText);
                            } else {
                                // 非"其他"类别，使用完整文本作为描述
                                detail = injuryCauseText;
                            }
                            break;
                        }
                    }
                    
                    // 如果没有匹配到，默认为"其他"
                    if (category == null) {
                        category = 4;
                        detail = extractOtherCauseDetail(injuryCauseText);
                        if (detail == null) {
                            detail = injuryCauseText;
                        }
                    }
                    
                    injuryRecord.setInjuryCauseCategory(category);
                    injuryRecord.setInjuryCauseDetail(detail != null ? detail : "");
                } else {
                    injuryRecord.setInjuryCauseCategory(4); // 默认为"其他"
                    injuryRecord.setInjuryCauseDetail("");
                }
                
                // 季节和时间段在导入后通过其他方式计算，这里不设置
                injuryRecord.setSeason(null);
                injuryRecord.setTimePeriod(null);
                
                // 经度和纬度在导入后通过其他方式计算，这里不设置
                injuryRecord.setLongitude(null);
                injuryRecord.setLatitude(null);
                
                validRecords.add(injuryRecord);
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
            
            // 验证通过，更新季节、时间段和经纬度，然后导入数据
            // 只有在所有数据都合法时才执行这些更新
            logger.info("数据验证通过，开始更新季节、时间段和经纬度信息...");
            
            // 更新季节（即使失败也不影响数据导入）
            try {
                SeasonUtils.updateSeason(validRecords);
                logger.info("季节更新完成");
            } catch (Exception e) {
                logger.warn("季节更新失败，但继续导入数据: {}", e.getMessage());
            }
            
            // 更新时间段（即使失败也不影响数据导入）
            try {
                TimePeriodUtils.updateTimePeriod(validRecords);
                logger.info("时间段更新完成");
            } catch (Exception e) {
                logger.warn("时间段更新失败，但继续导入数据: {}", e.getMessage());
            }
            
            // 更新经纬度（调用高德地图API，即使失败也不影响数据导入）
            try {
                LongitudeLatitudeUtils.updateLongitudeLatitude(
                    validRecords, 
                    amapConfig.getApiKey(), 
                    amapConfig.getCity()
                );
                logger.info("经纬度更新完成");
            } catch (Exception e) {
                logger.warn("经纬度更新失败，但继续导入数据: {}", e.getMessage());
            }
            
            // 导入数据
            ImportResultDTO importResult = importInjuryRecordData(validRecords);
            
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
     * 导入创伤病例数据（纯Java实现）
     * 注意：此方法假设数据已经通过验证，直接插入数据库
     * 
     * @param injuryRecords 已验证的创伤病例记录列表
     * @return 导入结果
     */
    private ImportResultDTO importInjuryRecordData(List<InjuryRecord> injuryRecords) {
        try {
            int insertCount = 0;  // 新插入的记录数
            int updateCount = 0;  // 更新的记录数
            int totalCount = injuryRecords.size();
            
            if (!injuryRecords.isEmpty()) {
                try {
                    // 使用批量插入方法（使用 ON DUPLICATE KEY UPDATE）
                    for (InjuryRecord record : injuryRecords) {
                        // 检查是否已存在相同的 patient_id 记录
                        LambdaQueryWrapper<InjuryRecord> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(InjuryRecord::getPatientId, record.getPatientId());
                        List<InjuryRecord> existingList = injuryRecordMapper.selectList(queryWrapper);
                        
                        if (existingList != null && !existingList.isEmpty()) {
                            // 如果存在多条记录，抛出错误
                            if (existingList.size() > 1) {
                                throw new RuntimeException("重复了");
                            }
                            
                            // 更新现有记录（只有一条）
                            InjuryRecord existing = existingList.get(0);
                            record.setInjuryId(existing.getInjuryId());
                            injuryRecordMapper.updateById(record);
                            updateCount++;
                            logger.debug("更新已存在的创伤病例记录，患者ID: {}", record.getPatientId());
                        } else {
                            // 插入新记录
                            injuryRecordMapper.insert(record);
                            insertCount++;
                            logger.debug("插入新的创伤病例记录，患者ID: {}", record.getPatientId());
                        }
                    }
                    
                    logger.info("批量插入创伤病例数据完成，新插入: {} 条，更新: {} 条", insertCount, updateCount);
                } catch (RuntimeException e) {
                    // 重新抛出RuntimeException，保持错误信息
                    throw e;
                } catch (Exception e) {
                    logger.error("批量插入创伤病例数据时发生异常", e);
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
            
            logger.info("创伤病例数据导入完成: 新插入 {} 条，更新 {} 条，失败 {} 条", insertCount, updateCount, failedCount);
            
            return result;
            
        } catch (Exception e) {
            logger.error("导入数据时发生异常", e);
            return createImportErrorResult("导入数据时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 从"其他"类别的受伤原因中提取具体描述
     * 
     * @param injuryCauseText 受伤原因文本
     * @return 具体描述
     */
    private String extractOtherCauseDetail(String injuryCauseText) {
        if (injuryCauseText == null || injuryCauseText.trim().isEmpty()) {
            return null;
        }
        
        // 提取〖〗中的内容
        Pattern pattern = Pattern.compile("〖([^〗]+)〗");
        Matcher matcher = pattern.matcher(injuryCauseText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return injuryCauseText;
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
                        double numericValue = cell.getNumericCellValue();
                        if (numericValue == (long) numericValue) {
                            return String.valueOf((long) numericValue);
                        } else {
                            return String.valueOf(numericValue);
                        }
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
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
                    // 尝试解析字符串为数字
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
     * 创建验证错误对象
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
}


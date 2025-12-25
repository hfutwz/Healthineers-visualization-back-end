package com.demo.upload.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.entity.Patient;
import com.demo.mapper.PatientMapper;
import com.demo.upload.constants.PatientColumnConstants;
import com.demo.upload.dto.ImportResultDTO;
import com.demo.upload.dto.ValidationErrorDTO;
import com.demo.upload.dto.ValidationResultDTO;
import com.demo.upload.validator.PatientFieldValidator;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 患者数据导入服务
 */
@Service
public class PatientDataImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(PatientDataImportService.class);
    
    @Autowired
    private PatientMapper patientMapper;
    
    @Autowired
    private PatientFieldValidator fieldValidator;
    
    /**
     * 验证Excel文件中的数据（纯Java实现）
     * 
     * @param excelFilePath Excel文件路径
     * @return 验证结果
     */
    public ValidationResultDTO validatePatientData(String excelFilePath) {
        Workbook workbook = null;
        try {
            // 检查Excel文件是否存在
            File excelFile = new File(excelFilePath);
            if (!excelFile.exists()) {
                logger.error("Excel文件不存在: {}", excelFilePath);
                return createErrorResult("Excel文件不存在: " + excelFilePath);
            }
            
            // 使用Apache POI读取Excel文件
            workbook = WorkbookFactory.create(excelFile);
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet == null || sheet.getLastRowNum() < 0) {
                return createErrorResult("Excel文件中没有数据");
            }
            
            // 读取标题行（第0行是列名）
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return createErrorResult("Excel文件中没有标题行（第0行应为列名）");
            }
            
            // 检查是否有数据行（第1行应该是第一条数据）
            if (sheet.getLastRowNum() < 1) {
                return createErrorResult("Excel文件中没有数据行（第1行应为第一条数据）");
            }
            
            // 构建列名到列索引的映射
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell).trim();
                    columnIndexMap.put(cellValue, cell.getColumnIndex());
                }
            }
            
            // 检查必需的列是否存在（使用trim后的列名进行比较）
            List<String> missingColumns = new ArrayList<>();
            for (String requiredColumn : PatientColumnConstants.REQUIRED_COLUMNS) {
                String trimmedRequiredColumn = requiredColumn.trim();
                if (!columnIndexMap.containsKey(trimmedRequiredColumn)) {
                    missingColumns.add(requiredColumn);
                }
            }
            
            if (!missingColumns.isEmpty()) {
                ValidationResultDTO result = new ValidationResultDTO();
                result.setSuccess(false);
                result.setValid(false);
                result.setErrorCount(1);
                result.setMessage("缺少必需的列: " + String.join(", ", missingColumns));
                
                ValidationErrorDTO error = new ValidationErrorDTO();
                error.setRow(0);
                error.setPatientId(0);
                error.setField("Excel列");
                error.setValue("");
                error.setMessage("缺少必需的列: " + String.join(", ", missingColumns));
                
                List<ValidationErrorDTO> errors = new ArrayList<>();
                errors.add(error);
                result.setErrors(errors);
                return result;
            }
            
            // 查询数据库中已存在的患者ID
            Set<Integer> existingPatientIds = new HashSet<>();
            try {
                List<Patient> existingPatients = patientMapper.selectList(new LambdaQueryWrapper<Patient>()
                    .select(Patient::getPatientId));
                existingPatientIds = existingPatients.stream()
                    .map(Patient::getPatientId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
                logger.info("数据库中已存在的患者ID数量: {}", existingPatientIds.size());
            } catch (Exception e) {
                logger.error("查询数据库中已存在的患者ID时发生异常", e);
                return createErrorResult("查询数据库中已存在的患者ID时发生异常: " + e.getMessage());
            }
            
            // 验证所有数据行
            List<ValidationErrorDTO> errors = new ArrayList<>();
            Set<Integer> patientIdSet = new HashSet<>(); // 用于检查Excel内部重复的患者ID
            
            // 从第1行开始读取数据（索引从0开始：第0行是列名，第1行是第一条数据）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                int excelRowNumber = rowIndex + 1; // Excel行号（从1开始：第1行是列名，第2行是第一条数据）
                
                // 1. 验证患者ID（序号）
                Integer patientId = fieldValidator.validatePatientId(
                    row, 
                    getColumnIndex(columnIndexMap, PatientColumnConstants.PATIENT_ID), 
                    excelRowNumber, 
                    errors
                );
                if (patientId == null) {
                    continue; // 患者ID无效，跳过该行的其他验证
                }
                
                // 检查患者ID在Excel内部是否重复
                if (patientIdSet.contains(patientId)) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "序号",
                        patientId,
                        "患者序号冲突: 患者ID " + patientId + " 在Excel中重复"
                    ));
                } else {
                    patientIdSet.add(patientId);
                }
                
                // 检查患者ID是否与数据库中已存在的患者ID重复
                if (existingPatientIds.contains(patientId)) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "序号",
                        patientId,
                        "患者序号冲突: 患者ID " + patientId + " 在数据库中已存在"
                    ));
                }
                
                // 2. 验证性别（只能是"男"或"女"）
                fieldValidator.validateGender(
                    row,
                    getColumnIndex(columnIndexMap, PatientColumnConstants.GENDER),
                    excelRowNumber,
                    patientId,
                    errors
                );
                
                // 3. 验证年龄（必须是纯整数，0-120）
                fieldValidator.validateAge(
                    row,
                    getColumnIndex(columnIndexMap, PatientColumnConstants.AGE),
                    excelRowNumber,
                    patientId,
                    errors
                );
                
                // 4. 验证是否绿色通道（只能是"是"或"否"）
                fieldValidator.validateIsGreenChannel(
                    row,
                    getColumnIndex(columnIndexMap, PatientColumnConstants.IS_GREEN_CHANNEL),
                    excelRowNumber,
                    patientId,
                    errors
                );
                
                // 5. 验证身高（DECIMAL(5,2)，可带小数）
                fieldValidator.validateHeight(
                    row,
                    getColumnIndex(columnIndexMap, PatientColumnConstants.HEIGHT),
                    excelRowNumber,
                    patientId,
                    errors
                );
                
                // 6. 验证体重（DECIMAL(5,2)，可带小数）
                fieldValidator.validateWeight(
                    row,
                    getColumnIndex(columnIndexMap, PatientColumnConstants.WEIGHT),
                    excelRowNumber,
                    patientId,
                    errors
                );
                
                // 7. 姓名可以为空，不需要验证
            }
            
            // 构建验证结果
            ValidationResultDTO result = new ValidationResultDTO();
            result.setSuccess(true);
            result.setValid(errors.isEmpty());
            result.setErrorCount(errors.size());
            result.setErrors(errors);
            
            if (errors.isEmpty()) {
                result.setMessage("数据验证通过");
            } else {
                result.setMessage("发现 " + errors.size() + " 个验证错误");
            }
            
            logger.info("数据验证完成: 共 {} 行数据，发现 {} 个错误", sheet.getLastRowNum(), errors.size());
            
            return result;
            
        } catch (Exception e) {
            logger.error("验证数据时发生异常", e);
            return createErrorResult("验证数据时发生异常: " + e.getMessage());
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
     * 验证并导入患者数据（一步完成）
     * 读取Excel全部数据，验证所有数据并记录所有错误
     * 错误数为0才插入数据库，否则返回所有错误信息
     * 
     * @param excelFilePath Excel文件路径
     * @return 包含验证结果和导入结果的Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> validateAndImportPatientData(String excelFilePath) {
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
            for (String requiredColumn : PatientColumnConstants.REQUIRED_COLUMNS) {
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
            
            // 查询数据库中已存在的患者ID
            Set<Integer> existingPatientIds = new HashSet<>();
            try {
                List<Patient> existingPatients = patientMapper.selectList(new LambdaQueryWrapper<Patient>()
                    .select(Patient::getPatientId));
                existingPatientIds = existingPatients.stream()
                    .map(Patient::getPatientId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
                logger.info("数据库中已存在的患者ID数量: {}", existingPatientIds.size());
            } catch (Exception e) {
                logger.error("查询数据库中已存在的患者ID时发生异常", e);
                ValidationResultDTO validationResult = createErrorResult("查询数据库中已存在的患者ID时发生异常: " + e.getMessage());
                ImportResultDTO importResult = createImportErrorResult("查询数据库中已存在的患者ID时发生异常: " + e.getMessage());
                result.put("validation", validationResult);
                result.put("import", importResult);
                result.put("success", false);
                result.put("message", "查询数据库中已存在的患者ID时发生异常: " + e.getMessage());
                return result;
            }
            
            // 读取并验证所有数据行，记录所有错误
            List<ValidationErrorDTO> errors = new ArrayList<>();
            List<Patient> validPatientList = new ArrayList<>();
            Set<Integer> patientIdSet = new HashSet<>(); // 用于检查Excel内部重复的患者ID
            
            // 从第1行开始读取数据（索引从0开始：第0行是列名，第1行是第一条数据）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                int excelRowNumber = rowIndex + 1; // Excel行号（从1开始：第1行是列名，第2行是第一条数据）
                boolean rowHasError = false;
                Integer patientId = null;
                
                // 记录验证前的错误数量
                int errorCountBefore = errors.size();
                
                // 1. 验证患者ID（序号）
                patientId = fieldValidator.validatePatientId(
                    row,
                    getColumnIndex(columnIndexMap, PatientColumnConstants.PATIENT_ID),
                    excelRowNumber,
                    errors
                );
                if (patientId == null) {
                    rowHasError = true;
                } else {
                    // 检查患者ID在Excel内部是否重复
                    if (patientIdSet.contains(patientId)) {
                        errors.add(createValidationError(
                            excelRowNumber,
                            patientId,
                            "序号",
                            patientId,
                            "患者序号冲突: 患者ID " + patientId + " 在Excel中重复"
                        ));
                        rowHasError = true;
                    } else {
                        patientIdSet.add(patientId);
                    }
                    
                    // 检查患者ID是否与数据库中已存在的患者ID重复
                    if (existingPatientIds.contains(patientId)) {
                        errors.add(createValidationError(
                            excelRowNumber,
                            patientId,
                            "序号",
                            patientId,
                            "患者序号冲突: 患者ID " + patientId + " 在数据库中已存在"
                        ));
                        rowHasError = true;
                    }
                }
                
                // 2. 验证性别（只能是"男"或"女"）
                String gender = fieldValidator.validateGender(
                    row,
                    getColumnIndex(columnIndexMap, PatientColumnConstants.GENDER),
                    excelRowNumber,
                    patientId != null ? patientId : 0,
                    errors
                );
                
                // 3. 验证年龄（必须是纯整数，0-120）
                Integer age = fieldValidator.validateAge(
                    row,
                    getColumnIndex(columnIndexMap, PatientColumnConstants.AGE),
                    excelRowNumber,
                    patientId != null ? patientId : 0,
                    errors
                );
                
                // 4. 验证是否绿色通道（只能是"是"或"否"）
                String isGreenChannel = fieldValidator.validateIsGreenChannel(
                    row,
                    getColumnIndex(columnIndexMap, PatientColumnConstants.IS_GREEN_CHANNEL),
                    excelRowNumber,
                    patientId != null ? patientId : 0,
                    errors
                );
                
                // 5. 验证身高（DECIMAL(5,2)，可带小数）
                Double height = fieldValidator.validateHeight(
                    row,
                    getColumnIndex(columnIndexMap, PatientColumnConstants.HEIGHT),
                    excelRowNumber,
                    patientId != null ? patientId : 0,
                    errors
                );
                
                // 6. 验证体重（DECIMAL(5,2)，可带小数）
                Double weight = fieldValidator.validateWeight(
                    row,
                    getColumnIndex(columnIndexMap, PatientColumnConstants.WEIGHT),
                    excelRowNumber,
                    patientId != null ? patientId : 0,
                    errors
                );
                
                // 检查是否有新的错误产生
                if (errors.size() > errorCountBefore) {
                    rowHasError = true;
                }
                
                // 如果该行没有错误，创建Patient对象并添加到有效列表
                if (!rowHasError && patientId != null && patientId > 0) {
                    Patient patient = new Patient();
                    patient.setPatientId(patientId);
                    patient.setGender(gender);
                    patient.setAge(age);
                    patient.setIsGreenChannel(isGreenChannel != null && !isGreenChannel.isEmpty() ? isGreenChannel : "否");
                    
                    if (height != null) {
                        BigDecimal heightDecimal = BigDecimal.valueOf(height).setScale(2, RoundingMode.HALF_UP);
                        patient.setHeight(heightDecimal.doubleValue());
                    } else {
                        patient.setHeight(null);
                    }
                    
                    if (weight != null) {
                        BigDecimal weightDecimal = BigDecimal.valueOf(weight).setScale(2, RoundingMode.HALF_UP);
                        patient.setWeight(weightDecimal.doubleValue());
                    } else {
                        patient.setWeight(null);
                    }
                    
                    // 姓名字段不需要从Excel读取，数据库允许为NULL
                    
                    validPatientList.add(patient);
                }
            }
            
            // 构建验证结果
            ValidationResultDTO validationResult = new ValidationResultDTO();
            validationResult.setSuccess(true);
            validationResult.setValid(errors.isEmpty());
            validationResult.setErrorCount(errors.size());
            validationResult.setErrors(errors);
            
            if (errors.isEmpty()) {
                validationResult.setMessage("数据验证通过");
            } else {
                validationResult.setMessage("发现 " + errors.size() + " 个验证错误");
            }
            
            logger.info("数据验证完成: 共 {} 行数据，发现 {} 个错误", sheet.getLastRowNum(), errors.size());
            
            // 根据错误数决定是否插入数据库
            ImportResultDTO importResult = new ImportResultDTO();
            
            if (errors.isEmpty()) {
                // 错误数为0，执行插入
                try {
                    int successCount = 0;
                    if (!validPatientList.isEmpty()) {
                        int affectedRows = patientMapper.insertBatch(validPatientList);
                        successCount = affectedRows;
                        logger.info("批量插入患者数据完成，影响行数: {}", affectedRows);
                    }
                    
                    importResult.setSuccess(true);
                    importResult.setSuccessCount(successCount);
                    importResult.setFailedCount(0);
                    importResult.setStatus("success");
                    importResult.setMessage("数据导入成功，共导入 " + successCount + " 条记录");
                    
                    result.put("success", true);
                    result.put("message", "数据导入成功，共导入 " + successCount + " 条记录");
                } catch (Exception e) {
                    logger.error("批量插入患者数据时发生异常", e);
                    String errorMessage = "导入数据时发生异常: " + e.getMessage();
                    importResult.setSuccess(false);
                    importResult.setStatus("failed");
                    importResult.setSuccessCount(0);
                    importResult.setFailedCount(validPatientList.size());
                    importResult.setMessage(errorMessage);
                    
                    // 将异常信息添加到验证结果中
                    ValidationErrorDTO error = new ValidationErrorDTO();
                    error.setRow(0);
                    error.setPatientId(0);
                    error.setField("系统错误");
                    error.setValue("");
                    error.setMessage(errorMessage);
                    errors.add(error);
                    validationResult.setErrors(errors);
                    validationResult.setValid(false);
                    validationResult.setErrorCount(errors.size());
                    validationResult.setMessage(errorMessage);
                    
                    result.put("success", false);
                    result.put("message", errorMessage);
                }
            } else {
                // 错误数不为0，不插入，返回所有错误信息
                importResult.setSuccess(false);
                importResult.setStatus("failed");
                importResult.setSuccessCount(0);
                importResult.setFailedCount(errors.size());
                importResult.setMessage("数据验证失败，发现 " + errors.size() + " 个错误，未执行导入操作");
                
                result.put("success", false);
                result.put("message", "数据验证失败，发现 " + errors.size() + " 个错误");
            }
            
            result.put("validation", validationResult);
            result.put("import", importResult);
            
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
     * 导入患者数据（纯Java实现，不使用Python）
     * 注意：此方法假设数据已经通过验证，直接插入数据库
     * 
     * @param excelFilePath Excel文件路径
     * @return 导入结果
     */
    private ImportResultDTO importPatientData(String excelFilePath) {
        Workbook workbook = null;
        try {
            // 检查Excel文件是否存在
            File excelFile = new File(excelFilePath);
            if (!excelFile.exists()) {
                logger.error("Excel文件不存在: {}", excelFilePath);
                return createImportErrorResult("Excel文件不存在: " + excelFilePath);
            }
            
            // 使用Apache POI读取Excel文件
            workbook = WorkbookFactory.create(excelFile);
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet == null || sheet.getLastRowNum() < 0) {
                return createImportErrorResult("Excel文件中没有数据");
            }
            
            // 读取标题行（第0行是列名）
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return createImportErrorResult("Excel文件中没有标题行（第0行应为列名）");
            }
            
            // 检查是否有数据行（第1行应该是第一条数据）
            if (sheet.getLastRowNum() < 1) {
                return createImportErrorResult("Excel文件中没有数据行（第1行应为第一条数据）");
            }
            
            // 构建列名到列索引的映射
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell).trim();
                    columnIndexMap.put(cellValue, cell.getColumnIndex());
                }
            }
            
            // 读取所有有效数据行并转换为Patient对象
            List<Patient> patientList = new ArrayList<>();
            
            // 从第1行开始读取数据（索引从0开始：第0行是列名，第1行是第一条数据）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                // 读取患者ID
                Integer patientId = getCellValueAsInt(row, getColumnIndex(columnIndexMap, PatientColumnConstants.PATIENT_ID));
                if (patientId == null || patientId <= 0) {
                    continue; // 跳过无效的患者ID
                }
                
                // 创建Patient对象
                Patient patient = new Patient();
                patient.setPatientId(patientId);
                
                // 读取性别
                String gender = getCellValueAsString(row, getColumnIndex(columnIndexMap, PatientColumnConstants.GENDER));
                patient.setGender(cleanText(gender));
                
                // 读取年龄
                Integer age = getCellValueAsInt(row, getColumnIndex(columnIndexMap, PatientColumnConstants.AGE));
                patient.setAge(age);
                
                // 读取是否绿色通道（只能是"是"或"否"）
                String isGreenChannel = getCellValueAsString(row, getColumnIndex(columnIndexMap, PatientColumnConstants.IS_GREEN_CHANNEL));
                isGreenChannel = cleanText(isGreenChannel);
                if (isGreenChannel == null || isGreenChannel.isEmpty()) {
                    patient.setIsGreenChannel("否"); // 默认为"否"
                } else {
                    patient.setIsGreenChannel(isGreenChannel); // 已验证过，直接使用
                }
                
                // 读取身高（保留2位小数）
                Double height = getCellValueAsDouble(row, getColumnIndex(columnIndexMap, PatientColumnConstants.HEIGHT));
                if (height != null) {
                    BigDecimal heightDecimal = BigDecimal.valueOf(height).setScale(2, RoundingMode.HALF_UP);
                    patient.setHeight(heightDecimal.doubleValue());
                } else {
                    patient.setHeight(null);
                }
                
                // 读取体重（保留2位小数）
                Double weight = getCellValueAsDouble(row, getColumnIndex(columnIndexMap, PatientColumnConstants.WEIGHT));
                if (weight != null) {
                    BigDecimal weightDecimal = BigDecimal.valueOf(weight).setScale(2, RoundingMode.HALF_UP);
                    patient.setWeight(weightDecimal.doubleValue());
                } else {
                    patient.setWeight(null);
                }
                
                // 姓名字段不需要从Excel读取，数据库允许为NULL
                
                patientList.add(patient);
            }
            
            // 批量插入数据库
            int successCount = 0;
            int totalCount = patientList.size();
            List<Integer> duplicatePatientIds = new ArrayList<>();
            
            if (!patientList.isEmpty()) {
                try {
                    // 再次检查患者ID是否与数据库中的重复（防止并发插入）
                    Set<Integer> patientIds = patientList.stream()
                        .map(Patient::getPatientId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                    
                    List<Patient> existingPatients = patientMapper.selectList(new LambdaQueryWrapper<Patient>()
                        .in(Patient::getPatientId, patientIds)
                        .select(Patient::getPatientId));
                    
                    Set<Integer> existingIds = existingPatients.stream()
                        .map(Patient::getPatientId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                    
                    // 找出重复的患者ID
                    for (Patient patient : patientList) {
                        if (existingIds.contains(patient.getPatientId())) {
                            duplicatePatientIds.add(patient.getPatientId());
                        }
                    }
                    
                    // 如果有重复的患者ID，抛出异常并回滚
                    if (!duplicatePatientIds.isEmpty()) {
                        String errorMessage = "患者序号冲突: 以下患者ID在数据库中已存在: " + 
                            duplicatePatientIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", "));
                        logger.error(errorMessage);
                        throw new RuntimeException(errorMessage);
                    }
                    
                    // 使用批量插入方法（不使用ON DUPLICATE KEY UPDATE）
                    int affectedRows = patientMapper.insertBatch(patientList);
                    successCount = affectedRows;
                    logger.info("批量插入患者数据完成，影响行数: {}", affectedRows);
                } catch (RuntimeException e) {
                    // 患者ID重复异常，直接抛出以触发回滚
                    logger.error("批量插入患者数据时发现重复的患者ID", e);
                    throw e;
                } catch (Exception e) {
                    logger.error("批量插入患者数据时发生异常", e);
                    // 检查是否是主键冲突异常
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && (errorMessage.contains("Duplicate entry") || 
                        errorMessage.contains("PRIMARY") || 
                        errorMessage.contains("duplicate key"))) {
                        throw new RuntimeException("患者序号冲突: 插入时发现重复的患者ID", e);
                    }
                    throw new RuntimeException("批量插入数据时发生异常: " + e.getMessage(), e);
                }
            }
            
            // 构建导入结果
            ImportResultDTO result = new ImportResultDTO();
            int failedCount = totalCount - successCount;
            result.setSuccess(failedCount == 0);
            result.setSuccessCount(successCount);
            result.setFailedCount(failedCount);
            result.setStatus(failedCount == 0 ? "success" : (successCount > 0 ? "partial" : "failed"));
            
            if (failedCount == 0) {
                result.setMessage("数据导入成功，共导入 " + successCount + " 条记录");
            } else {
                result.setMessage("数据导入完成，成功 " + successCount + " 条，失败 " + failedCount + " 条");
            }
            
            logger.info("患者数据导入完成: 成功 {} 条，失败 {} 条", successCount, failedCount);
            
            return result;
            
        } catch (Exception e) {
            logger.error("导入数据时发生异常", e);
            return createImportErrorResult("导入数据时发生异常: " + e.getMessage());
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
        result.setFailedCount(0);
        result.setMessage(message);
        return result;
    }
    
    
    /**
     * 从列名映射中获取列索引（自动处理trim）
     * 
     * @param columnIndexMap 列名到列索引的映射
     * @param columnName 列名常量
     * @return 列索引，如果不存在则返回null
     */
    private Integer getColumnIndex(Map<String, Integer> columnIndexMap, String columnName) {
        if (columnIndexMap == null || columnName == null) {
            return null;
        }
        // 使用trim后的列名进行查找
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
     * 获取单元格的整数值（对应 Python 的 clean_int）
     */
    private Integer getCellValueAsInt(Row row, Integer columnIndex) {
        if (row == null || columnIndex == null) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        return cleanInt(cell);
    }
    
    /**
     * 清理并转换为整数（对应 Python 的 clean_int）
     */
    private Integer cleanInt(Cell cell) {
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
     * 获取单元格的浮点数值（对应 Python 的 clean_float）
     */
    private Double getCellValueAsDouble(Row row, Integer columnIndex) {
        if (row == null || columnIndex == null) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        return cleanFloat(cell);
    }
    
    /**
     * 清理并转换为浮点数（对应 Python 的 clean_float）
     */
    private Double cleanFloat(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
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
                    return Double.parseDouble(strValue);
                case BOOLEAN:
                    return cell.getBooleanCellValue() ? 1.0 : 0.0;
                case FORMULA:
                    try {
                        return cell.getNumericCellValue();
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
     * 清理文本（对应 Python 的 clean_text）
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
}


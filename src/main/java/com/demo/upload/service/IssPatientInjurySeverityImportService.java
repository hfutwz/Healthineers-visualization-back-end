package com.demo.upload.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.entity.IssInjury;
import com.demo.entity.Patient;
import com.demo.mapper.IssInjuryMapper;
import com.demo.mapper.PatientMapper;
import com.demo.upload.constants.IssColumnConstants;
import com.demo.upload.constants.BodyPartScoreMapping;
import com.demo.upload.dto.ImportResultDTO;
import com.demo.upload.dto.ValidationErrorDTO;
import com.demo.upload.dto.ValidationResultDTO;
import com.demo.upload.validator.IssFieldValidator;
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
 * ISS患者创伤严重度数据导入服务
 */
@Service
public class IssPatientInjurySeverityImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(IssPatientInjurySeverityImportService.class);
    
    @Autowired
    private IssInjuryMapper issInjuryMapper;
    
    @Autowired
    private IssFieldValidator fieldValidator;
    
    @Autowired
    private PatientMapper patientMapper;
    
    /**
     * 验证并导入ISS数据（一步完成）
     * 读取Excel全部数据，验证所有数据并记录所有错误
     * 错误数为0才插入数据库，否则返回所有错误信息
     * 
     * @param excelFilePath Excel文件路径
     * @return 包含验证结果和导入结果的Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> validateAndImportIssData(String excelFilePath) {
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
            for (String requiredColumn : IssColumnConstants.REQUIRED_COLUMNS) {
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
            List<IssInjury> validRecords = new ArrayList<>();
            
            // 从第1行开始读取数据（索引从0开始：第0行是列名，第1行是第一条数据）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                // 读取患者ID
                Integer patientId = getCellValueAsInt(row, getColumnIndex(columnIndexMap, IssColumnConstants.PATIENT_ID));
                int excelRowNumber = rowIndex + 1; // Excel行号（从1开始）
                
                // 验证患者ID是否有效
                if (patientId == null || patientId <= 0) {
                    allErrors.add(createValidationError(
                        excelRowNumber,
                        0,
                        "患者ID",
                        getCellValueAsString(row, getColumnIndex(columnIndexMap, IssColumnConstants.PATIENT_ID)),
                        "患者ID不能为空或无效"
                    ));
                    continue; // 跳过无效的患者ID
                }
                
                // 验证患者ID是否存在于patient表中
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
                
                // 验证各部位分值
                String headNeck = fieldValidator.validateIssScore(
                    row,
                    getColumnIndex(columnIndexMap, IssColumnConstants.HEAD_NECK),
                    excelRowNumber,
                    patientId,
                    "头颈部伤情等级",
                    allErrors
                );
                
                String face = fieldValidator.validateIssScore(
                    row,
                    getColumnIndex(columnIndexMap, IssColumnConstants.FACE),
                    excelRowNumber,
                    patientId,
                    "面部伤情等级",
                    allErrors
                );
                
                String chest = fieldValidator.validateIssScore(
                    row,
                    getColumnIndex(columnIndexMap, IssColumnConstants.CHEST),
                    excelRowNumber,
                    patientId,
                    "胸部伤情等级",
                    allErrors
                );
                
                String abdomen = fieldValidator.validateIssScore(
                    row,
                    getColumnIndex(columnIndexMap, IssColumnConstants.ABDOMEN),
                    excelRowNumber,
                    patientId,
                    "腹部伤情等级",
                    allErrors
                );
                
                String limbs = fieldValidator.validateIssScore(
                    row,
                    getColumnIndex(columnIndexMap, IssColumnConstants.LIMBS),
                    excelRowNumber,
                    patientId,
                    "四肢伤情等级",
                    allErrors
                );
                
                String body = fieldValidator.validateIssScore(
                    row,
                    getColumnIndex(columnIndexMap, IssColumnConstants.BODY),
                    excelRowNumber,
                    patientId,
                    "体表伤情等级",
                    allErrors
                );
                
                // 如果验证失败，跳过该行（错误已记录）
                if (headNeck == null || face == null || chest == null || abdomen == null || limbs == null || body == null) {
                    continue;
                }
                
                // 验证ISS评分
                Integer issScore = getCellValueAsInt(row, getColumnIndex(columnIndexMap, IssColumnConstants.ISS_SCORE));
                if (issScore == null) {
                    issScore = 0; // 默认为0
                }
                
                // 记录解析详细伤情前的错误数量
                int errorCountBefore = allErrors.size();
                
                // 解析详细伤情信息（完整实现，参考Python代码）
                Map<String, String> detailedInjuries = parseDetailedInjuries(
                    row, 
                    columnIndexMap, 
                    headNeck, 
                    face, 
                    chest, 
                    abdomen, 
                    limbs, 
                    body,
                    excelRowNumber,
                    patientId,
                    allErrors
                );
                
                // 检查是否有新增的错误
                int errorCountAfter = allErrors.size();
                if (errorCountAfter > errorCountBefore) {
                    // 有新增错误，跳过该行
                    continue;
                }
                
                String headNeckDetails = detailedInjuries.get("headNeck");
                String faceDetails = detailedInjuries.get("face");
                String chestDetails = detailedInjuries.get("chest");
                String abdomenDetails = detailedInjuries.get("abdomen");
                String limbsDetails = detailedInjuries.get("limbs");
                String bodyDetails = detailedInjuries.get("body");
                
                // 创建 IssInjury 对象
                IssInjury issInjury = new IssInjury();
                issInjury.setPatientId(patientId);
                issInjury.setHeadNeck(headNeck.equals("0") ? null : headNeck);
                issInjury.setFace(face.equals("0") ? null : face);
                issInjury.setChest(chest.equals("0") ? null : chest);
                issInjury.setAbdomen(abdomen.equals("0") ? null : abdomen);
                issInjury.setLimbs(limbs.equals("0") ? null : limbs);
                issInjury.setBody(body.equals("0") ? null : body);
                issInjury.setIssScore(issScore == 0 ? null : issScore);
                // 如果详细伤情为空字符串，设置为null
                issInjury.setHeadNeckDetails(headNeckDetails == null || headNeckDetails.isEmpty() ? null : headNeckDetails);
                issInjury.setFaceDetails(faceDetails == null || faceDetails.isEmpty() ? null : faceDetails);
                issInjury.setChestDetails(chestDetails == null || chestDetails.isEmpty() ? null : chestDetails);
                issInjury.setAbdomenDetails(abdomenDetails == null || abdomenDetails.isEmpty() ? null : abdomenDetails);
                issInjury.setLimbsDetails(limbsDetails == null || limbsDetails.isEmpty() ? null : limbsDetails);
                issInjury.setBodyDetails(bodyDetails == null || bodyDetails.isEmpty() ? null : bodyDetails);
                
                // 判断是否有详细伤情信息
                boolean hasDetails = (headNeckDetails != null && !headNeckDetails.isEmpty()) ||
                                   (faceDetails != null && !faceDetails.isEmpty()) ||
                                   (chestDetails != null && !chestDetails.isEmpty()) ||
                                   (abdomenDetails != null && !abdomenDetails.isEmpty()) ||
                                   (limbsDetails != null && !limbsDetails.isEmpty()) ||
                                   (bodyDetails != null && !bodyDetails.isEmpty());
                issInjury.setHasDetails(hasDetails);
                
                validRecords.add(issInjury);
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
            logger.info("数据验证通过，开始导入ISS数据...");
            ImportResultDTO importResult = importIssData(validRecords);
            
            result.put("validation", validationResult);
            result.put("import", importResult);
            result.put("success", true);
            result.put("message", "数据导入成功");
            
            return result;
            
        } catch (Exception e) {
            logger.error("验证并导入ISS数据时发生异常", e);
            ValidationResultDTO validationResult = createErrorResult("处理Excel文件时发生异常: " + e.getMessage());
            ImportResultDTO importResult = createImportErrorResult("处理Excel文件时发生异常: " + e.getMessage());
            result.put("validation", validationResult);
            result.put("import", importResult);
            result.put("success", false);
            result.put("message", "处理Excel文件时发生异常: " + e.getMessage());
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
     * 导入ISS数据（纯Java实现）
     * 注意：此方法假设数据已经通过验证，直接插入数据库
     * 
     * @param issInjuries 已验证的ISS记录列表
     * @return 导入结果
     */
    private ImportResultDTO importIssData(List<IssInjury> issInjuries) {
        try {
            int insertCount = 0;  // 新插入的记录数
            int updateCount = 0;  // 更新的记录数
            int totalCount = issInjuries.size();
            
            if (!issInjuries.isEmpty()) {
                try {
                    // 使用批量插入方法（使用 ON DUPLICATE KEY UPDATE）
                    for (IssInjury record : issInjuries) {
                        // 检查是否已存在相同的 patient_id 记录
                        LambdaQueryWrapper<IssInjury> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(IssInjury::getPatientId, record.getPatientId());
                        List<IssInjury> existingList = issInjuryMapper.selectList(queryWrapper);
                        
                        if (existingList != null && !existingList.isEmpty()) {
                            // 如果存在多条记录，抛出错误
                            if (existingList.size() > 1) {
                                throw new RuntimeException("重复了");
                            }
                            
                            // 更新现有记录（只有一条）
                            IssInjury existing = existingList.get(0);
                            record.setInjuryId(existing.getInjuryId());
                            issInjuryMapper.updateById(record);
                            updateCount++;
                            logger.debug("更新已存在的ISS记录，患者ID: {}", record.getPatientId());
                        } else {
                            // 插入新记录
                            issInjuryMapper.insert(record);
                            insertCount++;
                            logger.debug("插入新的ISS记录，患者ID: {}", record.getPatientId());
                        }
                    }
                } catch (RuntimeException e) {
                    // 重新抛出RuntimeException，保持错误信息
                    throw e;
                } catch (Exception e) {
                    logger.error("批量导入ISS数据时发生异常", e);
                    // 检查是否是TooManyResultsException
                    if (e.getMessage() != null && e.getMessage().contains("TooManyResultsException")) {
                        throw new RuntimeException("重复了", e);
                    }
                    throw e;
                }
            }
            
            ImportResultDTO importResult = new ImportResultDTO();
            importResult.setSuccess(true);
            importResult.setStatus("success");
            importResult.setSuccessCount(totalCount);
            importResult.setInsertCount(insertCount);
            importResult.setUpdateCount(updateCount);
            importResult.setFailedCount(0);
            importResult.setMessage("ISS数据导入成功，共插入 " + insertCount + " 条，更新 " + updateCount + " 条，总计 " + totalCount + " 条");
            
            logger.info("ISS数据导入完成，共插入 {} 条，更新 {} 条，总计 {} 条", insertCount, updateCount, totalCount);
            
            return importResult;
            
        } catch (Exception e) {
            logger.error("导入ISS数据时发生异常", e);
            ImportResultDTO importResult = new ImportResultDTO();
            importResult.setSuccess(false);
            importResult.setStatus("failed");
            importResult.setSuccessCount(0);
            importResult.setInsertCount(0);
            importResult.setUpdateCount(0);
            importResult.setFailedCount(issInjuries.size());
            importResult.setMessage("导入失败: " + e.getMessage());
            return importResult;
        }
    }
    
    /**
     * 解析详细伤情信息（完整实现，参考Python代码中的parse_detailed_injuries方法）
     * 根据分值查找对应的列，并格式化输出
     * 
     * @param row 数据行
     * @param columnIndexMap 列名到列索引的映射
     * @param headNeck 头颈部分值
     * @param face 面部分值
     * @param chest 胸部分值
     * @param abdomen 腹部分值
     * @param limbs 四肢分值
     * @param body 体表分值
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param allErrors 错误列表
     * @return 详细伤情信息Map，如果解析失败返回null
     */
    private Map<String, String> parseDetailedInjuries(
            Row row,
            Map<String, Integer> columnIndexMap,
            String headNeck,
            String face,
            String chest,
            String abdomen,
            String limbs,
            String body,
            int excelRowNumber,
            int patientId,
            List<ValidationErrorDTO> allErrors) {
        
        Map<String, String> detailedInjuries = new HashMap<>();
        detailedInjuries.put("headNeck", "");
        detailedInjuries.put("face", "");
        detailedInjuries.put("chest", "");
        detailedInjuries.put("abdomen", "");
        detailedInjuries.put("limbs", "");
        detailedInjuries.put("body", "");
        
        // 获取映射关系
        Map<String, Map<Integer, List<String>>> bodyPartScoreMapping = BodyPartScoreMapping.getMapping();
        
        // 各部位的分值
        Map<String, String> scores = new HashMap<>();
        scores.put("headNeck", headNeck);
        scores.put("face", face);
        scores.put("chest", chest);
        scores.put("abdomen", abdomen);
        scores.put("limbs", limbs);
        scores.put("body", body);
        
        // 处理每个部位
        for (Map.Entry<String, String> scoreEntry : scores.entrySet()) {
            String bodyPart = scoreEntry.getKey();
            String scoreStr = scoreEntry.getValue();
            
            // 如果分值为"0"或空，跳过
            if (scoreStr == null || scoreStr.equals("0")) {
                continue;
            }
            
            // 获取该部位的所有分值（只处理数字部分）
            List<Integer> scoreList = getScoreList(scoreStr);
            
            if (scoreList.isEmpty()) {
                // 如果没有有效数字，跳过
                continue;
            }
            
            // 按分值分组收集伤情项目
            Map<Integer, List<String>> scoreGroups = new HashMap<>();
            Map<Integer, Set<String>> foundColumns = new HashMap<>(); // 记录找到的列名，用于验证
            
            // 获取该部位的映射
            Map<Integer, List<String>> partMapping = bodyPartScoreMapping.get(bodyPart);
            if (partMapping == null) {
                continue;
            }
            
            // 对于每个分值，查找匹配的列
            for (Integer score : scoreList) {
                List<String> expectedColumns = partMapping.get(score);
                if (expectedColumns == null || expectedColumns.isEmpty()) {
                    // 如果映射中没有该分值，记录错误
                    String bodyPartName = getBodyPartName(bodyPart);
                    allErrors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        bodyPartName + "详细伤情",
                        "分值" + score,
                        bodyPartName + "分值" + score + "在映射表中不存在"
                    ));
                    continue;
                }
                
                // 查找匹配的列
                Set<String> foundCols = new HashSet<>();
                for (String expectedCol : expectedColumns) {
                    // 在Excel列名中查找包含expectedCol的列
                    for (Map.Entry<String, Integer> colEntry : columnIndexMap.entrySet()) {
                        String actualColName = colEntry.getKey();
                        if (actualColName.contains(expectedCol)) {
                            foundCols.add(actualColName);
                            
                            // 读取该列的值
                            String value = getCellValueAsString(row, colEntry.getValue());
                            if (value != null && !value.trim().isEmpty() && 
                                !value.trim().equals("(空)") && !value.trim().equals("无")) {
                                
                                // 提取伤情描述
                                String description = BodyPartScoreMapping.extractDescription(actualColName);
                                
                                // 按分值分组
                                scoreGroups.computeIfAbsent(score, k -> new ArrayList<>()).add(description);
                            }
                        }
                    }
                }
                
                foundColumns.put(score, foundCols);
            }
            
            // 验证：如果分值存在但找不到对应的详细信息列，记录错误
            for (Integer score : scoreList) {
                Set<String> foundCols = foundColumns.get(score);
                if (foundCols == null || foundCols.isEmpty()) {
                    // 该分值没有找到任何匹配的列，记录错误
                    String bodyPartName = getBodyPartName(bodyPart);
                    List<String> expectedColumns = partMapping.get(score);
                    if (expectedColumns != null && !expectedColumns.isEmpty()) {
                        allErrors.add(createValidationError(
                            excelRowNumber,
                            patientId,
                            bodyPartName + "详细伤情",
                            "分值" + score,
                            bodyPartName + "分值" + score + "在Excel中找不到对应的详细伤情列（期望列名包含：" + 
                            String.join("、", expectedColumns.subList(0, Math.min(3, expectedColumns.size()))) + "等）"
                        ));
                    }
                } else {
                    // 检查是否有值（非空）
                    List<String> items = scoreGroups.get(score);
                    if (items == null || items.isEmpty()) {
                        // 找到了列但没有值，记录错误
                        String bodyPartName = getBodyPartName(bodyPart);
                        allErrors.add(createValidationError(
                            excelRowNumber,
                            patientId,
                            bodyPartName + "详细伤情",
                            "分值" + score,
                            bodyPartName + "分值" + score + "对应的详细伤情列为空"
                        ));
                    }
                }
            }
            
            // 格式化输出（只显示有伤情的部位）
            if (!scoreGroups.isEmpty()) {
                List<String> formattedParts = new ArrayList<>();
                // 按分值从高到低排序
                List<Integer> sortedScores = new ArrayList<>(scoreGroups.keySet());
                sortedScores.sort(Collections.reverseOrder());
                
                for (Integer score : sortedScores) {
                    List<String> items = scoreGroups.get(score);
                    if (items != null && !items.isEmpty()) {
                        formattedParts.add(score + "分（" + String.join("，", items) + "）");
                    }
                }
                
                if (!formattedParts.isEmpty()) {
                    detailedInjuries.put(bodyPart, String.join("，", formattedParts));
                }
            }
        }
        
        // 如果有错误，返回null表示解析失败
        // 注意：这里不直接返回null，而是让调用者检查allErrors
        // 因为我们需要收集所有错误，而不是遇到第一个错误就停止
        
        return detailedInjuries;
    }
    
    /**
     * 从多分值字符串中获取分值列表，只处理数字部分
     * 参考Python代码中的get_score_list方法
     */
    private List<Integer> getScoreList(String scoreStr) {
        List<Integer> scoreList = new ArrayList<>();
        
        if (scoreStr == null || scoreStr.equals("0")) {
            return scoreList;
        }
        
        if (scoreStr.contains("|")) {
            // 处理多个分值
            String[] parts = scoreStr.split("\\|");
            for (String part : parts) {
                part = part.trim();
                if (part.matches("\\d+")) {
                    try {
                        scoreList.add(Integer.parseInt(part));
                    } catch (NumberFormatException e) {
                        // 忽略无效数字
                    }
                }
            }
        } else {
            // 处理单个分值
            if (scoreStr.matches("\\d+")) {
                try {
                    scoreList.add(Integer.parseInt(scoreStr));
                } catch (NumberFormatException e) {
                    // 忽略无效数字
                }
            }
        }
        
        return scoreList;
    }
    
    /**
     * 获取部位的中文名称
     */
    private String getBodyPartName(String bodyPart) {
        switch (bodyPart) {
            case "headNeck":
                return "头颈部";
            case "face":
                return "面部";
            case "chest":
                return "胸部";
            case "abdomen":
                return "腹部";
            case "limbs":
                return "四肢";
            case "body":
                return "体表";
            default:
                return bodyPart;
        }
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 获取单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
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
     * 获取行的单元格字符串值
     */
    private String getCellValueAsString(Row row, Integer columnIndex) {
        if (row == null || columnIndex == null) {
            return "";
        }
        Cell cell = row.getCell(columnIndex);
        return getCellValueAsString(cell);
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
                case STRING:
                    String stringValue = cell.getStringCellValue().trim();
                    if (stringValue.isEmpty()) {
                        return null;
                    }
                    try {
                        return Integer.parseInt(stringValue);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                case NUMERIC:
                    double numericValue = cell.getNumericCellValue();
                    return (int) numericValue;
                case FORMULA:
                    try {
                        return (int) cell.getNumericCellValue();
                    } catch (Exception e) {
                        try {
                            String formulaStringValue = cell.getStringCellValue().trim();
                            return Integer.parseInt(formulaStringValue);
                        } catch (Exception ex) {
                            return null;
                        }
                    }
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取列索引
     */
    private Integer getColumnIndex(Map<String, Integer> columnIndexMap, String columnName) {
        return columnIndexMap.get(columnName);
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
        result.setErrors(new ArrayList<>());
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


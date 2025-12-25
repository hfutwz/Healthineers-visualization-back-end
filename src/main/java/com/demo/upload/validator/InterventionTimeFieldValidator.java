package com.demo.upload.validator;

import com.demo.upload.dto.ValidationErrorDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 干预时间字段验证器
 * 负责所有字段的格式验证逻辑
 */
@Component
public class InterventionTimeFieldValidator {
    
    /**
     * 日期格式：YYYY-MM-DD
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 日期格式正则表达式：严格匹配 YYYY-MM-DD
     */
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    
    /**
     * 4位时间格式正则表达式：严格匹配 0000-2359
     */
    private static final Pattern TIME_4_DIGIT_PATTERN = Pattern.compile("^\\d{4}$");
    
    /**
     * 匹配 有:〖数字〗 格式的正则表达式
     */
    private static final Pattern HAS_TIME_PATTERN = Pattern.compile("有:〖(\\d{4})〗");
    
    /**
     * 匹配 是:〖数字〗 格式的正则表达式
     */
    private static final Pattern YES_TIME_PATTERN = Pattern.compile("是:〖(\\d{4})〗");
    
    /**
     * 匹配 有，开始时间:〖数字〗 格式的正则表达式
     */
    private static final Pattern VENTILATOR_PATTERN = Pattern.compile("有，开始时间:〖(\\d{4})〗");
    
    /**
     * 验证接诊日期（复用 InjuryRecordFieldValidator 的逻辑）
     */
    public LocalDate validateAdmissionDate(Row row, Integer columnIndex, int excelRowNumber, int patientId, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                "接诊日期",
                "",
                "接诊日期不能为空"
            ));
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                "接诊日期",
                "",
                "接诊日期不能为空"
            ));
            return null;
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        LocalDate admissionDate = null;
        
        try {
            String stringValue = null;
            
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                stringValue = cell.getStringCellValue().trim();
            } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        java.util.Date dateValue = cell.getDateCellValue();
                        admissionDate = dateValue.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                        String formattedDate = admissionDate.format(DATE_FORMATTER);
                        if (!DATE_PATTERN.matcher(formattedDate).matches()) {
                            errors.add(createValidationError(
                                excelRowNumber,
                                patientId,
                                "接诊日期",
                                rawValue,
                                "接诊日期格式不正确: " + formattedDate + "（必须是 YYYY-MM-DD 格式，例如 2024-10-29）"
                            ));
                            return null;
                        }
                        return admissionDate;
                    } catch (Exception e) {
                        errors.add(createValidationError(
                            excelRowNumber,
                            patientId,
                            "接诊日期",
                            rawValue,
                            "接诊日期格式不正确: " + (rawValue != null ? rawValue.toString() : "") + "（必须是 YYYY-MM-DD 格式，例如 2024-10-29）"
                        ));
                        return null;
                    }
                } else {
                    stringValue = String.valueOf((long) cell.getNumericCellValue());
                }
            }
            
            if (stringValue == null || stringValue.isEmpty()) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "接诊日期",
                    rawValue,
                    "接诊日期不能为空"
                ));
                return null;
            }
            
            if (!DATE_PATTERN.matcher(stringValue).matches()) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "接诊日期",
                    rawValue,
                    "接诊日期格式不正确: " + stringValue + "（必须是 YYYY-MM-DD 格式，例如 2024-10-29）"
                ));
                return null;
            }
            
            try {
                admissionDate = LocalDate.parse(stringValue, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "接诊日期",
                    rawValue,
                    "接诊日期格式不正确: " + stringValue + "（必须是 YYYY-MM-DD 格式，例如 2024-10-29）"
                ));
                return null;
            }
            
        } catch (Exception e) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                "接诊日期",
                rawValue,
                "接诊日期格式不正确: " + (rawValue != null ? rawValue.toString() : "") + "（必须是 YYYY-MM-DD 格式，例如 2024-10-29）"
            ));
            return null;
        }
        
        return admissionDate;
    }
    
    /**
     * 验证接诊时间（复用 InjuryRecordFieldValidator 的逻辑）
     */
    public String validateAdmissionTime(Row row, Integer columnIndex, int excelRowNumber, int patientId, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                "接诊时间",
                "",
                "接诊时间不能为空"
            ));
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                "接诊时间",
                "",
                "接诊时间不能为空"
            ));
            return null;
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        String admissionTime = null;
        
        try {
            String stringValue = null;
            
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                stringValue = cell.getStringCellValue().trim();
            } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    stringValue = String.format("%04d", (long) numericValue);
                } else {
                    stringValue = String.valueOf(numericValue);
                }
            }
            
            if (stringValue == null || stringValue.isEmpty()) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "接诊时间",
                    rawValue,
                    "接诊时间不能为空"
                ));
                return null;
            }
            
            // 验证是否为4位数字
            if (!TIME_4_DIGIT_PATTERN.matcher(stringValue).matches()) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "接诊时间",
                    rawValue,
                    "接诊时间格式不正确: " + stringValue + "（必须是4位数字，例如 1100）"
                ));
                return null;
            }
            
            // 验证时间范围（0000-2359）
            int hour = Integer.parseInt(stringValue.substring(0, 2));
            int minute = Integer.parseInt(stringValue.substring(2, 4));
            if (hour >= 24 || minute >= 60) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "接诊时间",
                    rawValue,
                    "接诊时间超出范围: " + stringValue + "（小时必须在00-23之间，分钟必须在00-59之间）"
                ));
                return null;
            }
            
            admissionTime = stringValue;
            
        } catch (Exception e) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                "接诊时间",
                rawValue,
                "接诊时间格式不正确: " + (rawValue != null ? rawValue.toString() : "") + "（必须是4位数字，例如 1100）"
            ));
            return null;
        }
        
        return admissionTime;
    }
    
    /**
     * 验证时间值字段（格式：无、有:〖0958〗）
     * 如果数据是"有:"但没有数字，当作"无"处理，不记录错误
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param fieldName 字段名称（用于错误提示）
     * @param errors 错误列表
     * @return 验证后的时间值（4位数字字符串），如果无效或为空返回null
     */
    public String validateTimeValueField(Row row, Integer columnIndex, int excelRowNumber, int patientId, String fieldName, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        String stringValue = getCellValueAsString(cell);
        
        if (stringValue == null || stringValue.trim().isEmpty()) {
            return null;
        }
        
        stringValue = stringValue.trim();
        
        // 如果是"无"，返回null
        if ("无".equals(stringValue)) {
            return null;
        }
        
        // 如果是"有:"但没有数字，当作"无"处理，不记录错误
        if (stringValue.equals("有:") || stringValue.startsWith("有:") && !HAS_TIME_PATTERN.matcher(stringValue).find()) {
            return null;
        }
        
        // 匹配 有:〖数字〗 格式
        java.util.regex.Matcher matcher = HAS_TIME_PATTERN.matcher(stringValue);
        if (matcher.find()) {
            String timeStr = matcher.group(1);
            // 验证是否为4位数字
            if (!TIME_4_DIGIT_PATTERN.matcher(timeStr).matches()) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    fieldName,
                    rawValue,
                    fieldName + "格式不正确: " + stringValue + "（括号内必须是4位数字，例如 有:〖0958〗）"
                ));
                return null;
            }
            
            // 验证时间范围（0000-2359）
            int hour = Integer.parseInt(timeStr.substring(0, 2));
            int minute = Integer.parseInt(timeStr.substring(2, 4));
            if (hour >= 24 || minute >= 60) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    fieldName,
                    rawValue,
                    fieldName + "时间超出范围: " + timeStr + "（小时必须在00-23之间，分钟必须在00-59之间）"
                ));
                return null;
            }
            
            return timeStr;
        }
        
        // 如果格式不匹配，记录错误
        errors.add(createValidationError(
            excelRowNumber,
            patientId,
            fieldName,
            rawValue,
            fieldName + "格式不正确: " + stringValue + "（正常格式：无、有:〖0958〗，括号内必须是4位数字）"
        ));
        return null;
    }
    
    /**
     * 验证呼吸机字段（格式：无、有，开始时间:〖0910〗）
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param errors 错误列表
     * @return 验证后的时间值（4位数字字符串），如果无效或为空返回null
     */
    public String validateVentilatorField(Row row, Integer columnIndex, int excelRowNumber, int patientId, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        String stringValue = getCellValueAsString(cell);
        
        if (stringValue == null || stringValue.trim().isEmpty()) {
            return null;
        }
        
        stringValue = stringValue.trim();
        
        // 如果是"无"，返回null
        if ("无".equals(stringValue)) {
            return null;
        }
        
        // 匹配 有，开始时间:〖数字〗 格式
        java.util.regex.Matcher matcher = VENTILATOR_PATTERN.matcher(stringValue);
        if (matcher.find()) {
            String timeStr = matcher.group(1);
            // 验证是否为4位数字
            if (!TIME_4_DIGIT_PATTERN.matcher(timeStr).matches()) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "呼吸机",
                    rawValue,
                    "呼吸机格式不正确: " + stringValue + "（括号内必须是4位数字，例如 有，开始时间:〖0910〗）"
                ));
                return null;
            }
            
            // 验证时间范围（0000-2359）
            int hour = Integer.parseInt(timeStr.substring(0, 2));
            int minute = Integer.parseInt(timeStr.substring(2, 4));
            if (hour >= 24 || minute >= 60) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "呼吸机",
                    rawValue,
                    "呼吸机时间超出范围: " + timeStr + "（小时必须在00-23之间，分钟必须在00-59之间）"
                ));
                return null;
            }
            
            return timeStr;
        }
        
        // 如果格式不匹配，记录错误
        errors.add(createValidationError(
            excelRowNumber,
            patientId,
            "呼吸机",
            rawValue,
            "呼吸机格式不正确: " + stringValue + "（正常格式：无、有，开始时间:〖xxxx〗，括号内必须是4位数字）"
        ));
        return null;
    }
    
    /**
     * 验证是/否字段（格式：是/否）
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param fieldName 字段名称
     * @param errors 错误列表
     * @return 验证后的值（"是"或"否"），如果无效返回null
     */
    public String validateYesNoField(Row row, Integer columnIndex, int excelRowNumber, int patientId, String fieldName, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        String stringValue = getCellValueAsString(cell);
        
        if (stringValue == null || stringValue.trim().isEmpty()) {
            return null;
        }
        
        stringValue = stringValue.trim();
        
        if ("是".equals(stringValue)) {
            return "是";
        } else if ("否".equals(stringValue)) {
            return "否";
        } else {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                fieldName,
                rawValue,
                fieldName + "格式不正确: " + stringValue + "（只能是\"是\"或\"否\"）"
            ));
            return null;
        }
    }
    
    /**
     * 验证4位时间字段（格式：1600 或 (跳过)）
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param fieldName 字段名称
     * @param errors 错误列表
     * @return 验证后的时间值（4位数字字符串），如果无效或为(跳过)返回null
     */
    public String validate4DigitTimeField(Row row, Integer columnIndex, int excelRowNumber, int patientId, String fieldName, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        String stringValue = getCellValueAsString(cell);
        
        if (stringValue == null || stringValue.trim().isEmpty()) {
            return null;
        }
        
        stringValue = stringValue.trim();
        
        // 如果是"(跳过)"，返回null
        if ("(跳过)".equals(stringValue)) {
            return null;
        }
        
        // 验证是否为4位数字
        if (!TIME_4_DIGIT_PATTERN.matcher(stringValue).matches()) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                fieldName,
                rawValue,
                fieldName + "格式不正确: " + stringValue + "（必须是4位数字，例如 1600）或(跳过)"
            ));
            return null;
        }
        
        // 验证时间范围（0000-2359）
        int hour = Integer.parseInt(stringValue.substring(0, 2));
        int minute = Integer.parseInt(stringValue.substring(2, 4));
        if (hour >= 24 || minute >= 60) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                fieldName,
                rawValue,
                fieldName + "时间超出范围: " + stringValue + "（小时必须在00-23之间，分钟必须在00-59之间）"
            ));
            return null;
        }
        
        return stringValue;
    }
    
    /**
     * 验证是/否时间字段（格式：是:〖0002〗、否）
     * 对于采血和CT字段：如果为"是:"但没有数字，视作"否"，不记录错误
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param fieldName 字段名称
     * @param errors 错误列表
     * @return 验证后的时间值（4位数字字符串），如果无效或为空返回null
     */
    public String validateYesNoTimeField(Row row, Integer columnIndex, int excelRowNumber, int patientId, String fieldName, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        String stringValue = getCellValueAsString(cell);
        
        if (stringValue == null || stringValue.trim().isEmpty()) {
            return null;
        }
        
        stringValue = stringValue.trim();
        
        // 如果是"否"，返回null
        if ("否".equals(stringValue)) {
            return null;
        }
        
        // 检查是否包含"是:〖"格式（无论数字位数）
        boolean hasYesTimePattern = stringValue.contains("是:〖") && stringValue.contains("〗");
        
        // 如果包含"是:〖"格式，需要验证数字位数
        if (hasYesTimePattern) {
            // 提取括号内的数字
            java.util.regex.Pattern extractPattern = Pattern.compile("是:〖(\\d+)〗");
            java.util.regex.Matcher extractMatcher = extractPattern.matcher(stringValue);
            if (extractMatcher.find()) {
                String extractedDigits = extractMatcher.group(1);
                // 如果数字不是4位，记录错误
                if (extractedDigits.length() != 4) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        fieldName,
                        rawValue,
                        fieldName + "格式不正确: " + stringValue + "（括号内必须是4位数字，例如 是:〖0002〗，当前为" + extractedDigits.length() + "位数字）"
                    ));
                    return null;
                }
            }
        }
        
        // 对于采血和CT字段：如果为"是:"但没有数字部分，视作"否"，不记录错误
        if (("采血".equals(fieldName) || "CT".equals(fieldName)) && "是:".equals(stringValue)) {
            return null;
        }
        
        // 匹配 是:〖数字〗 格式（必须是4位数字）
        java.util.regex.Matcher matcher = YES_TIME_PATTERN.matcher(stringValue);
        if (matcher.find()) {
            String timeStr = matcher.group(1);
            // 验证是否为4位数字（这个检查实际上已经在上面做了，但保留作为双重检查）
            if (!TIME_4_DIGIT_PATTERN.matcher(timeStr).matches()) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    fieldName,
                    rawValue,
                    fieldName + "格式不正确: " + stringValue + "（括号内必须是4位数字，例如 是:〖0002〗）"
                ));
                return null;
            }
            
            // 验证时间范围（0000-2359）
            int hour = Integer.parseInt(timeStr.substring(0, 2));
            int minute = Integer.parseInt(timeStr.substring(2, 4));
            if (hour >= 24 || minute >= 60) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    fieldName,
                    rawValue,
                    fieldName + "时间超出范围: " + timeStr + "（小时必须在00-23之间，分钟必须在00-59之间）"
                ));
                return null;
            }
            
            return timeStr;
        }
        
        // 如果格式不匹配，记录错误
        errors.add(createValidationError(
            excelRowNumber,
            patientId,
            fieldName,
            rawValue,
            fieldName + "格式不正确: " + stringValue + "（正常格式：是:〖0002〗、否，括号内必须是4位数字）"
        ));
        return null;
    }
    
    /**
     * 验证死亡日期（格式：YYYY-MM-DD 或 (跳过)）
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param errors 错误列表
     * @return 验证后的日期，如果无效或为(跳过)返回null
     */
    public LocalDate validateDeathDate(Row row, Integer columnIndex, int excelRowNumber, int patientId, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        LocalDate deathDate = null;
        
        try {
            String stringValue = null;
            
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                stringValue = cell.getStringCellValue().trim();
            } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        java.util.Date dateValue = cell.getDateCellValue();
                        deathDate = dateValue.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                        String formattedDate = deathDate.format(DATE_FORMATTER);
                        if (!DATE_PATTERN.matcher(formattedDate).matches()) {
                            errors.add(createValidationError(
                                excelRowNumber,
                                patientId,
                                "死亡日期",
                                rawValue,
                                "死亡日期格式不正确: " + formattedDate + "（必须是 YYYY-MM-DD 格式，例如 2024-10-29）或(跳过)"
                            ));
                            return null;
                        }
                        return deathDate;
                    } catch (Exception e) {
                        errors.add(createValidationError(
                            excelRowNumber,
                            patientId,
                            "死亡日期",
                            rawValue,
                            "死亡日期格式不正确: " + (rawValue != null ? rawValue.toString() : "") + "（必须是 YYYY-MM-DD 格式，例如 2024-10-29）或(跳过)"
                        ));
                        return null;
                    }
                } else {
                    stringValue = String.valueOf((long) cell.getNumericCellValue());
                }
            }
            
            if (stringValue == null || stringValue.isEmpty()) {
                return null; // 死亡日期可以为空
            }
            
            // 如果是"(跳过)"，返回null
            if ("(跳过)".equals(stringValue)) {
                return null;
            }
            
            if (!DATE_PATTERN.matcher(stringValue).matches()) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "死亡日期",
                    rawValue,
                    "死亡日期格式不正确: " + stringValue + "（必须是 YYYY-MM-DD 格式，例如 2024-10-29）或(跳过)"
                ));
                return null;
            }
            
            try {
                deathDate = LocalDate.parse(stringValue, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "死亡日期",
                    rawValue,
                    "死亡日期格式不正确: " + stringValue + "（必须是 YYYY-MM-DD 格式，例如 2024-10-29）或(跳过)"
                ));
                return null;
            }
            
        } catch (Exception e) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                "死亡日期",
                rawValue,
                "死亡日期格式不正确: " + (rawValue != null ? rawValue.toString() : "") + "（必须是 YYYY-MM-DD 格式，例如 2024-10-29）或(跳过)"
            ));
            return null;
        }
        
        return deathDate;
    }
    
    /**
     * 解析离开抢救室时间（支持 MM-DD HHMM、MM-D HHMM 和 HHMM 格式）
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param admissionDate 接诊日期
     * @param admissionTime 接诊时间
     * @param errors 错误列表
     * @return 返回数组 [leaveDate, leaveTime]，如果无效返回 [null, null]
     */
    public Object[] parseLeaveSurgeryTime(Row row, Integer columnIndex, int excelRowNumber, int patientId, LocalDate admissionDate, String admissionTime, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            return new Object[]{null, null};
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return new Object[]{null, null};
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        String stringValue = getCellValueAsString(cell);
        
        if (stringValue == null || stringValue.trim().isEmpty()) {
            return new Object[]{null, null};
        }
        
        stringValue = stringValue.trim();
        
        // 匹配 "MM-DD HHMM" 或 "MM-D HHMM" 格式（支持月份后跟1-2位日期）
        Pattern dateTimePattern = Pattern.compile("(\\d{2}-\\d{1,2})\\s+(\\d{4})");
        java.util.regex.Matcher dateTimeMatcher = dateTimePattern.matcher(stringValue);
        if (dateTimeMatcher.find()) {
            String monthDay = dateTimeMatcher.group(1);
            String timeStr = dateTimeMatcher.group(2);
            
            // 验证时间格式
            if (!TIME_4_DIGIT_PATTERN.matcher(timeStr).matches()) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "离开抢救室时间",
                    rawValue,
                    "离开抢救室时间格式不正确: " + timeStr + "（必须是4位数字）"
                ));
                return new Object[]{null, null};
            }
            
            // 验证时间范围
            int hour = Integer.parseInt(timeStr.substring(0, 2));
            int minute = Integer.parseInt(timeStr.substring(2, 4));
            if (hour >= 24 || minute >= 60) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "离开抢救室时间",
                    rawValue,
                    "离开抢救室时间超出范围: " + timeStr + "（小时必须在00-23之间，分钟必须在00-59之间）"
                ));
                return new Object[]{null, null};
            }
            
            try {
                // 处理日期格式：如果是 MM-D 格式，需要补零为 MM-0D
                String normalizedMonthDay = monthDay;
                if (monthDay.matches("\\d{2}-\\d{1}$")) {
                    // 格式为 MM-D，需要补零为 MM-0D
                    String[] parts = monthDay.split("-");
                    normalizedMonthDay = parts[0] + "-0" + parts[1];
                }
                
                int currentYear = admissionDate != null ? admissionDate.getYear() : java.time.LocalDate.now().getYear();
                LocalDate leaveDate = LocalDate.parse(currentYear + "-" + normalizedMonthDay, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (admissionDate != null && leaveDate.isBefore(admissionDate)) {
                    leaveDate = leaveDate.plusYears(1);
                }
                return new Object[]{leaveDate, timeStr};
            } catch (Exception e) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "离开抢救室时间",
                    rawValue,
                    "离开抢救室日期格式不正确: " + monthDay
                ));
                return new Object[]{null, timeStr};
            }
        }
        
        // 匹配 "HHMM" 格式
        Pattern timePattern = Pattern.compile("^(\\d{4})$");
        java.util.regex.Matcher timeMatcher = timePattern.matcher(stringValue);
        if (timeMatcher.find()) {
            String timeStr = timeMatcher.group(1);
            
            // 验证时间格式
            if (!TIME_4_DIGIT_PATTERN.matcher(timeStr).matches()) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "离开抢救室时间",
                    rawValue,
                    "离开抢救室时间格式不正确: " + timeStr + "（必须是4位数字）"
                ));
                return new Object[]{null, null};
            }
            
            // 验证时间范围
            int hour = Integer.parseInt(timeStr.substring(0, 2));
            int minute = Integer.parseInt(timeStr.substring(2, 4));
            if (hour >= 24 || minute >= 60) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "离开抢救室时间",
                    rawValue,
                    "离开抢救室时间超出范围: " + timeStr + "（小时必须在00-23之间，分钟必须在00-59之间）"
                ));
                return new Object[]{null, null};
            }
            
            // 如果接诊日期和时间都存在，判断日期
            if (admissionDate != null && admissionTime != null) {
                try {
                    int admissionHour = Integer.parseInt(admissionTime.substring(0, 2));
                    int admissionMinute = Integer.parseInt(admissionTime.substring(2, 4));
                    int leaveHour = Integer.parseInt(timeStr.substring(0, 2));
                    int leaveMinute = Integer.parseInt(timeStr.substring(2, 4));
                    
                    java.time.LocalTime admissionTimeObj = java.time.LocalTime.of(admissionHour, admissionMinute);
                    java.time.LocalTime leaveTimeObj = java.time.LocalTime.of(leaveHour, leaveMinute);
                    
                    if (leaveTimeObj.isAfter(admissionTimeObj) || leaveTimeObj.equals(admissionTimeObj)) {
                        return new Object[]{admissionDate, timeStr};
                    } else {
                        return new Object[]{admissionDate.plusDays(1), timeStr};
                    }
                } catch (Exception e) {
                    return new Object[]{admissionDate, timeStr};
                }
            }
            
            return new Object[]{admissionDate, timeStr};
        }
        
        // 格式不匹配，不记录错误，直接返回null（用户要求：格式不正确的数据不记录）
        return new Object[]{null, null};
    }
    
    // ==================== 辅助方法 ====================
    
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
                        return String.format("%04d", (long) numericValue);
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


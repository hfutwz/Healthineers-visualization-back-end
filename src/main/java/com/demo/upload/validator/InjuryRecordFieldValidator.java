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
 * 创伤病例字段验证器
 * 负责所有字段的格式验证逻辑
 */
@Component
public class InjuryRecordFieldValidator {
    
    /**
     * 日期格式：YYYY-MM-DD
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 日期格式正则表达式：严格匹配 YYYY-MM-DD
     */
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    
    /**
     * 验证接诊日期（必须是 YYYY-MM-DD 格式，例如 2024-10-29）
     * 其他格式如 YYYY/MM/DD, YYYY-MM/DD, YYYY-MM-D 都是非法的
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号（从1开始）
     * @param patientId 患者ID
     * @param errors 错误列表
     * @return 验证后的接诊日期，如果无效返回null
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
            
            // 根据单元格类型处理
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                stringValue = cell.getStringCellValue().trim();
            } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Excel日期格式，转换为 LocalDate
                    try {
                        java.util.Date dateValue = cell.getDateCellValue();
                        admissionDate = dateValue.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                        // 验证格式是否为 YYYY-MM-DD（通过格式化后比较）
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
                            "接诊日期格式不正确: " + (rawValue != null ? rawValue.toString() : "") + "（必须是 YYYY-MM-DD 格式）"
                        ));
                        return null;
                    }
                } else {
                    // 数字类型但不是日期格式，尝试转换为字符串
                    double numericValue = cell.getNumericCellValue();
                    stringValue = String.valueOf((long) numericValue);
                }
            } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.FORMULA) {
                try {
                    if (DateUtil.isCellDateFormatted(cell)) {
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
                    } else {
                        stringValue = cell.getStringCellValue().trim();
                    }
                } catch (Exception e) {
                    stringValue = cell.getStringCellValue().trim();
                }
            } else {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "接诊日期",
                    rawValue,
                    "接诊日期格式不正确: 必须是 YYYY-MM-DD 格式（例如 2024-10-29）"
                ));
                return null;
            }
            
            // 如果是字符串类型，需要严格验证格式
            if (stringValue != null) {
                if (stringValue.isEmpty()) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "接诊日期",
                        rawValue,
                        "接诊日期不能为空"
                    ));
                    return null;
                }
                
                // 严格检查格式：必须是 YYYY-MM-DD
                if (!DATE_PATTERN.matcher(stringValue).matches()) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "接诊日期",
                        rawValue,
                        "接诊日期格式不正确: " + stringValue + "（必须是 YYYY-MM-DD 格式，例如 2024-10-29，不支持 YYYY/MM/DD 或其他格式）"
                    ));
                    return null;
                }
                
                // 尝试解析日期
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
     * 验证接诊时间（必须是四位数字字符串，例如 "1100"）
     * 少于四位的也记录为错误信息
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param errors 错误列表
     * @return 验证后的接诊时间，如果无效返回null
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
            
            // 根据单元格类型处理
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                stringValue = cell.getStringCellValue().trim();
            } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                // 数字类型，转换为字符串
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    stringValue = String.valueOf((long) numericValue);
                } else {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "接诊时间",
                        rawValue,
                        "接诊时间格式不正确: " + numericValue + "（必须是四位整数，例如 1100）"
                    ));
                    return null;
                }
            } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.FORMULA) {
                try {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        stringValue = String.valueOf((long) numericValue);
                    } else {
                        errors.add(createValidationError(
                            excelRowNumber,
                            patientId,
                            "接诊时间",
                            rawValue,
                            "接诊时间格式不正确: " + numericValue + "（必须是四位整数，例如 1100）"
                        ));
                        return null;
                    }
                } catch (Exception e) {
                    stringValue = cell.getStringCellValue().trim();
                }
            } else {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "接诊时间",
                    rawValue,
                    "接诊时间格式不正确: 必须是四位数字字符串（例如 1100）"
                ));
                return null;
            }
            
            // 如果是字符串类型，需要严格验证
            if (stringValue != null) {
                if (stringValue.isEmpty()) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "接诊时间",
                        rawValue,
                        "接诊时间不能为空"
                    ));
                    return null;
                }
                
                // 检查是否包含非数字字符
                if (!stringValue.matches("^\\d+$")) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "接诊时间",
                        rawValue,
                        "接诊时间格式不正确: " + stringValue + "（必须是四位数字，例如 1100）"
                    ));
                    return null;
                }
                
                // 检查长度是否为4位
                if (stringValue.length() != 4) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "接诊时间",
                        rawValue,
                        "接诊时间格式不正确: " + stringValue + "（必须是四位数字，当前为 " + stringValue.length() + " 位）"
                    ));
                    return null;
                }
                
                // 验证小时和分钟是否有效
                int hour = Integer.parseInt(stringValue.substring(0, 2));
                int minute = Integer.parseInt(stringValue.substring(2, 4));
                
                if (hour >= 24 || minute >= 60) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "接诊时间",
                        rawValue,
                        "接诊时间格式不正确: " + stringValue + "（小时必须在0-23之间，分钟必须在0-59之间）"
                    ));
                    return null;
                }
                
                admissionTime = stringValue;
            }
            
        } catch (Exception e) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                "接诊时间",
                rawValue,
                "接诊时间格式不正确: " + (rawValue != null ? rawValue.toString() : "") + "（必须是四位数字，例如 1100）"
            ));
            return null;
        }
        
        return admissionTime;
    }
    
    // ========== 辅助方法 ==========
    
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


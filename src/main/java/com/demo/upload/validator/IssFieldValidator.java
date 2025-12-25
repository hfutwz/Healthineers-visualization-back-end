package com.demo.upload.validator;

import com.demo.upload.dto.ValidationErrorDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ISS字段验证器
 * 负责所有字段的格式验证逻辑
 */
@Component
public class IssFieldValidator {
    
    /**
     * 验证ISS分值格式
     * 支持：单个数字、多个数字（如"1┋3┋4"或"1|3|4"）、"无"、"(空)"、空字符串、0
     * 其他格式都视为错误
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号（从1开始）
     * @param patientId 患者ID
     * @param fieldName 字段名称（用于错误提示）
     * @param errors 错误列表
     * @return 验证后的分值字符串，如果无效返回null
     */
    public String validateIssScore(Row row, Integer columnIndex, int excelRowNumber, int patientId, String fieldName, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            // 允许为空，返回"0"表示无伤情
            return "0";
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            // 允许为空，返回"0"表示无伤情
            return "0";
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        String scoreValue = null;
        
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
                        fieldName,
                        rawValue,
                        fieldName + "格式不正确: " + numericValue + "（必须是整数）"
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
                            fieldName,
                            rawValue,
                            fieldName + "格式不正确: " + numericValue + "（必须是整数）"
                        ));
                        return null;
                    }
                } catch (Exception e) {
                    stringValue = cell.getStringCellValue().trim();
                }
            } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.BLANK) {
                // 空白单元格，返回"0"
                return "0";
            } else {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    fieldName,
                    rawValue,
                    fieldName + "格式不正确: 必须是单个数字、多个数字（如\"1┋3┋4\"或\"1|3|4\"）、\"无\"、\"(空)\"或空字符串"
                ));
                return null;
            }
            
            // 如果是字符串类型，需要验证格式
            if (stringValue != null) {
                if (stringValue.isEmpty()) {
                    return "0";
                }
                
                // 检查是否为"无"、"(空)"、"0"
                if (stringValue.equals("无") || stringValue.equals("(空)") || stringValue.equals("0")) {
                    return "0";
                }
                
                // 检查是否包含"┋"分隔符，转换为"|"
                if (stringValue.contains("┋")) {
                    stringValue = stringValue.replace("┋", "|");
                }
                
                // 验证格式：单个数字或"数字|数字|..."格式
                if (stringValue.matches("^\\d+$")) {
                    // 单个数字
                    scoreValue = stringValue;
                } else if (stringValue.contains("|")) {
                    // 多个数字，用"|"分隔
                    String[] parts = stringValue.split("\\|");
                    boolean isValid = true;
                    for (String part : parts) {
                        part = part.trim();
                        if (!part.matches("^\\d+$")) {
                            isValid = false;
                            break;
                        }
                    }
                    if (isValid) {
                        // 重新组合，确保格式正确
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < parts.length; i++) {
                            if (i > 0) {
                                sb.append("|");
                            }
                            sb.append(parts[i].trim());
                        }
                        scoreValue = sb.toString();
                    } else {
                        errors.add(createValidationError(
                            excelRowNumber,
                            patientId,
                            fieldName,
                            rawValue,
                            fieldName + "格式不正确: " + stringValue + "（多个数字必须用\"|\"分隔，且每个部分必须是数字）"
                        ));
                        return null;
                    }
                } else {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        fieldName,
                        rawValue,
                        fieldName + "格式不正确: " + stringValue + "（必须是单个数字、多个数字（如\"1┋3┋4\"或\"1|3|4\"）、\"无\"、\"(空)\"或空字符串）"
                    ));
                    return null;
                }
            }
            
        } catch (Exception e) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                fieldName,
                rawValue,
                fieldName + "格式不正确: " + (rawValue != null ? rawValue.toString() : "") + "（必须是单个数字、多个数字（如\"1┋3┋4\"或\"1|3|4\"）、\"无\"、\"(空)\"或空字符串）"
            ));
            return null;
        }
        
        return scoreValue;
    }
    
    /**
     * 验证详细伤情信息
     * 如果有分值（非"0"），则必须有对应的详细伤情信息
     * 
     * @param scoreValue 分值字符串（如"2|3"或"0"）
     * @param detailsValue 详细伤情信息
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param fieldName 字段名称（用于错误提示）
     * @param errors 错误列表
     * @return 验证后的详细伤情信息，如果无效返回null
     */
    public String validateDetails(String scoreValue, String detailsValue, int excelRowNumber, int patientId, String fieldName, List<ValidationErrorDTO> errors) {
        // 如果分值为"0"或null，详细伤情可以为空
        if (scoreValue == null || scoreValue.equals("0")) {
            return detailsValue != null && !detailsValue.trim().isEmpty() ? detailsValue.trim() : null;
        }
        
        // 如果有分值，必须有详细伤情信息
        if (detailsValue == null || detailsValue.trim().isEmpty()) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                fieldName + "详细伤情",
                detailsValue,
                fieldName + "有分值（" + scoreValue + "），但缺少详细伤情信息"
            ));
            return null;
        }
        
        return detailsValue.trim();
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
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return (long) numericValue;
                    } else {
                        return numericValue;
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


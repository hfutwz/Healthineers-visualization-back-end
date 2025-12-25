package com.demo.upload.validator;

import com.demo.upload.dto.ValidationErrorDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 患者字段验证器
 * 负责所有字段的格式验证逻辑
 */
@Component
public class PatientFieldValidator {
    
    /**
     * 验证患者ID
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号（从1开始）
     * @param errors 错误列表
     * @return 验证后的患者ID，如果无效返回null
     */
    public Integer validatePatientId(Row row, Integer columnIndex, int excelRowNumber, List<ValidationErrorDTO> errors) {
        Integer patientId = getCellValueAsInt(row, columnIndex);
        if (patientId == null || patientId <= 0) {
            Object rawValue = getCellRawValue(row, columnIndex);
            errors.add(createValidationError(
                excelRowNumber,
                0,
                "序号",
                rawValue,
                "患者ID无效或为空"
            ));
            return null;
        }
        return patientId;
    }
    
    /**
     * 验证性别
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param errors 错误列表
     * @return 验证后的性别，如果无效返回null
     */
    public String validateGender(Row row, Integer columnIndex, int excelRowNumber, int patientId, List<ValidationErrorDTO> errors) {
        String gender = getCellValueAsString(row, columnIndex);
        gender = cleanText(gender);
        if (gender != null && !gender.isEmpty()) {
            if (!"男".equals(gender) && !"女".equals(gender)) {
                Object rawValue = getCellRawValue(row, columnIndex);
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "患者性别",
                    rawValue,
                    "性别格式不正确: " + gender + "（只能为'男'或'女'）"
                ));
                return null;
            }
        }
        return gender;
    }
    
    /**
     * 验证年龄（必须是纯整数，0-120之间，不能包含中文）
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param errors 错误列表
     * @return 验证后的年龄，如果无效返回null
     */
    public Integer validateAge(Row row, Integer columnIndex, int excelRowNumber, int patientId, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                "年龄",
                "",
                "年龄不能为空"
            ));
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                "年龄",
                "",
                "年龄不能为空"
            ));
            return null;
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        Integer age = null;
        
        try {
            // 先获取原始字符串值，用于检查是否包含非数字字符
            String stringValue = null;
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                stringValue = cell.getStringCellValue().trim();
                
                // 检查是否包含中文字符
                if (containsChinese(stringValue)) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "年龄",
                        rawValue,
                        "年龄格式不正确: " + stringValue + "（不能包含中文）"
                    ));
                    return null;
                }
            } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                // 对于数字类型，检查是否为整数
                double numericValue = cell.getNumericCellValue();
                if (numericValue != (long) numericValue) {
                    // 是小数，不符合要求
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "年龄",
                        rawValue,
                        "年龄必须是整数: " + numericValue + "（不能是小数）"
                    ));
                    return null;
                }
                age = (int) (long) numericValue;
            } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.FORMULA) {
                try {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue != (long) numericValue) {
                        errors.add(createValidationError(
                            excelRowNumber,
                            patientId,
                            "年龄",
                            rawValue,
                            "年龄必须是整数: " + numericValue + "（不能是小数）"
                        ));
                        return null;
                    }
                    age = (int) (long) numericValue;
                } catch (Exception e) {
                    stringValue = cell.getStringCellValue().trim();
                    // 检查是否包含中文字符
                    if (containsChinese(stringValue)) {
                        errors.add(createValidationError(
                            excelRowNumber,
                            patientId,
                            "年龄",
                            rawValue,
                            "年龄格式不正确: " + stringValue + "（不能包含中文）"
                        ));
                        return null;
                    }
                }
            } else {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "年龄",
                    rawValue,
                    "年龄格式不正确: 必须是整数"
                ));
                return null;
            }
            
            // 如果是字符串类型，需要严格验证
            if (stringValue != null) {
                if (stringValue.isEmpty()) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "年龄",
                        rawValue,
                        "年龄不能为空"
                    ));
                    return null;
                }
                
                // 检查是否包含非数字字符（如"月"、"岁"等）
                if (!stringValue.matches("^\\d+$")) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "年龄",
                        rawValue,
                        "年龄格式不正确: " + stringValue + "（必须是纯整数，不能包含文字或小数）"
                    ));
                    return null;
                }
                
                // 解析为整数
                age = Integer.parseInt(stringValue);
            }
            
            // 验证范围
            if (age != null) {
                if (age < 0 || age > 120) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "年龄",
                        rawValue,
                        "年龄超出合理范围: " + age + "（应在0-120之间）"
                    ));
                    return null;
                }
            }
            
        } catch (Exception e) {
            errors.add(createValidationError(
                excelRowNumber,
                patientId,
                "年龄",
                rawValue,
                "年龄格式不正确: " + (rawValue != null ? rawValue.toString() : "") + "（必须是纯整数）"
            ));
            return null;
        }
        
        return age;
    }
    
    /**
     * 验证是否绿色通道
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param errors 错误列表
     * @return 验证后的值，如果无效返回null
     */
    public String validateIsGreenChannel(Row row, Integer columnIndex, int excelRowNumber, int patientId, List<ValidationErrorDTO> errors) {
        String isGreenChannel = getCellValueAsString(row, columnIndex);
        isGreenChannel = cleanText(isGreenChannel);
        if (isGreenChannel != null && !isGreenChannel.isEmpty()) {
            if (!"是".equals(isGreenChannel) && !"否".equals(isGreenChannel)) {
                Object rawValue = getCellRawValue(row, columnIndex);
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "是否绿色通道",
                    rawValue,
                    "是否绿色通道格式不正确: " + isGreenChannel + "（只能为'是'或'否'）"
                ));
                return null;
            }
        }
        return isGreenChannel;
    }
    
    /**
     * 验证身高（DECIMAL(5,2)，可带小数，不能包含中文）
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param errors 错误列表
     * @return 验证后的身高，如果无效返回null
     */
    public Double validateHeight(Row row, Integer columnIndex, int excelRowNumber, int patientId, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        
        // 先检查是否包含中文字符
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
            String stringValue = cell.getStringCellValue().trim();
            if (containsChinese(stringValue)) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "身高",
                    rawValue,
                    "身高格式不正确: " + stringValue + "（不能包含中文）"
                ));
                return null;
            }
        } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            try {
                String stringValue = cell.getStringCellValue().trim();
                if (containsChinese(stringValue)) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "身高",
                        rawValue,
                        "身高格式不正确: " + stringValue + "（不能包含中文）"
                    ));
                    return null;
                }
            } catch (Exception e) {
                // 如果无法获取字符串值，继续数值验证
            }
        }
        
        Double height = getCellValueAsDouble(row, columnIndex);
        if (height != null && height > 0) {
            // 验证DECIMAL(5,2)格式：最多5位数字，小数点后最多2位
            BigDecimal heightDecimal = BigDecimal.valueOf(height).setScale(2, RoundingMode.HALF_UP);
            if (heightDecimal.precision() > 5) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "身高",
                    rawValue,
                    "身高格式不正确: " + height + "（最多5位数字，小数点后最多2位）"
                ));
                return null;
            } else if (height < 30 || height > 250) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "身高",
                    rawValue,
                    "身高超出合理范围: " + height + "cm（应在30-250cm之间）"
                ));
                return null;
            }
        }
        return height;
    }
    
    /**
     * 验证体重（DECIMAL(5,2)，可带小数，不能包含中文）
     * 
     * @param row Excel行
     * @param columnIndex 列索引
     * @param excelRowNumber Excel行号
     * @param patientId 患者ID
     * @param errors 错误列表
     * @return 验证后的体重，如果无效返回null
     */
    public Double validateWeight(Row row, Integer columnIndex, int excelRowNumber, int patientId, List<ValidationErrorDTO> errors) {
        if (row == null || columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        Object rawValue = getCellRawValue(row, columnIndex);
        
        // 先检查是否包含中文字符
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
            String stringValue = cell.getStringCellValue().trim();
            if (containsChinese(stringValue)) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "体重",
                    rawValue,
                    "体重格式不正确: " + stringValue + "（不能包含中文）"
                ));
                return null;
            }
        } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            try {
                String stringValue = cell.getStringCellValue().trim();
                if (containsChinese(stringValue)) {
                    errors.add(createValidationError(
                        excelRowNumber,
                        patientId,
                        "体重",
                        rawValue,
                        "体重格式不正确: " + stringValue + "（不能包含中文）"
                    ));
                    return null;
                }
            } catch (Exception e) {
                // 如果无法获取字符串值，继续数值验证
            }
        }
        
        Double weight = getCellValueAsDouble(row, columnIndex);
        if (weight != null && weight > 0) {
            // 验证DECIMAL(5,2)格式：最多5位数字，小数点后最多2位
            BigDecimal weightDecimal = BigDecimal.valueOf(weight).setScale(2, RoundingMode.HALF_UP);
            if (weightDecimal.precision() > 5) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "体重",
                    rawValue,
                    "体重格式不正确: " + weight + "（最多5位数字，小数点后最多2位）"
                ));
                return null;
            } else if (weight < 1 || weight > 500) {
                errors.add(createValidationError(
                    excelRowNumber,
                    patientId,
                    "体重",
                    rawValue,
                    "体重超出合理范围: " + weight + "kg（应在1-500kg之间）"
                ));
                return null;
            }
        }
        return weight;
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 获取单元格的字符串值
     */
    private String getCellValueAsString(Row row, Integer columnIndex) {
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
        } catch (Exception e) {
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
        return cleanInt(cell);
    }
    
    /**
     * 清理并转换为整数
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
     * 获取单元格的浮点数值
     */
    private Double getCellValueAsDouble(Row row, Integer columnIndex) {
        if (row == null || columnIndex == null) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        return cleanFloat(cell);
    }
    
    /**
     * 清理并转换为浮点数
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
     * 检查字符串是否包含中文字符
     * 
     * @param text 待检查的字符串
     * @return 如果包含中文字符返回true，否则返回false
     */
    private boolean containsChinese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        // 检查是否包含中文字符（Unicode范围：\u4e00-\u9fa5）
        return text.matches(".*[\\u4e00-\\u9fa5].*");
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


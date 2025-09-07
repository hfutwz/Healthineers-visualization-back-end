package com.demo.utils;

import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel导入工具类，用于读取Excel内容并批量保存实体对象
 */
public class ExcelImportUtil {

    /**
     * 读取Excel，将内容转成实体对象集合并批量保存
     *
     * @param file    上传的Excel文件
     * @param service 业务层Service，实现了saveBatch方法
     * @param clazz   实体类类型
     * @param <T>     实体类型
     * @throws Exception
     */
    public static <T> void importExcelToDb(MultipartFile file, IService<T> service, Class<T> clazz) throws Exception {
        List<T> entityList = new ArrayList<>();
        InputStream in = file.getInputStream();
        Workbook workbook = WorkbookFactory.create(in);
        Sheet sheet = workbook.getSheetAt(0); //读取第一个sheet
        if (sheet == null) {
            workbook.close();
            throw new RuntimeException("Excel中没有Sheet");
        }

        // 读取标题行
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            workbook.close();
            throw new RuntimeException("Excel中没有标题行");
        }
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(cell.getStringCellValue().trim());
        }

        // 从第2行开始读取数据
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;

            try {
                T entity = clazz.getDeclaredConstructor().newInstance();
                for (int col = 0; col < headers.size(); col++) {
                    Cell cell = row.getCell(col);
                    String header = headers.get(col);
                    setFieldValue(entity, header, cell);
                }
                entityList.add(entity);
            } catch (Exception e) {
                // 出现读取或设置异常，跳过本行
                // 可以考虑打印日志，方便调试
                System.out.println("跳过第" + (rowNum + 1) + "行，原因：" + e.getMessage());
                continue; // 跳过这行，继续处理下一行
            }
        }

        workbook.close();

        // 批量保存到数据库
        service.saveBatch(entityList);
    }

    private static <T> void setFieldValue(T entity, String fieldName, Cell cell) {
        try {
            java.lang.reflect.Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Class<?> type = field.getType();

            if (cell == null) {
                field.set(entity, null);
                return;
            }

            switch (type.getSimpleName()) {
                case "String":
                    if (cell.getCellType() == CellType.STRING) {
                        field.set(entity, cell.getStringCellValue());
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        field.set(entity, String.valueOf(cell.getNumericCellValue()));
                    }
                    break;

                case "Integer":
                case "int":
                    if (cell.getCellType() == CellType.NUMERIC) {
                        field.set(entity, (int) cell.getNumericCellValue());
                    } else if (cell.getCellType() == CellType.STRING) {
                        String strVal = cell.getStringCellValue();
                        if (strVal != null && !strVal.isEmpty()) {
                            field.set(entity, Integer.parseInt(strVal));
                        }
                    }
                    break;

                case "Double":
                case "double":
                    if (cell.getCellType() == CellType.NUMERIC) {
                        field.set(entity, cell.getNumericCellValue());
                    } else if (cell.getCellType() == CellType.STRING) {
                        String strVal = cell.getStringCellValue();
                        if (strVal != null && !strVal.isEmpty()) {
                            field.set(entity, Double.parseDouble(strVal));
                        }
                    }
                    break;

                case "Boolean":
                case "boolean":
                    if (cell.getCellType() == CellType.BOOLEAN) {
                        field.set(entity, cell.getBooleanCellValue());
                    } else if (cell.getCellType() == CellType.STRING) {
                        String strVal = cell.getStringCellValue();
                        if (strVal != null) {
                            if (strVal.equalsIgnoreCase("true") || strVal.equalsIgnoreCase("是")) {
                                field.set(entity, true);
                            } else {
                                field.set(entity, false);
                            }
                        }
                    }
                    break;

                case "Long":
                case "long":
                    if (cell.getCellType() == CellType.NUMERIC) {
                        field.set(entity, (long) cell.getNumericCellValue());
                    } else if (cell.getCellType() == CellType.STRING) {
                        String strVal = cell.getStringCellValue();
                        if (strVal != null && !strVal.isEmpty()) {
                            field.set(entity, Long.parseLong(strVal));
                        }
                    }
                    break;
                case "LocalDate":
                    if (cell.getCellType() == CellType.NUMERIC) {
                        if (DateUtil.isCellDateFormatted(cell)) {
                            // 转换为LocalDate
                            java.util.Date date = cell.getDateCellValue();
                            java.time.LocalDate localDate = date.toInstant()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate();
                            field.set(entity, localDate);
                        }
                    } else if (cell.getCellType() == CellType.STRING) {
                        String dateStr = cell.getStringCellValue();
                        // 根据你的日期格式，自己定义解析逻辑
                        // 比如：yyyy-MM-dd
                        java.time.LocalDate localDate = LocalDate.parse(dateStr);
                        field.set(entity, localDate);
                    }
                    break;

                case "Date": // 如果是java.util.Date
                    if (cell.getCellType() == CellType.NUMERIC) {
                        if (DateUtil.isCellDateFormatted(cell)) {
                            java.util.Date date = cell.getDateCellValue();
                            field.set(entity, date);
                        }
                    } else if (cell.getCellType() == CellType.STRING) {
                        String dateStr = cell.getStringCellValue();
                        // 自定义日期格式解析
                        java.util.Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                        field.set(entity, date);
                    }
                    break;

                default:
                    // 其他类型可以自行扩展
                    break;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            // 这里可以选择抛出异常或忽略
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}


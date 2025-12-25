package com.demo.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据导入结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {
    /**
     * 导入是否成功
     */
    private Boolean success;
    
    /**
     * 成功导入的记录数（包括插入和更新）
     */
    private Integer successCount;
    
    /**
     * 新插入的记录数
     */
    private Integer insertCount;
    
    /**
     * 更新的记录数（已存在的记录）
     */
    private Integer updateCount;
    
    /**
     * 失败的记录数
     */
    private Integer failedCount;
    
    /**
     * 状态：success, partial, failed
     */
    private String status;
    
    /**
     * 消息
     */
    private String message;
}


package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * RTS评分表实体类
 * 对应数据库表：rts_score
 */
@TableName("rts_score")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RtsScore implements Serializable {
    /**
     * RTS评分ID，自增主键
     */
    @TableId(value = "rts_id", type = IdType.AUTO)
    private Integer rtsId;
    
    /**
     * 患者ID
     */
    private Integer patientId;
    
    /**
     * GCS评分 (0-4分)
     */
    private Integer gcsScore;
    
    /**
     * 收缩压评分 (0-4分)
     */
    private Integer sbpScore;
    
    /**
     * 呼吸频率评分 (0-4分)
     */
    private Integer rrScore;
    
    /**
     * RTS总分 (0-12分)
     */
    private Integer totalScore;
}
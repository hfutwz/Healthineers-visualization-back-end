package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 伤情类型表
 * 存储所有可能的伤情类型和对应的分值
 */
@TableName("injury_types")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class InjuryType implements Serializable {
    /**
     * 伤情类型ID，自增主键
     */
    @TableId(value = "injury_type_id", type = IdType.AUTO)
    private Integer injuryTypeId;
    
    /**
     * 身体部位：headNeck, face, chest, abdomen, limbs, body
     */
    private String bodyPart;
    
    /**
     * 伤情名称
     */
    private String injuryName;
    
    /**
     * 详细描述
     */
    private String injuryDescription;
    
    /**
     * 分值
     */
    private Integer scoreValue;
    
    /**
     * 是否启用
     */
    private Boolean isActive;
}

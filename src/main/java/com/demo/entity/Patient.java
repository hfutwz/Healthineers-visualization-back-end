package com.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 建表语句：
 * CREATE TABLE Patient (
 *     patient_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '患者ID',
 *     gender VARCHAR(10) NOT NULL COMMENT '性别',
 *     age INT NOT NULL COMMENT '年龄',
 *     is_green_channel BOOLEAN NOT NULL COMMENT '是否绿色通道',
 *     height DECIMAL(5,2) DEFAULT NULL COMMENT '身高',
 *     weight DECIMAL(5,2) DEFAULT NULL COMMENT '体重',
 *     name VARCHAR(50) DEFAULT NULL COMMENT '姓名'
 * ) COMMENT='患者基础信息表';
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("patient")
@AllArgsConstructor
@NoArgsConstructor
public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 患者ID，唯一
     */
    @TableField(value = "patient_id")
    private Integer patientId;
    /**
     *  性别
     */
    private String gender;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 是否绿色通道
     */
    @TableField("is_green_channel")
    private String isGreenChannel;
    /**
     * 身高 cm
     */
    private Double height;
    /**
     * 体重 kg
     */
    private Double weight;
    /**
     * 姓名
     */
    private String name;
}

package com.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
    
    /**
     * 批量插入患者数据（不允许主键重复）
     * SQL 定义在 PatientMapper.xml 中
     * 
     * @param patients 患者列表
     * @return 插入的记录数
     */
    int insertBatch(@Param("patients") List<Patient> patients);
}

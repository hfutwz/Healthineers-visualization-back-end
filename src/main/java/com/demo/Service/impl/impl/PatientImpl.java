package com.demo.Service.impl.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.Service.impl.IPatientService;
import com.demo.Service.impl.IInjuryRecordService;
import com.demo.Service.impl.IIssInjuryService;
import com.demo.Service.impl.IInterventionTimeService;
import com.demo.Service.IGcsScoreService;
import com.demo.Service.IRtsScoreService;
import com.demo.Service.IPatientInfoOnAdmissionService;
import com.demo.Service.IPatientInfoOffAdmissionService;
import com.demo.dto.PatientPageDTO;
import com.demo.dto.PatientQueryDTO;
import com.demo.dto.PatientUpdateResultDTO;
import com.demo.entity.Patient;
import com.demo.entity.InterventionTime;
import com.demo.entity.InterventionExtra;
import com.demo.mapper.PatientMapper;
import com.demo.mapper.InterventionExtraMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PatientImpl extends ServiceImpl<PatientMapper, Patient> implements IPatientService {

    @Autowired
    private IInjuryRecordService injuryRecordService;

    @Autowired
    private IIssInjuryService issInjuryService;

    @Autowired
    private IInterventionTimeService interventionTimeService;

    @Autowired
    private IGcsScoreService gcsScoreService;

    @Autowired
    private IRtsScoreService rtsScoreService;

    @Autowired
    private IPatientInfoOnAdmissionService patientInfoOnAdmissionService;

    @Autowired
    private IPatientInfoOffAdmissionService patientInfoOffAdmissionService;

    @Autowired
    private InterventionExtraMapper interventionExtraMapper;

    @Override
    public PatientPageDTO getPatientPage(PatientQueryDTO queryDTO) {
        // 创建分页对象
        Page<Patient> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        // 构建查询条件
        LambdaQueryWrapper<Patient> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按患者ID查询
        if (queryDTO.getPatientId() != null) {
            queryWrapper.eq(Patient::getPatientId, queryDTO.getPatientId());
        }
        
        // 按性别查询
        if (StringUtils.hasText(queryDTO.getGender())) {
            queryWrapper.eq(Patient::getGender, queryDTO.getGender());
        }
        
        // 按年龄段查询
        if (queryDTO.getMinAge() != null) {
            queryWrapper.ge(Patient::getAge, queryDTO.getMinAge());
        }
        if (queryDTO.getMaxAge() != null) {
            queryWrapper.le(Patient::getAge, queryDTO.getMaxAge());
        }
        
        // 按患者ID排序
        queryWrapper.orderByAsc(Patient::getPatientId);
        
        // 执行分页查询
        Page<Patient> result = this.page(page, queryWrapper);
        
        // 构建返回结果
        PatientPageDTO pageDTO = new PatientPageDTO();
        pageDTO.setRecords(result.getRecords());
        pageDTO.setTotal(result.getTotal());
        pageDTO.setCurrent(result.getCurrent());
        pageDTO.setSize(result.getSize());
        pageDTO.setPages(result.getPages());
        
        return pageDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePatientById(Integer patientId) {
        if (patientId == null) {
            return false;
        }

        try {
            // 1. 删除干预补充信息（intervention_extra，通过patient_id关联）
            interventionExtraMapper.delete(
                new LambdaQueryWrapper<InterventionExtra>()
                    .eq(InterventionExtra::getPatientId, patientId)
            );
            
            // 2. 删除干预时间信息（intervention_time）
            interventionTimeService.remove(
                new LambdaQueryWrapper<InterventionTime>()
                    .eq(InterventionTime::getPatientId, patientId)
            );

            // 3. 删除病例记录
            injuryRecordService.remove(
                new LambdaQueryWrapper<com.demo.entity.InjuryRecord>()
                    .eq(com.demo.entity.InjuryRecord::getPatientId, patientId)
            );

            // 4. 删除ISS创伤严重度信息
            issInjuryService.remove(
                new LambdaQueryWrapper<com.demo.entity.IssInjury>()
                    .eq(com.demo.entity.IssInjury::getPatientId, patientId)
            );

            // 5. 删除GCS评分
            gcsScoreService.remove(
                new LambdaQueryWrapper<com.demo.entity.GcsScore>()
                    .eq(com.demo.entity.GcsScore::getPatientId, patientId)
            );

            // 6. 删除RTS评分
            rtsScoreService.remove(
                new LambdaQueryWrapper<com.demo.entity.RtsScore>()
                    .eq(com.demo.entity.RtsScore::getPatientId, patientId)
            );

            // 7. 删除入室前信息
            patientInfoOnAdmissionService.remove(
                new LambdaQueryWrapper<com.demo.entity.PatientInfoOnAdmission>()
                    .eq(com.demo.entity.PatientInfoOnAdmission::getPatientId, patientId)
            );

            // 8. 删除离室后信息
            patientInfoOffAdmissionService.remove(
                new LambdaQueryWrapper<com.demo.entity.PatientInfoOffAdmission>()
                    .eq(com.demo.entity.PatientInfoOffAdmission::getPatientId, patientId)
            );

            // 9. 最后删除患者基本信息
            boolean result = this.removeById(patientId);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("删除患者数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientUpdateResultDTO updatePatient(Patient patient) {
        // 1. 验证患者对象和ID
        if (patient == null || patient.getPatientId() == null) {
            return PatientUpdateResultDTO.fail("患者ID不能为空");
        }
        
        // 2. 验证性别格式（选填，但如果填写了必须符合格式）
        if (patient.getGender() != null && !patient.getGender().trim().isEmpty()) {
            if (!patient.getGender().equals("男") && !patient.getGender().equals("女")) {
                return PatientUpdateResultDTO.fail("性别只能是'男'或'女'");
            }
        }
        
        // 3. 验证年龄范围（选填，但如果填写了必须符合范围）
        if (patient.getAge() != null) {
            if (patient.getAge() < 0 || patient.getAge() > 120) {
                return PatientUpdateResultDTO.fail("年龄必须在0-120之间");
            }
        }
        
        // 4. 验证是否绿色通道格式（选填，但如果填写了必须符合格式）
        if (patient.getIsGreenChannel() != null && !patient.getIsGreenChannel().trim().isEmpty()) {
            if (!patient.getIsGreenChannel().equals("是") && !patient.getIsGreenChannel().equals("否")) {
                return PatientUpdateResultDTO.fail("是否绿色通道只能是'是'或'否'");
            }
        }
        
        // 5. 验证身高范围（选填）
        if (patient.getHeight() != null) {
            double height = patient.getHeight();
            if (height < 0 || height > 300) {
                return PatientUpdateResultDTO.fail("身高必须在0-300之间");
            }
        }
        
        // 6. 验证体重范围（选填）
        if (patient.getWeight() != null) {
            double weight = patient.getWeight();
            if (weight < 0 || weight > 500) {
                return PatientUpdateResultDTO.fail("体重必须在0-500之间");
            }
        }
        
        // 7. 验证患者是否存在
        Patient existingPatient = this.getById(patient.getPatientId());
        if (existingPatient == null) {
            return PatientUpdateResultDTO.fail("患者不存在，无法更新");
        }
        
        // 8. 执行更新（只更新非空字段）
        boolean result = this.updateById(patient);
        
        if (!result) {
            return PatientUpdateResultDTO.fail("更新患者信息失败");
        }
        
        // 9. 获取更新后的患者信息
        Patient updatedPatient = this.getById(patient.getPatientId());
        
        // 10. 返回成功结果
        return PatientUpdateResultDTO.success("更新成功", updatedPatient);
    }
}

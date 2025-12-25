package com.demo.Service.impl.impl;

import com.demo.Service.impl.IIssInjuryService;
import com.demo.Service.impl.IPatientInjuryDetailService;
import com.demo.dto.IssInjuryDTO;
import com.demo.dto.PatientInjuryDetailDTO;
import com.demo.entity.IssInjury;
import com.demo.mapper.PatientInjuryDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 患者伤情详情服务实现类
 */
@Service
public class PatientInjuryDetailServiceImpl implements IPatientInjuryDetailService {
    
    @Autowired
    private IIssInjuryService issInjuryService;
    
    @Autowired
    private PatientInjuryDetailMapper patientInjuryDetailMapper;
    
    @Override
    public PatientInjuryDetailDTO getInjuryDetailsByPatientId(Integer patientId) {
        // 获取基础ISS信息（DTO用于获取计算后的伤情等级）
        IssInjuryDTO baseInjuryDTO = issInjuryService.getInjuryDTOByPatientId(patientId);
        if (baseInjuryDTO == null) {
            return null;
        }
        
        // 直接从实体获取String类型的得分（支持"1|2"格式）
        IssInjury baseInjury = issInjuryService.getByPatientId(patientId);
        if (baseInjury == null) {
            return null;
        }
        
        // 创建详细伤情DTO
        PatientInjuryDetailDTO detailDTO = new PatientInjuryDetailDTO();
        detailDTO.setPatientId(patientId);
        // 从实体直接获取String类型的得分，如果为null则设置为"0"
        detailDTO.setHeadNeck(baseInjury.getHeadNeck() != null ? baseInjury.getHeadNeck() : "0");
        detailDTO.setFace(baseInjury.getFace() != null ? baseInjury.getFace() : "0");
        detailDTO.setChest(baseInjury.getChest() != null ? baseInjury.getChest() : "0");
        detailDTO.setAbdomen(baseInjury.getAbdomen() != null ? baseInjury.getAbdomen() : "0");
        detailDTO.setLimbs(baseInjury.getLimbs() != null ? baseInjury.getLimbs() : "0");
        detailDTO.setBody(baseInjury.getBody() != null ? baseInjury.getBody() : "0");
        detailDTO.setIssScore(baseInjuryDTO.getIssScore());
        detailDTO.setInjurySeverity(baseInjuryDTO.getInjurySeverity());
        
        // 获取各部位详细伤情信息
        detailDTO.setHeadNeckDetails(getInjuryDetailsByBodyPart(patientId, "headNeck"));
        detailDTO.setFaceDetails(getInjuryDetailsByBodyPart(patientId, "face"));
        detailDTO.setChestDetails(getInjuryDetailsByBodyPart(patientId, "chest"));
        detailDTO.setAbdomenDetails(getInjuryDetailsByBodyPart(patientId, "abdomen"));
        detailDTO.setLimbsDetails(getInjuryDetailsByBodyPart(patientId, "limbs"));
        detailDTO.setBodyDetails(getInjuryDetailsByBodyPart(patientId, "body"));
        
        return detailDTO;
    }
    
    /**
     * 根据身体部位获取伤情详情
     */
    private List<com.demo.dto.InjuryDetailDTO> getInjuryDetailsByBodyPart(Integer patientId, String bodyPart) {
        return patientInjuryDetailMapper.selectInjuryDetailsByPatientIdAndBodyPart(patientId, bodyPart);
    }
}

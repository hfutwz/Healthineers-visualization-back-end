package com.demo.Service.impl.impl;

import com.demo.Service.impl.IIssInjuryService;
import com.demo.Service.impl.IPatientInjuryDetailService;
import com.demo.dto.IssInjuryDTO;
import com.demo.dto.PatientInjuryDetailDTO;
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
        // 获取基础ISS信息
        IssInjuryDTO baseInjury = issInjuryService.getInjuryDTOByPatientId(patientId);
        if (baseInjury == null) {
            return null;
        }
        
        // 创建详细伤情DTO
        PatientInjuryDetailDTO detailDTO = new PatientInjuryDetailDTO();
        detailDTO.setPatientId(baseInjury.getPatientId());
        detailDTO.setHeadNeck(baseInjury.getHeadNeck());
        detailDTO.setFace(baseInjury.getFace());
        detailDTO.setChest(baseInjury.getChest());
        detailDTO.setAbdomen(baseInjury.getAbdomen());
        detailDTO.setLimbs(baseInjury.getLimbs());
        detailDTO.setBody(baseInjury.getBody());
        detailDTO.setIssScore(baseInjury.getIssScore());
        detailDTO.setInjurySeverity(baseInjury.getInjurySeverity());
        
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

package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 患者详细伤情信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientInjuryDetailDTO {
    private Integer patientId;
    private Integer headNeck;
    private Integer face;
    private Integer chest;
    private Integer abdomen;
    private Integer limbs;
    private Integer body;
    private Integer issScore;
    private Integer injurySeverity;
    
    // 详细伤情列表
    private List<InjuryDetailDTO> headNeckDetails;
    private List<InjuryDetailDTO> faceDetails;
    private List<InjuryDetailDTO> chestDetails;
    private List<InjuryDetailDTO> abdomenDetails;
    private List<InjuryDetailDTO> limbsDetails;
    private List<InjuryDetailDTO> bodyDetails;
}

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
    /**
     * 头颈部伤情等级，支持"1|2"格式
     */
    private String headNeck;
    /**
     * 面部伤情等级，支持"1|2"格式
     */
    private String face;
    /**
     * 胸部伤情等级，支持"1|2"格式
     */
    private String chest;
    /**
     * 腹部伤情等级，支持"1|2"格式
     */
    private String abdomen;
    /**
     * 四肢伤情等级，支持"1|2"格式
     */
    private String limbs;
    /**
     * 体表伤情等级，支持"1|2"格式
     */
    private String body;
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

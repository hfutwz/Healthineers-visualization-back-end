package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人体图，标记创伤部位
 * 返回数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssInjuryDTO {
    private Integer patientId;
    private Integer headNeck;
    private Integer face;
    private Integer chest;
    private Integer abdomen;
    private Integer limbs;
    private Integer body;
    private Integer issScore;
    /**
     * 受伤等级，0-轻伤、1-重伤、2-严重
     * service层计算，并填充0、1、2
     */
    private Integer injurySeverity;
    
    /**
     * 头颈部详细伤情
     */
    private String headNeckDetails;
    
    /**
     * 面部详细伤情
     */
    private String faceDetails;
    
    /**
     * 胸部详细伤情
     */
    private String chestDetails;
    
    /**
     * 腹部详细伤情
     */
    private String abdomenDetails;
    
    /**
     * 四肢详细伤情
     */
    private String limbsDetails;
    
    /**
     * 体表详细伤情
     */
    private String bodyDetails;
    
    /**
     * 是否有详细伤情信息
     */
    private Boolean hasDetails;
}

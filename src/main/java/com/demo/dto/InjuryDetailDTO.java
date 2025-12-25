package com.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 伤情详情DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InjuryDetailDTO {
    private Integer injuryTypeId;
    private String injuryName;
    private String injuryDescription;
    private Integer scoreValue;
    private Integer injuryCount;
    private String notes;
}

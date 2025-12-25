package com.demo.dto;

import java.io.Serializable;

/**
 * 干预时间DTO - 用于存储原始时间数据
 */
public class InterventionTimeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer patientId;
    private String admissionDate;      // 入室日期 (YYYY-MM-DD)
    private String admissionTime;       // 入室时间 (4位字符串，如"2326"表示23:26)
    private String leaveSurgeryDate;   // 离室日期 (YYYY-MM-DD)
    private String leaveSurgeryTime;   // 离室时间 (4位字符串，如"2326"表示23:26)
    
    public InterventionTimeDTO() {
    }
    
    public InterventionTimeDTO(Integer patientId, String admissionDate, String admissionTime, 
                               String leaveSurgeryDate, String leaveSurgeryTime) {
        this.patientId = patientId;
        this.admissionDate = admissionDate;
        this.admissionTime = admissionTime;
        this.leaveSurgeryDate = leaveSurgeryDate;
        this.leaveSurgeryTime = leaveSurgeryTime;
    }
    
    public Integer getPatientId() {
        return patientId;
    }
    
    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }
    
    public String getAdmissionDate() {
        return admissionDate;
    }
    
    public void setAdmissionDate(String admissionDate) {
        this.admissionDate = admissionDate;
    }
    
    public String getAdmissionTime() {
        return admissionTime;
    }
    
    public void setAdmissionTime(String admissionTime) {
        this.admissionTime = admissionTime;
    }
    
    public String getLeaveSurgeryDate() {
        return leaveSurgeryDate;
    }
    
    public void setLeaveSurgeryDate(String leaveSurgeryDate) {
        this.leaveSurgeryDate = leaveSurgeryDate;
    }
    
    public String getLeaveSurgeryTime() {
        return leaveSurgeryTime;
    }
    
    public void setLeaveSurgeryTime(String leaveSurgeryTime) {
        this.leaveSurgeryTime = leaveSurgeryTime;
    }
    
    /**
     * 检查数据是否完整（入室和离室时间都存在）
     */
    public boolean isComplete() {
        return admissionDate != null && admissionTime != null 
            && leaveSurgeryDate != null && leaveSurgeryTime != null
            && admissionTime.length() == 4 && leaveSurgeryTime.length() == 4;
    }
}


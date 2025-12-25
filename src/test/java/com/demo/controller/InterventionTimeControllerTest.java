package com.demo.controller;

import com.demo.Service.impl.IPatientService;
import com.demo.entity.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-level tests for updating InterventionTime via REST.
 * These tests create a Patient first to satisfy FK, then update CT and other fields, and verify persistence.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class InterventionTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IPatientService patientService;

    private Integer testPatientId;

    @BeforeEach
    void setupPatient() {
        if (testPatientId == null) {
            Patient p = new Patient();
            p.setGender("男");
            p.setAge(30);
            p.setIsGreenChannel("否");
            patientService.save(p);
            testPatientId = p.getPatientId();
            assertThat(testPatientId).isNotNull();
        }
    }

    @Test
    @DisplayName("PUT /api/intervention/update updates CT field")
    void testUpdateCTField() throws Exception {
        String jsonBody = "{"
                + "\"patientId\":" + testPatientId + ","
                + "\"CT\":\"0930\""
                + "}";

        mockMvc.perform(put("/api/intervention/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        MvcResult res = mockMvc.perform(get("/api/intervention/edit/{patientId}", testPatientId))
                .andExpect(status().isOk())
                .andReturn();

        String response = res.getResponse().getContentAsString();
        // Simple contains check to avoid tight coupling to Result wrapper
        assertThat(response).contains("\"CT\":\"0930\"");
    }

    @Test
    @DisplayName("PUT /api/intervention/update updates multiple event times")
    void testUpdateMultipleEventTimes() throws Exception {
        String jsonBody = "{"
                + "\"patientId\":" + testPatientId + ","
                + "\"ultrasound\":\"0815\","
                + "\"tourniquet\":\"0820\","
                + "\"bloodDraw\":\"0825\","
                + "\"ventilator\":\"0835\","
                + "\"cprStartTime\":\"0840\","
                + "\"cprEndTime\":\"0845\","
                + "\"transfusion\":\"是\","
                + "\"transfusionStart\":\"0900\","
                + "\"transfusionEnd\":\"0915\""
                + "}";

        mockMvc.perform(put("/api/intervention/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        MvcResult res = mockMvc.perform(get("/api/intervention/edit/{patientId}", testPatientId))
                .andExpect(status().isOk())
                .andReturn();

        String response = res.getResponse().getContentAsString();
        assertThat(response).contains("\"ultrasound\":\"0815\"");
        assertThat(response).contains("\"tourniquet\":\"0820\"");
        assertThat(response).contains("\"bloodDraw\":\"0825\"");
        assertThat(response).contains("\"ventilator\":\"0835\"");
        assertThat(response).contains("\"cprStartTime\":\"0840\"");
        assertThat(response).contains("\"cprEndTime\":\"0845\"");
        assertThat(response).contains("\"transfusion\":\"是\"");
        assertThat(response).contains("\"transfusionStart\":\"0900\"");
        assertThat(response).contains("\"transfusionEnd\":\"0915\"");
    }
}



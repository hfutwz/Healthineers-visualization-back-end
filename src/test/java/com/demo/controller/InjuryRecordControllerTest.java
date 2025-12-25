package com.demo.controller;

import com.demo.Service.impl.IInjuryRecordService;
import com.demo.dto.AddressCountDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"unchecked", "rawtypes"})
@WebMvcTest(controllers = InjuryRecordController.class)
class InjuryRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IInjuryRecordService injuryRecordService;

    @Test
    void locations_should_pass_years_single_value_and_filter() throws Exception {
        // Arrange mock result
        AddressCountDTO dto = new AddressCountDTO();
        dto.setLatitude(31.0);
        dto.setLongitude(121.0);
        dto.setCount(2L);
        when(injuryRecordService.getLocationsBySeasonsAndTime(anyList(), anyList(), anyList()))
                .thenReturn(Collections.singletonList(dto));

        // Act
        mockMvc.perform(get("/api/map/locations")
                        .param("years", "2023")
                        .param("seasons", "0")
                        .param("seasons", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Assert service received normalized params
        ArgumentCaptor<List<Integer>> seasonsCaptor = ArgumentCaptor.forClass((Class) List.class);
        ArgumentCaptor<List<Integer>> timePeriodsCaptor = ArgumentCaptor.forClass((Class) List.class);
        ArgumentCaptor<List<Integer>> yearsCaptor = ArgumentCaptor.forClass((Class) List.class);

        org.mockito.Mockito.verify(injuryRecordService)
                .getLocationsBySeasonsAndTime(seasonsCaptor.capture(), timePeriodsCaptor.capture(), yearsCaptor.capture());

        assertThat(seasonsCaptor.getValue()).containsExactlyInAnyOrder(0, 1);
        assertThat(timePeriodsCaptor.getValue()).isNull();
        assertThat(yearsCaptor.getValue()).containsExactly(2023);
    }
}



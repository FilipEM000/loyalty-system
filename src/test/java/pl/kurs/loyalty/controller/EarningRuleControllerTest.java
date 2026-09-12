package pl.kurs.loyalty.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.loyalty.dto.request.create.CreateEarningRuleRequest;
import pl.kurs.loyalty.dto.request.update.UpdateEarningRuleRequest;
import pl.kurs.loyalty.dto.response.EarningRuleResponse;
import pl.kurs.loyalty.exception.EarningRuleNotFoundException;
import pl.kurs.loyalty.exception.ProgramExpiredException;
import pl.kurs.loyalty.exception.ProgramNotFoundException;
import pl.kurs.loyalty.model.EarningEventType;
import pl.kurs.loyalty.service.EarningRuleService;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EarningRuleControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    EarningRuleService earningRuleService;

    @Test
    void getAllForProgram_dataCorrect_rulesReturned() throws Exception {
        EarningRuleResponse rule1 = new EarningRuleResponse(0L, "test_rule_name", "PURCHASE", 10, LocalDateTime.of(2025, 1, 1, 0, 0), null, true);
        EarningRuleResponse rule2 = new EarningRuleResponse(1L, "test_rule_name_2", "REVIEW", 5, LocalDateTime.of(2025, 1, 1, 0, 0), null, true);
        when(earningRuleService.getAllRulesForProgram(0L)).thenReturn(List.of(rule1, rule2));

        mockMvc.perform(get("/programs/0/earning-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(0))
                .andExpect(jsonPath("$[0].name").value("test_rule_name"))
                .andExpect(jsonPath("$[0].eventType").value("PURCHASE"))
                .andExpect(jsonPath("$[0].numberOfPoints").value(10))
                .andExpect(jsonPath("$[0].isActive").value(true))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].name").value("test_rule_name_2"))
                .andExpect(jsonPath("$[1].eventType").value("REVIEW"))
                .andExpect(jsonPath("$[1].numberOfPoints").value(5))
                .andExpect(jsonPath("$[1].isActive").value(true));
        verify(earningRuleService).getAllRulesForProgram(0L);
    }

    @Test
    void getById_dataCorrect_ruleReturned() throws Exception {
        EarningRuleResponse rule = new EarningRuleResponse(0L, "test_rule_name", "PURCHASE", 10, LocalDateTime.of(2025, 1, 1, 0, 0), null, true);
        when(earningRuleService.getRuleById(0L)).thenReturn(rule);

        mockMvc.perform(get("/earning-rules/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.name").value("test_rule_name"))
                .andExpect(jsonPath("$.eventType").value("PURCHASE"))
                .andExpect(jsonPath("$.numberOfPoints").value(10))
                .andExpect(jsonPath("$.isActive").value(true));
        verify(earningRuleService).getRuleById(0L);
    }

    @Test
    void getById_ruleNotFound_returns404() throws Exception {
        when(earningRuleService.getRuleById(99L)).thenThrow(new EarningRuleNotFoundException(99L));

        mockMvc.perform(get("/earning-rules/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.detail").exists());
        verify(earningRuleService).getRuleById(99L);
    }

    @Test
    void create_dataCorrect_ruleCreated() throws Exception {
        CreateEarningRuleRequest request = new CreateEarningRuleRequest("test_rule_name", EarningEventType.PURCHASE, 10, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        EarningRuleResponse rule = new EarningRuleResponse(0L, "test_rule_name", "PURCHASE", 10, LocalDateTime.of(2025, 1, 1, 0, 0), null, true);
        when(earningRuleService.createRule(0L, request)).thenReturn(rule);

        mockMvc.perform(post("/programs/0/earning-rules")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.name").value("test_rule_name"))
                .andExpect(jsonPath("$.numberOfPoints").value(10));
        verify(earningRuleService).createRule(0L, request);
    }

    @Test
    void create_programNotFound_returns404() throws Exception {
        CreateEarningRuleRequest request = new CreateEarningRuleRequest("test_rule_name", EarningEventType.PURCHASE, 10, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        when(earningRuleService.createRule(99L, request)).thenThrow(new ProgramNotFoundException(99L));

        mockMvc.perform(post("/programs/99/earning-rules")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists());
        verify(earningRuleService).createRule(99L, request);
    }

    @Test
    void create_programExpired_returns409() throws Exception {
        CreateEarningRuleRequest request = new CreateEarningRuleRequest("test_rule_name", EarningEventType.PURCHASE, 10, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        when(earningRuleService.createRule(0L, request)).thenThrow(new ProgramExpiredException());

        mockMvc.perform(post("/programs/0/earning-rules")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").exists());
        verify(earningRuleService).createRule(0L, request);
    }

    @Test
    void create_invalidData_returns400() throws Exception {
        CreateEarningRuleRequest request = new CreateEarningRuleRequest("", EarningEventType.PURCHASE, 10, LocalDateTime.of(2025, 1, 1, 0, 0), null);

        mockMvc.perform(post("/programs/0/earning-rules")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors").isArray());
        verify(earningRuleService, never()).createRule(anyLong(), any());
    }

    @Test
    void update_dataCorrect_ruleUpdated() throws Exception {
        UpdateEarningRuleRequest request = new UpdateEarningRuleRequest("updated_test_rule_name", EarningEventType.PURCHASE, 20, LocalDateTime.of(2025, 1, 1, 0, 0), null);

        mockMvc.perform(put("/earning-rules/0")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(earningRuleService).updateRule(0L, request);
    }

    @Test
    void delete_dataCorrect_ruleDeleted() throws Exception {
        mockMvc.perform(delete("/earning-rules/0"))
                .andExpect(status().isOk());
        verify(earningRuleService).deleteRule(0L);
    }
}

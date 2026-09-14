package pl.kurs.loyalty.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.loyalty.dto.request.EarnPointsRequest;
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.response.BalanceResponse;
import pl.kurs.loyalty.dto.response.EarnPointsResponse;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.PointsHistoryResponse;
import pl.kurs.loyalty.model.EarningEventType;
import pl.kurs.loyalty.model.TransactionType;
import pl.kurs.loyalty.service.PointsService;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PointsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PointsService pointsService;

    @Test
    void earnPoints_dataCorrect_pointsEarned() throws Exception {
        EarnPointsRequest request = new EarnPointsRequest(EarningEventType.PURCHASE, 1L, null, "test_ref_123");
        EarnPointsResponse response = new EarnPointsResponse(10L, 5, 15, "Rule: test_rule_name, reference number: test_ref_123");
        when(pointsService.earnPoints(1L, request)).thenReturn(response);

        mockMvc.perform(post("/users/1/points/earn")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.points").value(5))
                .andExpect(jsonPath("$.newBalance").value(15))
                .andExpect(jsonPath("$.description").value("Rule: test_rule_name, reference number: test_ref_123"));
        verify(pointsService).earnPoints(1L, request);
    }

    @Test
    void getBalances_dataCorrect_balancesReturned() throws Exception {
        BalanceResponse balance1 = new BalanceResponse(1L, "test_program_name_1", 100, LocalDateTime.of(2025, 1, 1, 0, 0));
        BalanceResponse balance2 = new BalanceResponse(2L, "test_program_name_2", 250, LocalDateTime.of(2025, 1, 1, 0, 0));
        when(pointsService.getUserBalances(1L)).thenReturn(List.of(balance1, balance2));

        mockMvc.perform(get("/users/1/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].programId").value(1))
                .andExpect(jsonPath("$[0].programName").value("test_program_name_1"))
                .andExpect(jsonPath("$[0].pointsBalance").value(100))
                .andExpect(jsonPath("$[1].programId").value(2))
                .andExpect(jsonPath("$[1].programName").value("test_program_name_2"))
                .andExpect(jsonPath("$[1].pointsBalance").value(250));
        verify(pointsService).getUserBalances(1L);
    }

    @Test
    void earnPoints_missingReferenceId_returns400() throws Exception {
        EarnPointsRequest request = new EarnPointsRequest(EarningEventType.PURCHASE, 1L, null, "");

        mockMvc.perform(post("/users/{userId}/points/earn", 1L)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void getHistory_dataCorrect_historyReturned() throws Exception {
        PointsHistoryResponse resp1 = new PointsHistoryResponse(
                "test_program_name",
                LocalDateTime.of(2025, 1, 1, 10, 0),
                10,
                TransactionType.EARN,
                "test_desc"
        );
        Page<PointsHistoryResponse> page = new PageImpl<>(List.of(resp1));
        PageResponse<PointsHistoryResponse> response = new PageResponse<>(page);
        when(pointsService.getUserHistory(any(), any(GetPageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/users/1/points/history")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].programName").value("test_program_name"))
                .andExpect(jsonPath("$.content[0].points").value(10))
                .andExpect(jsonPath("$.content[0].type").value("EARN"))
                .andExpect(jsonPath("$.content[0].description").value("test_desc"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
        verify(pointsService).getUserHistory(any(), any(GetPageRequest.class));
    }
}

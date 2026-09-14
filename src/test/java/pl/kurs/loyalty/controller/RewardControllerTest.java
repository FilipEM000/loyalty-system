package pl.kurs.loyalty.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.loyalty.dto.request.create.CreateRewardRequest;
import pl.kurs.loyalty.dto.request.update.UpdateRewardRequest;
import pl.kurs.loyalty.dto.response.RewardResponse;
import pl.kurs.loyalty.exception.ProgramExpiredException;
import pl.kurs.loyalty.exception.ProgramNotFoundException;
import pl.kurs.loyalty.exception.RewardNotFoundException;
import pl.kurs.loyalty.service.RewardService;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RewardControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    RewardService rewardService;

    @Test
    void getAllForProgram_dataCorrect_rewardsReturned() throws Exception {
        RewardResponse reward1 = new RewardResponse(0L, "test_reward_name", "test_description", 100, 50, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        RewardResponse reward2 = new RewardResponse(1L, "test_reward_name_2", "test_description_2", 200, null, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        when(rewardService.getAllRewardsForProgram(0L)).thenReturn(List.of(reward1, reward2));

        mockMvc.perform(get("/programs/0/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(0))
                .andExpect(jsonPath("$[0].name").value("test_reward_name"))
                .andExpect(jsonPath("$[0].description").value("test_description"))
                .andExpect(jsonPath("$[0].cost").value(100))
                .andExpect(jsonPath("$[0].availableQuantity").value(50))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].name").value("test_reward_name_2"))
                .andExpect(jsonPath("$[1].cost").value(200))
                .andExpect(jsonPath("$[1].availableQuantity").doesNotExist()); // null
        verify(rewardService).getAllRewardsForProgram(0L);
    }

    @Test
    void getById_dataCorrect_rewardReturned() throws Exception {
        RewardResponse reward = new RewardResponse(0L, "test_reward_name", "test_description", 100, 50, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        when(rewardService.getRewardById(0L)).thenReturn(reward);

        mockMvc.perform(get("/rewards/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.name").value("test_reward_name"))
                .andExpect(jsonPath("$.description").value("test_description"))
                .andExpect(jsonPath("$.cost").value(100))
                .andExpect(jsonPath("$.availableQuantity").value(50));
        verify(rewardService).getRewardById(0L);
    }

    @Test
    void getById_rewardNotFound_returns404() throws Exception {
        when(rewardService.getRewardById(99L)).thenThrow(new RewardNotFoundException(99L));

        mockMvc.perform(get("/rewards/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.detail").exists());
        verify(rewardService).getRewardById(99L);
    }

    @Test
    void create_dataCorrect_rewardCreated() throws Exception {
        CreateRewardRequest request = new CreateRewardRequest("test_reward_name", "test_description", 100, 50, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        RewardResponse reward = new RewardResponse(0L, "test_reward_name", "test_description", 100, 50, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        when(rewardService.createReward(0L, request)).thenReturn(reward);

        mockMvc.perform(post("/programs/{programId}/rewards", 0L)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.name").value("test_reward_name"))
                .andExpect(jsonPath("$.cost").value(100))
                .andExpect(jsonPath("$.availableQuantity").value(50));
        verify(rewardService).createReward(0L, request);
    }

    @Test
    void create_programNotFound_returns404() throws Exception {
        CreateRewardRequest request = new CreateRewardRequest("test_reward_name", "test_description", 100, 50, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        when(rewardService.createReward(99L, request)).thenThrow(new ProgramNotFoundException(99L));

        mockMvc.perform(post("/programs/99/rewards")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists());
        verify(rewardService).createReward(99L, request);
    }

    @Test
    void create_programExpired_returns409() throws Exception {
        CreateRewardRequest request = new CreateRewardRequest("test_reward_name", "test_description", 100, 50, LocalDateTime.of(2025, 1, 1, 0, 0), null);
        when(rewardService.createReward(0L, request)).thenThrow(new ProgramExpiredException());

        mockMvc.perform(post("/programs/0/rewards")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").exists());
        verify(rewardService).createReward(0L, request);
    }

    @Test
    void create_invalidData_returns400() throws Exception {
        CreateRewardRequest request = new CreateRewardRequest("", "test_description", 0, 50, LocalDateTime.of(2025, 1, 1, 0, 0), null);

        mockMvc.perform(post("/programs/0/rewards")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors").isArray());
        verify(rewardService, never()).createReward(anyLong(), any());
    }

    @Test
    void update_dataCorrect_rewardUpdated() throws Exception {
        UpdateRewardRequest request = new UpdateRewardRequest("updated_test_reward_name", "updated_test_description", 200, 10, LocalDateTime.of(2025, 1, 1, 0, 0), null);

        mockMvc.perform(put("/rewards/0")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(rewardService).updateReward(0L, request);
    }

    @Test
    void delete_dataCorrect_rewardDeleted() throws Exception {
        mockMvc.perform(delete("/rewards/{rewardId}", 0L))
                .andExpect(status().isOk());
        verify(rewardService).deleteReward(0L);
    }
}

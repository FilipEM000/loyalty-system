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
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.request.create.CreateLoyaltyProgramRequest;
import pl.kurs.loyalty.dto.request.update.UpdateLoyaltyProgramRequest;
import pl.kurs.loyalty.dto.response.LoyaltyProgramResponse;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.UserResponse;
import pl.kurs.loyalty.exception.ProgramAlreadyExistsException;
import pl.kurs.loyalty.exception.ProgramHasActiveMembersException;
import pl.kurs.loyalty.exception.ProgramNotFoundException;
import pl.kurs.loyalty.service.LoyaltyProgramService;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LoyaltyProgramControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    LoyaltyProgramService loyaltyProgramService;

    @Test
    void getAll_dataCorrect_programsReturned() throws Exception {
        LoyaltyProgramResponse program1 = new LoyaltyProgramResponse(0L, "test_name", "test_description", LocalDateTime.of(2025, 1, 1, 0, 0), null, true);
        LoyaltyProgramResponse program2 = new LoyaltyProgramResponse(1L, "test_name_2", "test_description_2", LocalDateTime.of(2025, 2, 1, 0, 0), null, false);
        Page<LoyaltyProgramResponse> page = new PageImpl<>(List.of(program1, program2));
        PageResponse<LoyaltyProgramResponse> pageResponse = new PageResponse<>(page);
        when(loyaltyProgramService.getAllPrograms(any(GetPageRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/programs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(0))
                .andExpect(jsonPath("$.content[0].name").value("test_name"))
                .andExpect(jsonPath("$.content[0].description").value("test_description"))
                .andExpect(jsonPath("$.content[0].isActive").value(true))
                .andExpect(jsonPath("$.content[1].id").value(1))
                .andExpect(jsonPath("$.content[1].name").value("test_name_2"))
                .andExpect(jsonPath("$.content[1].isActive").value(false))
                .andExpect(jsonPath("$.totalElements").value(2));
        verify(loyaltyProgramService).getAllPrograms(any(GetPageRequest.class));
    }

    @Test
    void getById_dataCorrect_programReturned() throws Exception {
        LoyaltyProgramResponse program = new LoyaltyProgramResponse(0L, "test_name", "test_description", LocalDateTime.of(2025, 1, 1, 0, 0), null, true);
        when(loyaltyProgramService.getProgramById(0L)).thenReturn(program);

        mockMvc.perform(get("/programs/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.name").value("test_name"))
                .andExpect(jsonPath("$.description").value("test_description"))
                .andExpect(jsonPath("$.isActive").value(true));
        verify(loyaltyProgramService).getProgramById(0L);
    }

    @Test
    void getById_programNotFound_returns404() throws Exception {
        when(loyaltyProgramService.getProgramById(99L)).thenThrow(new ProgramNotFoundException(99L));

        mockMvc.perform(get("/programs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.detail").exists());
        verify(loyaltyProgramService).getProgramById(99L);
    }

    @Test
    void create_dataCorrect_programCreated() throws Exception {
        CreateLoyaltyProgramRequest request = new CreateLoyaltyProgramRequest("test_name", "test_description", LocalDateTime.of(2025, 1, 1, 0, 0), null);
        LoyaltyProgramResponse program = new LoyaltyProgramResponse(0L, "test_name", "test_description", LocalDateTime.of(2025, 1, 1, 0, 0), null, true);
        when(loyaltyProgramService.createProgram(request)).thenReturn(program);

        mockMvc.perform(post("/programs")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.name").value("test_name"));
        verify(loyaltyProgramService).createProgram(request);
    }

    @Test
    void create_programAlreadyExists_returns409() throws Exception {
        CreateLoyaltyProgramRequest request = new CreateLoyaltyProgramRequest("test_name", "test_description", LocalDateTime.of(2025, 1, 1, 0, 0), null);
        when(loyaltyProgramService.createProgram(request)).thenThrow(new ProgramAlreadyExistsException("test_name"));

        mockMvc.perform(post("/programs")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").exists());
        verify(loyaltyProgramService).createProgram(request);
    }

    @Test
    void create_invalidData_returns400() throws Exception {
        CreateLoyaltyProgramRequest request = new CreateLoyaltyProgramRequest("", "test_description", LocalDateTime.of(2025, 1, 1, 0, 0), null);

        mockMvc.perform(post("/programs")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors").isArray());
        verify(loyaltyProgramService, never()).createProgram(any());
    }

    @Test
    void update_dataCorrect_programUpdated() throws Exception {
        UpdateLoyaltyProgramRequest request = new UpdateLoyaltyProgramRequest("updated_test_name", "updated_test_description", LocalDateTime.of(2025, 1, 1, 0, 0), null);

        mockMvc.perform(put("/programs/0")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(loyaltyProgramService).updateProgram(0L, request);
    }

    @Test
    void delete_dataCorrect_programDeleted() throws Exception {
        mockMvc.perform(delete("/programs/0"))
                .andExpect(status().isOk());
        verify(loyaltyProgramService).deleteProgram(0L);
    }

    @Test
    void delete_programHasActiveMembers_returns409() throws Exception {
        doThrow(new ProgramHasActiveMembersException()).when(loyaltyProgramService).deleteProgram(0L);

        mockMvc.perform(delete("/programs/0"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").exists());
        verify(loyaltyProgramService).deleteProgram(0L);
    }

    @Test
    void getAllProgramUsers_dataCorrect_usersReturned() throws Exception {
        UserResponse user = new UserResponse(0L, "test_user_name", "test_user_lastName", "test@test.com", LocalDateTime.now(), new ArrayList<>());
        when(loyaltyProgramService.getProgramUsers(0L)).thenReturn(List.of(user));

        mockMvc.perform(get("/programs/0/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(0))
                .andExpect(jsonPath("$[0].name").value("test_user_name"));
        verify(loyaltyProgramService).getProgramUsers(0L);
    }
}

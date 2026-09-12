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
import pl.kurs.loyalty.dto.request.create.CreateUserRequest;
import pl.kurs.loyalty.dto.request.update.UpdateUserRequest;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.ProgramSummaryResponse;
import pl.kurs.loyalty.dto.response.UserResponse;
import pl.kurs.loyalty.exception.ProgramExpiredException;
import pl.kurs.loyalty.exception.UserAlreadyExistsException;
import pl.kurs.loyalty.exception.UserNotFoundException;
import pl.kurs.loyalty.service.UserService;
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
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @Test
    void getAll_dataCorrect_usersReturned() throws Exception {
        UserResponse user1 = new UserResponse(0L, "test_name", "test_lastName", "test1@test.com", LocalDateTime.of(2025, 10, 5, 15, 15), new ArrayList<>());
        UserResponse user2 = new UserResponse(1L, "test_name_2", "test_lastName_2", "test2@test.com", LocalDateTime.of(2025, 10, 6, 15, 15), new ArrayList<>());
        Page<UserResponse> page = new PageImpl<>(List.of(user1, user2));
        PageResponse<UserResponse> pageResponse = new PageResponse<>(page);

        when(userService.getAllUsers(any(GetPageRequest.class))).thenReturn(pageResponse);
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(0))
                .andExpect(jsonPath("$.content[0].name").value("test_name"))
                .andExpect(jsonPath("$.content[0].lastName").value("test_lastName"))
                .andExpect(jsonPath("$.content[0].email").value("test1@test.com"))
                .andExpect(jsonPath("$.content[1].id").value(1))
                .andExpect(jsonPath("$.content[1].name").value("test_name_2"))
                .andExpect(jsonPath("$.content[1].lastName").value("test_lastName_2"))
                .andExpect(jsonPath("$.content[1].email").value("test2@test.com"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
        verify(userService).getAllUsers(any(GetPageRequest.class));
    }

    @Test
    void getById_dataCorrect_userReturned() throws Exception {
        ProgramSummaryResponse program = new ProgramSummaryResponse(5L, "test_program");
        UserResponse user = new UserResponse(0L, "test_name", "test_lastName", "test@test.com", LocalDateTime.of(2025, 10, 5, 15, 15), List.of(program));
        when(userService.getUserById(0L)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", 0L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.name").value("test_name"))
                .andExpect(jsonPath("$.lastName").value("test_lastName"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.programs[0].id").value(5))
                .andExpect(jsonPath("$.programs[0].name").value("test_program"));
        verify(userService).getUserById(0L);
    }

    @Test
    void getById_userNotFound_returns404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.detail").exists());
        verify(userService).getUserById(99L);
    }

    @Test
    void create_dataCorrect_userCreated() throws Exception {
        CreateUserRequest request = new CreateUserRequest("test_name", "test_lastName", "test@test.com", null);
        UserResponse user = new UserResponse(0L, "test_name", "test_lastName", "test@test.com", LocalDateTime.of(2025, 10, 5, 15, 15), new ArrayList<>());
        when(userService.createUser(request)).thenReturn(user);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.name").value("test_name"))
                .andExpect(jsonPath("$.lastName").value("test_lastName"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
        verify(userService).createUser(request);
    }

    @Test
    void create_emailAlreadyExists_returns409() throws Exception {
        CreateUserRequest request = new CreateUserRequest("test_name", "test_lastName", "test@test.com", null);
        when(userService.createUser(request)).thenThrow(new UserAlreadyExistsException("zajety@email.pl"));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").exists());
        verify(userService).createUser(request);
    }

    @Test
    void create_invalidData_returns400() throws Exception {
        CreateUserRequest request = new CreateUserRequest("", "test_lastName", "invalid-email", null);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors").isArray());

        verify(userService, never()).createUser(any());
    }

    @Test
    void update_dataCorrect_userUpdated() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("updated_name", "updated_lastName", "updated@test.com");

        mockMvc.perform(put("/users/{id}", 0L)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(userService).updateUser(request, 0L);
    }

    @Test
    void delete_dataCorrect_userDeleted() throws Exception {
        mockMvc.perform(delete("/users/{id}", 0L))
                .andExpect(status().isOk());
        verify(userService).deleteUser(0L);
    }

    @Test
    void getAllPrograms_dataCorrect_programsReturned() throws Exception {
        ProgramSummaryResponse program1 = new ProgramSummaryResponse(5L, "test_program_1");
        ProgramSummaryResponse program2 = new ProgramSummaryResponse(6L, "test_program_2");
        when(userService.getAllUserPrograms(0L)).thenReturn(List.of(program1, program2));

        mockMvc.perform(get("/users/{id}/programs", 0L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].name").value("test_program_1"))
                .andExpect(jsonPath("$[1].id").value(6))
                .andExpect(jsonPath("$[1].name").value("test_program_2"));
        verify(userService).getAllUserPrograms(0L);
    }

    @Test
    void assignProgram_dataCorrect_programAssigned() throws Exception {
        mockMvc.perform(post("/users/{userId}/programs/{programId}", 0L, 5L))
                .andExpect(status().isOk());
        verify(userService).assignProgram(0L, 5L);
    }

    @Test
    void assignProgram_programExpired_returns409() throws Exception {
        doThrow(new ProgramExpiredException()).when(userService).assignProgram(1L, 2L);

        mockMvc.perform(post("/users/1/programs/2"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").exists());
        verify(userService).assignProgram(1L, 2L);
    }

    @Test
    void unassignProgram_dataCorrect_programUnassigned() throws Exception {
        mockMvc.perform(delete("/users/{userId}/programs/{programId}", 0L, 5L))
                .andExpect(status().isOk());
        verify(userService).unassignProgram(0L, 5L);
    }

}

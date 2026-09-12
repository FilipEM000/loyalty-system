package pl.kurs.loyalty.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pl.kurs.loyalty.dto.request.create.CreateUserRequest;
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.request.update.UpdateUserRequest;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.ProgramSummaryResponse;
import pl.kurs.loyalty.dto.response.UserResponse;
import pl.kurs.loyalty.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(description = "Get All users with pagination")
    @GetMapping
    public PageResponse<UserResponse> getAll(GetPageRequest getPageRequest) {
        log.info("Received request get all users with pagination");
        return userService.getAllUsers(getPageRequest);
    }

    @Operation(description = "Get user by ID")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        log.info("Received request get user by id: {}", id);
        return userService.getUserById(id);
    }

    @Operation(description = "Create new user")
    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest createUserRequest) {
        log.info("Received request to create user with email: {}", createUserRequest.email());
        return userService.createUser(createUserRequest);
    }

    @Operation(description = "Update user by ID")
    @PutMapping("/{id}")
    public void update(@Valid @RequestBody UpdateUserRequest updateUserRequest, @PathVariable Long id) {
        log.info("Received request to update user with id: {}", id);
        userService.updateUser(updateUserRequest, id);
    }

    @Operation(description = "Delete user by ID")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Received request to delete user with id: {}", id);
        userService.deleteUser(id);
    }

    @Operation(description = "Get all user programs bu user ID")
    @GetMapping("/{id}/programs")
    public List<ProgramSummaryResponse> getAllPrograms(@PathVariable Long id) {
        log.info("Received request to get all programs for user with id: {}", id);
        return userService.getAllUserPrograms(id);
    }

    @Operation(description = "Assign program to user")
    @PostMapping("/{userId}/programs/{programId}")
    public void assignProgram(@PathVariable Long userId, @PathVariable Long programId) {
        log.info("Received request to assign program with id {} to user with id: {}", programId, userId);
        userService.assignProgram(userId, programId);
    }

    @Operation(description = "Unassign program from user")
    @DeleteMapping("/{userId}/programs/{programId}")
    public void unassignProgram(@PathVariable Long userId, @PathVariable Long programId) {
        log.info("Received request to unassign program with id {} from user with id: {}", programId, userId);
        userService.unassignProgram(userId, programId);
    }
}

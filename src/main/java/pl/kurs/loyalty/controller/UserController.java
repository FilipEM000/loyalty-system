package pl.kurs.loyalty.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.kurs.loyalty.dto.request.CreateUserRequest;
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.request.UpdateUserRequest;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.ProgramSummaryResponse;
import pl.kurs.loyalty.dto.response.UserResponse;
import pl.kurs.loyalty.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(description = "Get All users with pagination")
    @GetMapping
    public PageResponse<UserResponse> getAll(GetPageRequest getPageRequest) {
        return userService.getAllUsers(getPageRequest);
    }

    @Operation(description = "Get user by ID")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Operation(description = "Create new user")
    @PostMapping
    public UserResponse create(@RequestBody CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest);
    }

    @Operation(description = "Update user by ID")
    @PutMapping("/{id}")
    public void update(@Valid @RequestBody UpdateUserRequest updateUserRequest, @PathVariable Long id) {
        userService.updateUser(updateUserRequest, id);
    }

    @Operation(description = "Delete user by ID")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @Operation(description = "Get all user programs bu user ID")
    @GetMapping("/{id}/programs")
    public List<ProgramSummaryResponse> getAllPrograms(@PathVariable Long id) {
        return userService.getAllUserPrograms(id);
    }

    @Operation(description = "Assign program to user")
    @PostMapping("/{userId}/programs/{programId}")
    public void assignProgram(@PathVariable Long userId, @PathVariable Long programId) {
        userService.assignProgram(userId, programId);
    }

    @Operation(description = "Unassign program from user")
    @DeleteMapping("/{userId}/programs/{programId}")
    public void deleteProgram(@PathVariable Long userId, @PathVariable Long programId) {
        userService.unassignProgram(userId, programId);
    }
}

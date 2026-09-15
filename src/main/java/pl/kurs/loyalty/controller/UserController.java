package pl.kurs.loyalty.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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

    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    @GetMapping
    public PageResponse<UserResponse> getAll(GetPageRequest getPageRequest) {
        log.info("Received request get all users with pagination");
        return userService.getAllUsers(getPageRequest);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        log.info("Received request get user by id: {}", id);
        return userService.getUserById(id);
    }

    @Operation(summary = "Create user", description = "Registers a new user in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Email already in use",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest createUserRequest) {
        log.info("Received request to create user with email: {}", createUserRequest.email());
        return userService.createUser(createUserRequest);
    }

    @Operation(summary = "Update user", description = "Updates profile details of an existing user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public void update(@Valid @RequestBody UpdateUserRequest updateUserRequest, @PathVariable Long id) {
        log.info("Received request to update user with id: {}", id);
        userService.updateUser(updateUserRequest, id);
    }

    @Operation(summary = "Delete user", description = "Deletes a user from the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Received request to delete user with id: {}", id);
        userService.deleteUser(id);
    }

    @Operation(summary = "Get user programs", description = "Retrieves a list of loyalty programs the user belongs to.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Programs retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}/programs")
    public List<ProgramSummaryResponse> getAllPrograms(@PathVariable Long id) {
        log.info("Received request to get all programs for user with id: {}", id);
        return userService.getAllUserPrograms(id);
    }

    @Operation(summary = "Assign user to program", description = "Assigns an existing user to a specific loyalty program.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User assigned successfully"),
            @ApiResponse(responseCode = "404", description = "User or Program not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Program is expired or User is already a member",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{userId}/programs/{programId}")
    public void assignProgram(@PathVariable Long userId, @PathVariable Long programId) {
        log.info("Received request to assign program with id {} to user with id: {}", programId, userId);
        userService.assignProgram(userId, programId);
    }

    @Operation(summary = "Unassign user from program", description = "Removes a user from a loyalty program.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User unassigned successfully"),
            @ApiResponse(responseCode = "404", description = "User or Program not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "User does not belong to the program OR has active points balance",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{userId}/programs/{programId}")
    public void unassignProgram(@PathVariable Long userId, @PathVariable Long programId) {
        log.info("Received request to unassign program with id {} from user with id: {}", programId, userId);
        userService.unassignProgram(userId, programId);
    }
}

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
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.request.create.CreateLoyaltyProgramRequest;
import pl.kurs.loyalty.dto.request.update.UpdateLoyaltyProgramRequest;
import pl.kurs.loyalty.dto.response.LoyaltyProgramResponse;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.UserResponse;
import pl.kurs.loyalty.service.LoyaltyProgramService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/programs")
@RequiredArgsConstructor
public class LoyaltyProgramController {
    private final LoyaltyProgramService loyaltyProgramService;

    @Operation(summary = "Get all loyalty programs", description = "Retrieves a paginated list of all loyalty programs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Programs retrieved successfully")
    })
    @GetMapping
    public PageResponse<LoyaltyProgramResponse> getAll(GetPageRequest getPageRequest) {
        log.info("Received request to get all programs with pagination");
        return loyaltyProgramService.getAllPrograms(getPageRequest);
    }

    @Operation(summary = "Get loyalty program by ID", description = "Retrieves details of a specific loyalty program.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Program retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public LoyaltyProgramResponse getById(@PathVariable Long id) {
        log.info("Received request to get program with id {}", id);
        return loyaltyProgramService.getProgramById(id);
    }

    @Operation(summary = "Get loyalty program users", description = "Retrieves a list of all users who are members of a specific program.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}/users")
    public List<UserResponse> getAllProgramUsers(@PathVariable Long id) {
        log.info("Received request to get all users from program with id {}", id);
        return loyaltyProgramService.getProgramUsers(id);
    }

    @Operation(summary = "Create loyalty program", description = "Creates a new loyalty program.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Program created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Program name already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoyaltyProgramResponse create(@Valid @RequestBody CreateLoyaltyProgramRequest createLoyaltyProgramRequest) {
        log.info("Received request to create program [{}]", createLoyaltyProgramRequest.name());
        return loyaltyProgramService.createProgram(createLoyaltyProgramRequest);
    }

    @Operation(summary = "Update loyalty program", description = "Updates details of an existing loyalty program.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Program updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Program not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @Valid @RequestBody UpdateLoyaltyProgramRequest updateLoyaltyProgramRequest) {
        log.info("Received request to update program with id {}", id);
        loyaltyProgramService.updateProgram(id, updateLoyaltyProgramRequest);
    }

    @Operation(summary = "Delete loyalty program", description = "Deletes a loyalty program from the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Program deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Cannot delete program with active memberships",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Received request to delete program with id {}", id);
        loyaltyProgramService.deleteProgram(id);
    }
}

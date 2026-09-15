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
import pl.kurs.loyalty.dto.request.create.CreateRewardRequest;
import pl.kurs.loyalty.dto.request.update.UpdateRewardRequest;
import pl.kurs.loyalty.dto.response.RewardResponse;
import pl.kurs.loyalty.service.RewardService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RewardController {
    private final RewardService rewardService;

    @Operation(summary = "Get all rewards for program", description = "Retrieves a list of all rewards associated with a specific loyalty program.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rewards retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/programs/{programId}/rewards")
    public List<RewardResponse> getAllForProgram(@PathVariable Long programId) {
        log.info("Received request get all rewards with pagination");
        return rewardService.getAllRewardsForProgram(programId);
    }

    @Operation(summary = "Get reward by ID", description = "Retrieves details of a specific reward.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reward retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Reward not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/rewards/{rewardId}")
    public RewardResponse getById(@PathVariable Long rewardId) {
        log.info("Received request get all rewards with id {}", rewardId);
        return rewardService.getRewardById(rewardId);
    }

    @Operation(summary = "Create reward", description = "Creates a new reward for a specific loyalty program.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reward created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Program not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Program is already expired",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/programs/{programId}/rewards")
    @ResponseStatus(HttpStatus.CREATED)
    public RewardResponse create(@PathVariable Long programId, @Valid @RequestBody CreateRewardRequest createRewardRequest) {
        log.info("Received request to create reward for program with id {}", programId);
        return rewardService.createReward(programId, createRewardRequest);
    }

    @Operation(summary = "Update reward", description = "Updates details of an existing reward.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reward updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Reward not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/rewards/{rewardId}")
    public void update(@PathVariable Long rewardId, @Valid @RequestBody UpdateRewardRequest updateRewardRequest) {
        log.info("Received request to update reward with id {}", rewardId);
        rewardService.updateReward(rewardId, updateRewardRequest);
    }

    @Operation(summary = "Delete reward", description = "Deletes a reward from the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reward deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Reward not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/rewards/{rewardId}")
    public void delete(@PathVariable Long rewardId) {
        log.info("Received request to delete reward with id {}", rewardId);
        rewardService.deleteReward(rewardId);
    }
}

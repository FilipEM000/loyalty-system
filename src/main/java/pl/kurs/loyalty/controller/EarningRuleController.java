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
import pl.kurs.loyalty.dto.request.create.CreateEarningRuleRequest;
import pl.kurs.loyalty.dto.request.update.UpdateEarningRuleRequest;
import pl.kurs.loyalty.dto.response.EarningRuleResponse;
import pl.kurs.loyalty.service.EarningRuleService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class EarningRuleController {
    private final EarningRuleService earningRuleService;

    @Operation(summary = "Get all earning rules for program", description = "Retrieves a list of all earning rules associated with a specific loyalty program.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rules retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("programs/{programId}/earning-rules")
    public List<EarningRuleResponse> getAllForProgram(@PathVariable Long programId) {
        log.info("Received request to get all earning rules with pagination");
        return earningRuleService.getAllRulesForProgram(programId);
    }

    @Operation(summary = "Get earning rule by ID", description = "Retrieves details of a specific earning rule.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/earning-rules/{ruleId}")
    public EarningRuleResponse getById(@PathVariable Long ruleId) {
        log.info("Received request to get earning rule with id {}", ruleId);
        return earningRuleService.getRuleById(ruleId);
    }

    @Operation(summary = "Create earning rule", description = "Creates a new earning rule for a specific loyalty program.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rule created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Program not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Program is already expired (or conflicting rule exists)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/programs/{programId}/earning-rules")
    @ResponseStatus(HttpStatus.CREATED)
    public EarningRuleResponse create(@PathVariable Long programId, @Valid @RequestBody CreateEarningRuleRequest createEarningRuleRequest) {
        log.info("Received request to create earning rule [{}]", createEarningRuleRequest.name());
        return earningRuleService.createRule(programId, createEarningRuleRequest);
    }

    @Operation(summary = "Update earning rule", description = "Updates details of an existing earning rule. Note: The program assignment cannot be changed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or attempt to change program assignment",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Rule not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/earning-rules/{ruleId}")
    public void update(@PathVariable Long ruleId, @Valid @RequestBody UpdateEarningRuleRequest updateEarningRuleRequest) {
        log.info("Received request to update earning rule with id {}", ruleId);
        earningRuleService.updateRule(ruleId, updateEarningRuleRequest);
    }

    @Operation(summary = "Delete earning rule", description = "Deletes an earning rule from the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/earning-rules/{ruleId}")
    public void delete(@PathVariable Long ruleId) {
        log.info("Received request to delete earning rule with id {}", ruleId);
        earningRuleService.deleteRule(ruleId);
    }
}

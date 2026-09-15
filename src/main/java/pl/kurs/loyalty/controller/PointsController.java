package pl.kurs.loyalty.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;
import pl.kurs.loyalty.dto.request.EarnPointsRequest;
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.response.BalanceResponse;
import pl.kurs.loyalty.dto.response.EarnPointsResponse;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.PointsHistoryResponse;
import pl.kurs.loyalty.service.PointsService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PointsController {
    private final PointsService pointsService;

    @Operation(summary = "Earn points", description = "Registers an event and calculates points for a user. You must provide either earningRuleId OR both eventType and programId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Points earned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EarnPointsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request format or ambiguous membership",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "User does not belong to the requested program",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Program is expired or no active earning rule found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/users/{userId}/points/earn")
    public EarnPointsResponse earnPoints(@PathVariable Long userId, @Valid @RequestBody EarnPointsRequest earnPointsRequest) {
        log.info("Received request to register earn point request and calculate points");
        return pointsService.earnPoints(userId, earnPointsRequest);
    }

    @Operation(summary = "Get user balances", description = "Returns points balances for all programs the user is a member of.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balances returned successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/users/{userId}/points")
    public List<BalanceResponse> getBalances(@PathVariable Long userId) {
        log.info("Received request to get points balances for user ID: {}", userId);
        return pointsService.getUserBalances(userId);
    }

    @Operation(summary = "Get user points history", description = "Returns paginated points transaction history for the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History returned successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/users/{userId}/points/history")
    public PageResponse<PointsHistoryResponse> getHistory(@PathVariable Long userId, GetPageRequest getPageRequest) {
        log.info("Received request to get points history for user ID: {}", userId);
        return pointsService.getUserHistory(userId, getPageRequest);
    }
}

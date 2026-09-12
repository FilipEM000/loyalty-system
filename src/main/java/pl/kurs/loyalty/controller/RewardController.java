package pl.kurs.loyalty.controller;

import jakarta.validation.Valid;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Description("Get all rewards for program")
    @GetMapping("/programs/{programId}/rewards")
    public List<RewardResponse> getAllForProgram(@PathVariable Long programId) {
        log.info("Received request get all rewards with pagination");
        return rewardService.getAllRewardsForProgram(programId);
    }

    @Description("Get reward by ID")
    @GetMapping("/rewards/{rewardId}")
    public RewardResponse getById(@PathVariable Long rewardId) {
        log.info("Received request get all rewards with id {}", rewardId);
        return rewardService.getRewardById(rewardId);
    }

    @Description("Create reward")
    @PostMapping("/programs/{programId}/rewards")
    public RewardResponse create(@PathVariable Long programId, @Valid @RequestBody CreateRewardRequest createRewardRequest) {
        log.info("Received request to create reward for program with id {}", programId);
        return rewardService.createReward(programId, createRewardRequest);
    }

    @Description("Update reward")
    @PutMapping("/rewards/{rewardId}")
    public void update(@PathVariable Long rewardId, @Valid @RequestBody UpdateRewardRequest updateRewardRequest) {
        log.info("Received request to update reward with id {}", rewardId);
        rewardService.updateReward(rewardId, updateRewardRequest);
    }

    @DeleteMapping("/rewards/{rewardId}")
    public void delete(@PathVariable Long rewardId) {
        log.info("Received request to delete reward with id {}", rewardId);
        rewardService.deleteReward(rewardId);
    }
}

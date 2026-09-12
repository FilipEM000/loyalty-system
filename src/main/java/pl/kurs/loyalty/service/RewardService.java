package pl.kurs.loyalty.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.kurs.loyalty.dto.request.create.CreateRewardRequest;
import pl.kurs.loyalty.dto.request.update.UpdateRewardRequest;
import pl.kurs.loyalty.dto.response.RewardResponse;
import pl.kurs.loyalty.exception.ProgramExpiredException;
import pl.kurs.loyalty.exception.ProgramNotFoundException;
import pl.kurs.loyalty.exception.RewardNotFoundException;
import pl.kurs.loyalty.mapper.RewardMapper;
import pl.kurs.loyalty.model.LoyaltyProgram;
import pl.kurs.loyalty.model.Reward;
import pl.kurs.loyalty.repository.LoyaltyProgramJpaRepository;
import pl.kurs.loyalty.repository.RewardJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RewardService {
    private final RewardJpaRepository rewardJpaRepository;
    private final LoyaltyProgramJpaRepository loyaltyProgramJpaRepository;
    private final RewardMapper rewardMapper;

    public List<RewardResponse> getAllRewardsForProgram(Long programId) {
        LoyaltyProgram loyaltyProgram = findProgramById(programId);
        return loyaltyProgram.getRewards().stream()
                .map(rewardMapper::toResponse)
                .toList();
    }

    public RewardResponse getRewardById(Long rewardId) {
        Reward reward = findRewardById(rewardId);
        return rewardMapper.toResponse(reward);
    }

    @Transactional
    public RewardResponse createReward(Long programId, CreateRewardRequest createRewardRequest) {
        log.info("Attempting to create new reward with name: {}", createRewardRequest.name());
        LoyaltyProgram loyaltyProgram = findProgramById(programId);
        if (!loyaltyProgram.getValidityPeriod().isActiveAt(LocalDateTime.now())) {
            throw new ProgramExpiredException();
        }
        Reward reward = rewardMapper.toEntity(createRewardRequest);
        Reward saved = rewardJpaRepository.save(reward);
        loyaltyProgram.addReward(saved);
        loyaltyProgramJpaRepository.save(loyaltyProgram);
        log.info("Successfully created reward with ID: {}", saved.getId());
        return rewardMapper.toResponse(saved);
    }

    public void updateReward(Long rewardId, UpdateRewardRequest updateRewardRequest) {
        log.info("Attempting to update reward with ID: {}", rewardId);
        Reward reward = findRewardById(rewardId);
        reward.update(updateRewardRequest);
        rewardJpaRepository.save(reward);
        log.info("Successfully updated reward with ID: {}", rewardId);
    }

    public void deleteReward(Long rewardId) {
        log.info("Attempting to delete reward with ID: {}", rewardId);
        Reward reward = findRewardById(rewardId);
        rewardJpaRepository.delete(reward);
        log.info("Successfully deleted reward with ID: {}", rewardId);
    }

    private Reward findRewardById(Long rewardId) {
        return rewardJpaRepository.findById(rewardId)
                .orElseThrow(() -> new RewardNotFoundException(rewardId));
    }

    private LoyaltyProgram findProgramById(Long programId) {
        return loyaltyProgramJpaRepository.findById(programId)
                .orElseThrow(() -> new ProgramNotFoundException(programId));
    }
}

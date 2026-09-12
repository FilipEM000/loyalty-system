package pl.kurs.loyalty.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import pl.kurs.loyalty.dto.request.create.CreateRewardRequest;
import pl.kurs.loyalty.dto.request.update.UpdateRewardRequest;
import pl.kurs.loyalty.dto.response.RewardResponse;
import pl.kurs.loyalty.exception.ProgramExpiredException;
import pl.kurs.loyalty.mapper.RewardMapper;
import pl.kurs.loyalty.model.LoyaltyProgram;
import pl.kurs.loyalty.model.Period;
import pl.kurs.loyalty.model.Reward;
import pl.kurs.loyalty.repository.LoyaltyProgramJpaRepository;
import pl.kurs.loyalty.repository.RewardJpaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RewardServiceTest {
    RewardService rewardService;
    RewardJpaRepository rewardJpaRepository;
    LoyaltyProgramJpaRepository loyaltyProgramJpaRepository;
    RewardMapper rewardMapper;

    @BeforeEach
    void setup() {
        this.rewardJpaRepository = Mockito.mock(RewardJpaRepository.class);
        this.loyaltyProgramJpaRepository = Mockito.mock(LoyaltyProgramJpaRepository.class);
        this.rewardMapper = Mappers.getMapper(RewardMapper.class);
        this.rewardService = new RewardService(rewardJpaRepository, loyaltyProgramJpaRepository, rewardMapper);
    }

    @Test
    void getAllRewardsForProgram_dataCorrect_rewardsReturned() {
        Period period = new Period(LocalDateTime.of(2025, 10, 5, 15, 15), LocalDateTime.of(2026, 10, 5, 15, 15));
        LoyaltyProgram program = new LoyaltyProgram(0L, "test_program_name", "test_description", period, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Reward reward1 = new Reward(0L, "test_reward_name", "test_reward_desc", 100, period, 50, true, program);
        Reward reward2 = new Reward(1L, "test_reward_name_2", "test_reward_desc_2", 200, period, null, true, program);
        program.addReward(reward1);
        program.addReward(reward2);
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(program));

        List<RewardResponse> result = rewardService.getAllRewardsForProgram(0L);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, result.size()),
                () -> Assertions.assertEquals(0L, result.getFirst().id()),
                () -> Assertions.assertEquals("test_reward_name", result.getFirst().name()),
                () -> Assertions.assertEquals("test_reward_desc", result.getFirst().description()),
                () -> Assertions.assertEquals(100, result.getFirst().cost()),
                () -> Assertions.assertEquals(50, result.getFirst().availableQuantity()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 10, 5, 15, 15), result.get(0).startDate()),
                () -> Assertions.assertEquals(LocalDateTime.of(2026, 10, 5, 15, 15), result.get(0).endDate()),
                () -> Assertions.assertEquals(1L, result.get(1).id()),
                () -> Assertions.assertEquals("test_reward_name_2", result.get(1).name()),
                () -> Assertions.assertEquals("test_reward_desc_2", result.get(1).description()),
                () -> Assertions.assertEquals(200, result.get(1).cost()),
                () -> Assertions.assertNull(result.get(1).availableQuantity()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 10, 5, 15, 15), result.get(1).startDate()),
                () -> Assertions.assertEquals(LocalDateTime.of(2026, 10, 5, 15, 15), result.get(1).endDate())
        );
    }

    @Test
    void getRewardById_dataCorrect_rewardReturned() {
        Period period = new Period(LocalDateTime.of(2025, 10, 5, 15, 15), null);
        Reward reward = new Reward(0L, "test_reward_name", "test_reward_desc", 100, period, 50, true, null);
        when(rewardJpaRepository.findById(0L)).thenReturn(Optional.of(reward));

        RewardResponse result = rewardService.getRewardById(0L);

        Assertions.assertAll(
                () -> Assertions.assertEquals(0L, result.id()),
                () -> Assertions.assertEquals("test_reward_name", result.name()),
                () -> Assertions.assertEquals("test_reward_desc", result.description()),
                () -> Assertions.assertEquals(100, result.cost()),
                () -> Assertions.assertEquals(50, result.availableQuantity()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 10, 5, 15, 15), result.startDate()),
                () -> Assertions.assertNull(result.endDate())
        );
    }

    @Test
    void createReward_dataCorrect_rewardCreated() {
        CreateRewardRequest request = new CreateRewardRequest("test_reward_name", "test_reward_desc", 100, 50, LocalDateTime.of(2025, 10, 5, 15, 15), null);
        Period period = new Period(LocalDateTime.of(2026, 9, 1, 0, 0), null);
        LoyaltyProgram program = new LoyaltyProgram(0L, "test_program_name", "test_description", period, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Reward savedReward = new Reward(10L, "test_reward_name", "test_reward_desc", 100, new Period(LocalDateTime.of(2025, 10, 5, 15, 15), null), 50, true, program);
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(program));
        when(rewardJpaRepository.save(any(Reward.class))).thenReturn(savedReward);

        RewardResponse result = rewardService.createReward(0L, request);

        Assertions.assertAll(
                () -> Assertions.assertEquals(10L, result.id()),
                () -> Assertions.assertEquals("test_reward_name", result.name()),
                () -> Assertions.assertEquals("test_reward_desc", result.description()),
                () -> Assertions.assertEquals(100, result.cost()),
                () -> Assertions.assertEquals(50, result.availableQuantity()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 10, 5, 15, 15), result.startDate()),
                () -> Assertions.assertNull(result.endDate()),
                () -> verify(loyaltyProgramJpaRepository).save(program),
                () -> verify(rewardJpaRepository).save(any(Reward.class))
        );
    }

    @Test
    void createReward_programExpired_throwsProgramExpiredException() {
        CreateRewardRequest request = new CreateRewardRequest("test_reward_name", "test_reward_desc", 100, 50, LocalDateTime.of(2025, 10, 5, 15, 15), null);
        Period expiredPeriod = new Period(LocalDateTime.of(2020, 1, 1, 0, 0), LocalDateTime.of(2021, 1, 1, 0, 0));
        LoyaltyProgram program = new LoyaltyProgram(0L, "test_program_name", "test_description", expiredPeriod, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(program));

        assertThatExceptionOfType(ProgramExpiredException.class)
                .isThrownBy(() -> rewardService.createReward(0L, request));
        verify(rewardJpaRepository, never()).save(any());
    }

    @Test
    void updateReward_dataCorrect_rewardUpdated() {
        UpdateRewardRequest request = new UpdateRewardRequest("new_test_reward_name", "new_test_reward_desc", 200, 10, LocalDateTime.of(2027, 10, 5, 15, 15), LocalDateTime.of(2028, 10, 5, 15, 15));
        Period period = new Period(LocalDateTime.of(2025, 10, 5, 15, 15), null);
        Reward reward = new Reward(0L, "old_test_reward_name", "old_test_reward_desc", 100, period, 50, true, null);
        when(rewardJpaRepository.findById(0L)).thenReturn(Optional.of(reward));

        rewardService.updateReward(0L, request);

        Assertions.assertAll(
                () -> Assertions.assertEquals("new_test_reward_name", reward.getName()),
                () -> Assertions.assertEquals("new_test_reward_desc", reward.getDescription()),
                () -> Assertions.assertEquals(200, reward.getCost()),
                () -> Assertions.assertEquals(10, reward.getAvailableQuantity()),
                () -> Assertions.assertEquals(LocalDateTime.of(2027, 10, 5, 15, 15), reward.getValidityPeriod().getStartDate()),
                () -> Assertions.assertEquals(LocalDateTime.of(2028, 10, 5, 15, 15), reward.getValidityPeriod().getEndDate()),
                () -> verify(rewardJpaRepository).save(reward)
        );
    }

    @Test
    void deleteReward_dataCorrect_rewardDeleted() {
        Period period = new Period(LocalDateTime.of(2025, 10, 5, 15, 15), null);
        Reward reward = new Reward(0L, "test_reward_name", "test_reward_desc", 100, period, 50, true, null);
        when(rewardJpaRepository.findById(0L)).thenReturn(Optional.of(reward));

        rewardService.deleteReward(0L);

        verify(rewardJpaRepository).delete(reward);
    }
}

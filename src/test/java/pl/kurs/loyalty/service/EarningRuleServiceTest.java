package pl.kurs.loyalty.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import pl.kurs.loyalty.dto.request.create.CreateEarningRuleRequest;
import pl.kurs.loyalty.dto.request.update.UpdateEarningRuleRequest;
import pl.kurs.loyalty.dto.response.EarningRuleResponse;
import pl.kurs.loyalty.exception.ProgramExpiredException;
import pl.kurs.loyalty.mapper.EarningRuleMapper;
import pl.kurs.loyalty.model.EarningEventType;
import pl.kurs.loyalty.model.EarningRule;
import pl.kurs.loyalty.model.LoyaltyProgram;
import pl.kurs.loyalty.model.Period;
import pl.kurs.loyalty.repository.EarningRuleJpaRepository;
import pl.kurs.loyalty.repository.LoyaltyProgramJpaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EarningRuleServiceTest {
    EarningRuleService earningRuleService;
    EarningRuleJpaRepository earningRuleJpaRepository;
    LoyaltyProgramJpaRepository loyaltyProgramJpaRepository;
    EarningRuleMapper earningRuleMapper;

    @BeforeEach
    void setup() {
        this.earningRuleJpaRepository = Mockito.mock(EarningRuleJpaRepository.class);
        this.loyaltyProgramJpaRepository = Mockito.mock(LoyaltyProgramJpaRepository.class);
        this.earningRuleMapper = Mappers.getMapper(EarningRuleMapper.class);
        this.earningRuleService = new EarningRuleService(earningRuleJpaRepository, loyaltyProgramJpaRepository, earningRuleMapper);
    }

    @Test
    void getAllRulesForProgram_dataCorrect_rulesReturned() {
        Period period = new Period(LocalDateTime.of(2025, 10, 5, 15, 15), null);
        LoyaltyProgram program = new LoyaltyProgram(0L, "test_program_name", "test_description", period, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        EarningRule rule1 = new EarningRule(0L, "test_rule_name", EarningEventType.PURCHASE, 10, period, true, program);
        EarningRule rule2 = new EarningRule(1L, "test_rule_name_2", EarningEventType.REVIEW, 5, period, true, program);
        program.addRule(rule1);
        program.addRule(rule2);
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(program));

        List<EarningRuleResponse> result = earningRuleService.getAllRulesForProgram(0L);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, result.size()),
                () -> Assertions.assertEquals(0L, result.getFirst().id()),
                () -> Assertions.assertEquals("test_rule_name", result.getFirst().name()),
                () -> Assertions.assertEquals("PURCHASE", result.getFirst().eventType()), // DTO ma Stringa!
                () -> Assertions.assertEquals(10, result.getFirst().numberOfPoints()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 10, 5, 15, 15), result.getFirst().startDate()),
                () -> Assertions.assertNull(result.getFirst().endDate()),
                () -> Assertions.assertTrue(result.getFirst().isActive()),
                () -> Assertions.assertEquals(1L, result.get(1).id()),
                () -> Assertions.assertEquals("test_rule_name_2", result.get(1).name()),
                () -> Assertions.assertEquals("REVIEW", result.get(1).eventType()),
                () -> Assertions.assertEquals(5, result.get(1).numberOfPoints()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 10, 5, 15, 15), result.get(1).startDate()),
                () -> Assertions.assertNull(result.get(1).endDate()),
                () -> Assertions.assertTrue(result.get(1).isActive())
        );
    }

    @Test
    void getRuleById_dataCorrect_ruleReturned() {
        Period period = new Period(LocalDateTime.of(2025, 10, 5, 15, 15), null);
        EarningRule rule = new EarningRule(0L, "test_rule_name", EarningEventType.PURCHASE, 10, period, true, null);
        when(earningRuleJpaRepository.findById(0L)).thenReturn(Optional.of(rule));

        EarningRuleResponse result = earningRuleService.getRuleById(0L);

        Assertions.assertAll(
                () -> Assertions.assertEquals(0L, result.id()),
                () -> Assertions.assertEquals("test_rule_name", result.name()),
                () -> Assertions.assertEquals("PURCHASE", result.eventType()),
                () -> Assertions.assertEquals(10, result.numberOfPoints()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 10, 5, 15, 15), result.startDate()),
                () -> Assertions.assertNull(result.endDate()),
                () -> Assertions.assertTrue(result.isActive())
        );
    }

    @Test
    void createRule_dataCorrect_ruleCreated() {
        CreateEarningRuleRequest request = new CreateEarningRuleRequest("test_rule_name", EarningEventType.PURCHASE, 10, LocalDateTime.of(2025, 10, 5, 15, 15), null);
        Period period = new Period(LocalDateTime.of(2026, 9, 1, 0, 0), null);
        LoyaltyProgram program = new LoyaltyProgram(0L, "test_program_name", "test_description", period, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        EarningRule savedRule = new EarningRule(10L, "test_rule_name", EarningEventType.PURCHASE, 10, period, true, program);
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(program));
        when(earningRuleJpaRepository.save(any(EarningRule.class))).thenReturn(savedRule);

        EarningRuleResponse result = earningRuleService.createRule(0L, request);

        Assertions.assertAll(
                () -> Assertions.assertEquals(10L, result.id()),
                () -> Assertions.assertEquals("test_rule_name", result.name()),
                () -> Assertions.assertEquals("PURCHASE", result.eventType()),
                () -> Assertions.assertEquals(10, result.numberOfPoints()),
                () -> Assertions.assertEquals(LocalDateTime.of(2026, 9, 1, 0, 0), result.startDate()),
                () -> Assertions.assertNull(result.endDate()),
                () -> Assertions.assertTrue(result.isActive()),
                () -> verify(loyaltyProgramJpaRepository).save(any(LoyaltyProgram.class)),
                () -> verify(earningRuleJpaRepository).save(any(EarningRule.class))
        );
    }

    @Test
    void createRule_programExpired_throwsProgramExpiredException() {
        CreateEarningRuleRequest request = new CreateEarningRuleRequest("test_rule_name", EarningEventType.PURCHASE, 10, LocalDateTime.of(2025, 10, 5, 15, 15), null);
        Period expiredPeriod = new Period(LocalDateTime.of(2020, 1, 1, 0, 0), LocalDateTime.of(2021, 1, 1, 0, 0)); // Program już dawno wygasł
        LoyaltyProgram program = new LoyaltyProgram(0L, "test_program_name", "test_description", expiredPeriod, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(program));

        assertThatExceptionOfType(ProgramExpiredException.class)
                .isThrownBy(() -> earningRuleService.createRule(0L, request));
        verify(earningRuleJpaRepository, never()).save(any());
    }

    @Test
    void updateRule_dataCorrect_ruleUpdated() {
        UpdateEarningRuleRequest request = new UpdateEarningRuleRequest("new_test_rule_name", EarningEventType.REVIEW, 50, LocalDateTime.of(2027, 10, 5, 15, 15), null);
        Period period = new Period(LocalDateTime.of(2025, 10, 5, 15, 15), null);
        EarningRule rule = new EarningRule(0L, "old_test_rule_name", EarningEventType.PURCHASE, 10, period, true, null);
        when(earningRuleJpaRepository.findById(0L)).thenReturn(Optional.of(rule));

        earningRuleService.updateRule(0L, request);

        Assertions.assertAll(
                () -> Assertions.assertEquals("new_test_rule_name", rule.getName()),
                () -> Assertions.assertEquals(EarningEventType.REVIEW, rule.getEventType()),
                () -> Assertions.assertEquals(50, rule.getNumberOfPoints()),
                () -> Assertions.assertEquals(LocalDateTime.of(2027, 10, 5, 15, 15), rule.getValidityPeriod().getStartDate()),
                () -> verify(earningRuleJpaRepository).save(rule)
        );
    }

    @Test
    void deleteRule_dataCorrect_ruleDeleted() {
        Period period = new Period(LocalDateTime.of(2025, 10, 5, 15, 15), null);
        EarningRule rule = new EarningRule(0L, "test_rule_name", EarningEventType.PURCHASE, 10, period, true, null);
        when(earningRuleJpaRepository.findById(0L)).thenReturn(Optional.of(rule));

        earningRuleService.deleteRule(0L);

        verify(earningRuleJpaRepository).delete(rule);
    }
}

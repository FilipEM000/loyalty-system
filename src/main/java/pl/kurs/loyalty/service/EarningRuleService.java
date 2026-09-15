package pl.kurs.loyalty.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.kurs.loyalty.dto.request.create.CreateEarningRuleRequest;
import pl.kurs.loyalty.dto.request.update.UpdateEarningRuleRequest;
import pl.kurs.loyalty.dto.response.EarningRuleResponse;
import pl.kurs.loyalty.exception.EarningRuleNotFoundException;
import pl.kurs.loyalty.exception.ProgramExpiredException;
import pl.kurs.loyalty.exception.ProgramNotFoundException;
import pl.kurs.loyalty.mapper.EarningRuleMapper;
import pl.kurs.loyalty.model.EarningRule;
import pl.kurs.loyalty.model.LoyaltyProgram;
import pl.kurs.loyalty.repository.EarningRuleJpaRepository;
import pl.kurs.loyalty.repository.LoyaltyProgramJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EarningRuleService {
    private final EarningRuleJpaRepository earningRuleJpaRepository;
    private final LoyaltyProgramJpaRepository loyaltyProgramJpaRepository;
    private final EarningRuleMapper earningRuleMapper;

    public List<EarningRuleResponse> getAllRulesForProgram(Long programId) {
        LoyaltyProgram loyaltyProgram = findProgramById(programId);
        return loyaltyProgram.getEarningRules().stream()
                .map(earningRuleMapper::toResponse)
                .toList();
    }

    public EarningRuleResponse getRuleById(Long id) {
        EarningRule earningRule = findRuleById(id);
        return earningRuleMapper.toResponse(earningRule);
    }

    @Transactional
    public EarningRuleResponse createRule(Long programId, CreateEarningRuleRequest createEarningRuleRequest) {
        log.info("Attempting to create new earning rule with name: {}", createEarningRuleRequest.name());
        LoyaltyProgram loyaltyProgram = findProgramById(programId);

        if (!loyaltyProgram.getValidityPeriod().isActiveAt(LocalDateTime.now())) {
            throw new ProgramExpiredException();
        }
        EarningRule earningRule = earningRuleMapper.toEntity(createEarningRuleRequest);
        loyaltyProgram.addRule(earningRule);
        loyaltyProgramJpaRepository.save(loyaltyProgram);
        EarningRule saved = earningRuleJpaRepository.save(earningRule);
        log.info("Successfully created earning rule with ID: {}", saved.getId());
        return earningRuleMapper.toResponse(saved);
    }

    public void updateRule(Long ruleId, UpdateEarningRuleRequest updateEarningRuleRequest) {
        log.info("Attempting to update earning rule with ID: {}", ruleId);
        EarningRule earningRule = findRuleById(ruleId);
        earningRule.update(updateEarningRuleRequest);
        earningRuleJpaRepository.save(earningRule);
        log.info("Successfully updated earning rule with ID: {}", ruleId);
    }

    public void deleteRule(Long ruleId) {
        log.info("Attempting to delete earning rule with ID: {}", ruleId);
        EarningRule earningRule = findRuleById(ruleId);
        earningRuleJpaRepository.delete(earningRule);
        log.info("Successfully deleted earning rule with ID: {}", ruleId);
    }

    private EarningRule findRuleById(Long ruleId) {
        return earningRuleJpaRepository.findById(ruleId)
                .orElseThrow(EarningRuleNotFoundException::new);
    }

    private LoyaltyProgram findProgramById(Long programId) {
        return loyaltyProgramJpaRepository.findById(programId)
                .orElseThrow(() -> new ProgramNotFoundException(programId));
    }
}

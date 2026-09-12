package pl.kurs.loyalty.controller;

import jakarta.validation.Valid;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Description("Get all earning rules for program")
    @GetMapping("programs/{programId}/earning-rules")
    public List<EarningRuleResponse> getAllForProgram(@PathVariable Long programId) {
        log.info("Received request to get all earning rules with pagination");
        return earningRuleService.getAllRulesForProgram(programId);
    }

    @Description("Get earning rule by ID")
    @GetMapping("/earning-rules/{ruleId}")
    public EarningRuleResponse getById(@PathVariable Long ruleId) {
        log.info("Received request to get earning rule with id {}", ruleId);
        return earningRuleService.getRuleById(ruleId);
    }

    @Description("Create earning rule")
    @PostMapping("/programs/{programId}/earning-rules")
    public EarningRuleResponse create(@PathVariable Long programId, @Valid @RequestBody CreateEarningRuleRequest createEarningRuleRequest) {
        log.info("Received request to create earning rule [{}]", createEarningRuleRequest.name());
        return earningRuleService.createRule(programId, createEarningRuleRequest);
    }

    @Description("Update earning rule")
    @PutMapping("/earning-rules/{ruleId}")
    public void update(@PathVariable Long ruleId, @Valid @RequestBody UpdateEarningRuleRequest updateEarningRuleRequest) {
        log.info("Received request to update earning rule with id {}", ruleId);
        earningRuleService.updateRule(ruleId, updateEarningRuleRequest);
    }

    @Description("Delete earning rule")
    @DeleteMapping("/earning-rules/{ruleId}")
    public void delete(@PathVariable Long ruleId) {
        log.info("Received request to delete earning rule with id {}", ruleId);
        earningRuleService.deleteRule(ruleId);
    }
}

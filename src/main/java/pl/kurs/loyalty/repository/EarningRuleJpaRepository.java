package pl.kurs.loyalty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.loyalty.model.EarningEventType;
import pl.kurs.loyalty.model.EarningRule;

import java.util.Optional;

public interface EarningRuleJpaRepository extends JpaRepository<EarningRule, Long> {
    Optional<EarningRule> findByProgramIdAndEventType(Long programId, EarningEventType eventType);
}

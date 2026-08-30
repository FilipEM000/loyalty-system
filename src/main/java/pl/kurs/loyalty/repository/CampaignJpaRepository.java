package pl.kurs.loyalty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kurs.loyalty.model.Campaign;
import pl.kurs.loyalty.model.EarningEventType;
import pl.kurs.loyalty.model.LoyaltyProgram;

import java.time.LocalDateTime;
import java.util.List;

public interface CampaignJpaRepository extends JpaRepository<Campaign, Long> {
    @Query("SELECT c FROM Campaign c WHERE c.targetEventType = :event_type " +
            "AND (c.program = :program OR c.program IS NULL ) " +
            "AND c.validityPeriod.startDate <= :now AND (c.validityPeriod.endDate IS NULL OR c.validityPeriod.endDate >= :now)")
    List<Campaign> findActiveCampaigns(@Param("event_type") EarningEventType eventType, @Param("program") LoyaltyProgram program, @Param("now") LocalDateTime now);
}

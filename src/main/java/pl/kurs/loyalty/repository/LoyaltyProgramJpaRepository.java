package pl.kurs.loyalty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.loyalty.model.LoyaltyProgram;

public interface LoyaltyProgramJpaRepository extends JpaRepository<LoyaltyProgram, Long> {
}

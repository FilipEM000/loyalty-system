package pl.kurs.loyalty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.loyalty.model.Reward;

public interface RewardJpaRepository extends JpaRepository<Reward, Long> {
}

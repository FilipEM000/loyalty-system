package pl.kurs.loyalty.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.loyalty.model.PointsTransaction;

public interface PointsTransactionJpaRepository extends JpaRepository<PointsTransaction, Long> {
    Page<PointsTransaction> findByMembership_IdOrderByDateOfTransactionDesc(Long membershipId, Pageable pageable);
}

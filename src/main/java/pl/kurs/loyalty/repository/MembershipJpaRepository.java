package pl.kurs.loyalty.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kurs.loyalty.model.Membership;

import java.util.List;

public interface MembershipJpaRepository extends JpaRepository<Membership, Long> {
    boolean existsByUserIdAndProgramId(Long userId, Long programId);

    @Query("SELECT m.user.id AS userId, m.user.name AS displayName, SUM(m.pointsBalance) AS totalPoints " +
            "FROM Membership m " +
            "GROUP BY m.user.id, m.user.name " +
            "ORDER BY totalPoints DESC")
    List<LeaderboardEntry> findGlobalLeaderboard(Pageable pageable);

    @Query("SELECT m.user.id AS userId, m.user.name AS displayName, m.pointsBalance AS totalPoints " +
            "FROM Membership m " +
            "WHERE m.program.id = :programId " +
            "ORDER BY totalPoints DESC")
    List<LeaderboardEntry> findProgramLeaderboard(@Param("programId") Long programId, Pageable pageable);
}

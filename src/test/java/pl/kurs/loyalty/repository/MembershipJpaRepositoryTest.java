package pl.kurs.loyalty.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import pl.kurs.loyalty.model.LoyaltyProgram;
import pl.kurs.loyalty.model.Membership;
import pl.kurs.loyalty.model.User;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
public class MembershipJpaRepositoryTest {

    @Autowired
    private MembershipJpaRepository membershipJpaRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private LoyaltyProgramJpaRepository loyaltyProgramJpaRepository;

    @Test
    void findGlobalLeaderBoard_dataCorrect_leaderboardReturned() {
        User user = new User();
        user.setName("Jan");
        user.setLastName("Kowalski");
        user.setEmail("jan@test.pl");
        user.setRegistrationDate(LocalDateTime.now());
        userJpaRepository.save(user);

        User user2 = new User();
        user2.setName("Anna");
        user2.setLastName("Nowak");
        user2.setEmail("anna@test.pl");
        user2.setRegistrationDate(LocalDateTime.now());
        userJpaRepository.save(user2);

        LoyaltyProgram program = new LoyaltyProgram();
        program.setName("Silver");
        program.setStatus(true);
        loyaltyProgramJpaRepository.save(program);

        LoyaltyProgram program2 = new LoyaltyProgram();
        program2.setName("Gold");
        program2.setStatus(true);
        loyaltyProgramJpaRepository.save(program2);

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setProgram(program);
        membership.setPointsBalance(100L);
        membership.setVersion(0L);
        membershipJpaRepository.save(membership);

        Membership membership2 = new Membership();
        membership2.setUser(user2);
        membership2.setProgram(program2);
        membership2.setPointsBalance(50L);
        membership2.setVersion(0L);
        membershipJpaRepository.save(membership2);

        List<LeaderboardEntry> leaderboard = membershipJpaRepository.findGlobalLeaderboard(PageRequest.of(0, 10));

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, leaderboard.size()),
                () -> Assertions.assertEquals(user.getId(), leaderboard.getFirst().getUserId()),
                () -> Assertions.assertEquals("Jan", leaderboard.getFirst().getDisplayName()),
                () -> Assertions.assertEquals(100L, leaderboard.getFirst().getTotalPoints()),
                () -> Assertions.assertEquals(user2.getId(), leaderboard.get(1).getUserId()),
                () -> Assertions.assertEquals("Anna", leaderboard.get(1).getDisplayName()),
                () -> Assertions.assertEquals(50L, leaderboard.get(1).getTotalPoints())
        );
    }

    @Test
    void findProgramLeaderBoard_dataCorrect_leaderboardReturned() {
        User user = new User();
        user.setName("Tomasz");
        user.setLastName("Lis");
        user.setEmail("tomek@test.pl");
        user.setRegistrationDate(LocalDateTime.now());
        userJpaRepository.save(user);

        User user2 = new User();
        user2.setName("Kasia");
        user2.setLastName("Kowalska");
        user2.setEmail("kasia@test.pl");
        user2.setRegistrationDate(LocalDateTime.now());
        userJpaRepository.save(user2);

        LoyaltyProgram program = new LoyaltyProgram();
        program.setName("VIP");
        program.setStatus(true);
        loyaltyProgramJpaRepository.save(program);

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setProgram(program);
        membership.setPointsBalance(100L);
        membership.setVersion(0L);
        membershipJpaRepository.save(membership);

        Membership membership2 = new Membership();
        membership2.setUser(user2);
        membership2.setProgram(program);
        membership2.setPointsBalance(300L);
        membership2.setVersion(0L);
        membershipJpaRepository.save(membership2);

        List<LeaderboardEntry> leaderboard = membershipJpaRepository.findProgramLeaderboard(program.getId(), PageRequest.of(0, 10));

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, leaderboard.size()),
                () -> Assertions.assertEquals(user2.getId(), leaderboard.getFirst().getUserId()),
                () -> Assertions.assertEquals("Kasia", leaderboard.getFirst().getDisplayName()),
                () -> Assertions.assertEquals(300, leaderboard.getFirst().getTotalPoints()),
                () -> Assertions.assertEquals(user.getId(), leaderboard.get(1).getUserId()),
                () -> Assertions.assertEquals("Tomasz", leaderboard.get(1).getDisplayName()),
                () -> Assertions.assertEquals(100, leaderboard.get(1).getTotalPoints())
        );
    }
}

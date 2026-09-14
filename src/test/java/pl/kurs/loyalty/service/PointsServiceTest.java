package pl.kurs.loyalty.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pl.kurs.loyalty.dto.request.EarnPointsRequest;
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.response.BalanceResponse;
import pl.kurs.loyalty.dto.response.EarnPointsResponse;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.PointsHistoryResponse;
import pl.kurs.loyalty.exception.AmbiguousMembershipException;
import pl.kurs.loyalty.exception.EarnPointsRequestInvalidFormatException;
import pl.kurs.loyalty.exception.NoEarningRuleException;
import pl.kurs.loyalty.exception.ProgramExpiredException;
import pl.kurs.loyalty.model.*;
import pl.kurs.loyalty.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PointsServiceTest {

    PointsTransactionJpaRepository pointsTransactionJpaRepository;
    LoyaltyProgramJpaRepository loyaltyProgramJpaRepository;
    UserJpaRepository userJpaRepository;
    MembershipJpaRepository membershipJpaRepository;
    EarningRuleJpaRepository earningRuleJpaRepository;
    PointsService pointsService;

    @BeforeEach
    void setup() {
        pointsTransactionJpaRepository = Mockito.mock(PointsTransactionJpaRepository.class);
        loyaltyProgramJpaRepository = Mockito.mock(LoyaltyProgramJpaRepository.class);
        userJpaRepository = Mockito.mock(UserJpaRepository.class);
        membershipJpaRepository = Mockito.mock(MembershipJpaRepository.class);
        earningRuleJpaRepository = Mockito.mock(EarningRuleJpaRepository.class);
        pointsService = new PointsService(
                pointsTransactionJpaRepository,
                loyaltyProgramJpaRepository,
                userJpaRepository,
                membershipJpaRepository,
                earningRuleJpaRepository
        );
    }

    @Test
    void earnPoints_validRequestWithProgramId_pointsAddedAndTransactionSaved() {
        EarnPointsRequest request = new EarnPointsRequest(EarningEventType.PURCHASE, 1L, null, "test_ref_123");
        User user = new User(1L, "test_name", "test_lastName", "test@test.com", LocalDateTime.now(), new ArrayList<>());
        Period activePeriod = new Period(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(10));
        LoyaltyProgram program = new LoyaltyProgram(1L, "test_program_name", "test_description", activePeriod, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Membership membership = new Membership(1L, user, program, LocalDateTime.now(), 10L, 1L);
        EarningRule rule = new EarningRule(1L, "test_rule_name", EarningEventType.PURCHASE, 5, activePeriod, true, program);
        PointsTransaction savedTx = new PointsTransaction(10L, TransactionType.EARN, 5, "Rule: test_rule_name, reference number: test_ref_123", LocalDateTime.now(), 15, membership);
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipJpaRepository.findByUserIdAndProgramId(1L, 1L)).thenReturn(Optional.of(membership));
        when(earningRuleJpaRepository.findByProgramIdAndEventType(1L, EarningEventType.PURCHASE)).thenReturn(Optional.of(rule));
        when(pointsTransactionJpaRepository.save(any(PointsTransaction.class))).thenReturn(savedTx);

        EarnPointsResponse response = pointsService.earnPoints(1L, request);

        Assertions.assertAll(
                () -> Assertions.assertEquals(10L, response.id()),
                () -> Assertions.assertEquals(5, response.points()),
                () -> Assertions.assertEquals(15, response.newBalance()),
                () -> Assertions.assertEquals("Rule: test_rule_name, reference number: test_ref_123", response.description()),
                () -> Assertions.assertEquals(15L, membership.getPointsBalance()),
                () -> verify(pointsTransactionJpaRepository).save(any(PointsTransaction.class))
        );
    }

    @Test
    void earnPoints_bothRuleIdAndEventTypeProvided_throwsInvalidFormatException() {
        EarnPointsRequest request = new EarnPointsRequest(EarningEventType.PURCHASE, 1L, 10L, "test_ref_123");

        assertThatExceptionOfType(EarnPointsRequestInvalidFormatException.class)
                .isThrownBy(() -> pointsService.earnPoints(1L, request))
                .extracting(EarnPointsRequestInvalidFormatException::getMessage)
                .isEqualTo("You have to specify one of the approved formats for earning points");
    }

    @Test
    void earnPoints_noProgramIdAndMultipleMemberships_throwsAmbiguousMembership() {
        EarnPointsRequest request = new EarnPointsRequest(null, null, 10L, "test_ref_123");
        Membership membership1 = new Membership(1L, null, null, LocalDateTime.now(), 0L, 1L);
        Membership membership2 = new Membership(2L, null, null, LocalDateTime.now(), 0L, 1L);
        User user = new User(1L, "test_name", "test_lastName", "test@test.com", LocalDateTime.now(), List.of(membership1, membership2));
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatExceptionOfType(AmbiguousMembershipException.class)
                .isThrownBy(() -> pointsService.earnPoints(1L, request))
                .extracting(AmbiguousMembershipException::getMessage)
                .isEqualTo("User has multiple memberships");
    }

    @Test
    void earnPoints_programExpired_throwsProgramExpiredException() {
        EarnPointsRequest request = new EarnPointsRequest(EarningEventType.PURCHASE, 1L, null, "test_ref_123");
        User user = new User(1L, "test_name", "test_lastName", "test@test.com", LocalDateTime.now(), new ArrayList<>());
        Period expiredPeriod = new Period(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));
        LoyaltyProgram program = new LoyaltyProgram(1L, "test_program_name", "test_description", expiredPeriod, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Membership membership = new Membership(1L, user, program, LocalDateTime.now(), 0L, 1L);
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipJpaRepository.findByUserIdAndProgramId(1L, 1L)).thenReturn(Optional.of(membership));

        assertThatExceptionOfType(ProgramExpiredException.class)
                .isThrownBy(() -> pointsService.earnPoints(1L, request))
                .extracting(ProgramExpiredException::getMessage)
                .isEqualTo("Loyalty program expired");
    }

    @Test
    void earnPoints_noActiveEarningRule_throwsNoEarningRuleException() {
        EarnPointsRequest request = new EarnPointsRequest(EarningEventType.PURCHASE, 1L, null, "test_ref_123");
        User user = new User(1L, "test_name", "test_lastName", "test@test.com", LocalDateTime.now(), new ArrayList<>());
        Period activePeriod = new Period(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(10));
        LoyaltyProgram program = new LoyaltyProgram(1L, "test_program_name", "test_description", activePeriod, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Membership membership = new Membership(1L, user, program, LocalDateTime.now(), 0L, 1L);
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipJpaRepository.findByUserIdAndProgramId(1L, 1L)).thenReturn(Optional.of(membership));
        when(earningRuleJpaRepository.findByProgramIdAndEventType(1L, EarningEventType.PURCHASE)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NoEarningRuleException.class)
                .isThrownBy(() -> pointsService.earnPoints(1L, request))
                .extracting(NoEarningRuleException::getMessage)
                .isEqualTo("No active rule for this event");
    }

    @Test
    void getUserBalances_dataCorrect_balancesReturned() {
        User user = new User(1L, "test_name", "test_lastName", "test@test.com", LocalDateTime.now(), new ArrayList<>());
        Period activePeriod = new Period(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(10));
        LoyaltyProgram program1 = new LoyaltyProgram(1L, "test_program_name_1", "test_desc", activePeriod, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        LoyaltyProgram program2 = new LoyaltyProgram(2L, "test_program_name_2", "test_desc", activePeriod, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        LocalDateTime joinTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        Membership membership1 = new Membership(1L, user, program1, joinTime, 100L, 1L);
        Membership membership2 = new Membership(2L, user, program2, joinTime, 250L, 1L);
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(membershipJpaRepository.findAllByUserId(1L)).thenReturn(List.of(membership1, membership2));

        List<BalanceResponse> result = pointsService.getUserBalances(1L);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, result.size()),
                () -> Assertions.assertEquals(1L, result.getFirst().programId()),
                () -> Assertions.assertEquals("test_program_name_1", result.getFirst().programName()),
                () -> Assertions.assertEquals(100, result.getFirst().pointsBalance()),
                () -> Assertions.assertEquals(joinTime, result.getFirst().joinTime()),
                () -> Assertions.assertEquals(2L, result.get(1).programId()),
                () -> Assertions.assertEquals("test_program_name_2", result.get(1).programName()),
                () -> Assertions.assertEquals(250, result.get(1).pointsBalance()),
                () -> Assertions.assertEquals(joinTime, result.get(1).joinTime())
        );
    }

    @Test
    void getUserHistory_dataCorrect_historyReturned() {
        User user = new User(1L, "test_name", "test_lastName", "test@test.com", LocalDateTime.now(), new ArrayList<>());
        LoyaltyProgram program = new LoyaltyProgram();
        program.setName("test_program_name");
        Membership membership = new Membership();
        membership.setProgram(program);
        PointsTransaction pointsTransaction1 = new PointsTransaction();
        pointsTransaction1.setMembership(membership);
        pointsTransaction1.setDateOfTransaction(LocalDateTime.of(2025, 1, 1, 10, 0));
        pointsTransaction1.setNumberOfPoints(5);
        pointsTransaction1.setType(TransactionType.EARN);
        pointsTransaction1.setDescription("Rule: test_rule, reference number: ref-1");
        PointsTransaction pointsTransaction2 = new PointsTransaction();
        pointsTransaction2.setMembership(membership);
        pointsTransaction2.setDateOfTransaction(LocalDateTime.of(2025, 1, 2, 10, 0));
        pointsTransaction2.setNumberOfPoints(10);
        pointsTransaction2.setType(TransactionType.EARN);
        pointsTransaction2.setDescription("Rule: test_rule2, reference number: ref-2");
        GetPageRequest request = new GetPageRequest(0, 10);
        Page<PointsTransaction> page = new PageImpl<>(List.of(pointsTransaction2, pointsTransaction1), request.toPageable(), 2);
        when(userJpaRepository.findById(1L)).thenReturn(Optional.of(user));
        when(pointsTransactionJpaRepository.findByMembershipUserIdOrderByDateOfTransactionDesc(any(), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<PointsHistoryResponse> response = pointsService.getUserHistory(1L, request);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, response.getContent().size()),
                () -> Assertions.assertEquals(2L, response.getTotalElements()),
                () -> Assertions.assertEquals(1, response.getTotalPages()),
                () -> Assertions.assertEquals("Rule: test_rule2, reference number: ref-2", response.getContent().getFirst().description()),
                () -> Assertions.assertEquals("Rule: test_rule, reference number: ref-1", response.getContent().get(1).description()),
                () -> Assertions.assertEquals(10, response.getContent().getFirst().points()),
                () -> Assertions.assertEquals(5, response.getContent().get(1).points())
        );
    }
}

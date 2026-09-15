package pl.kurs.loyalty.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import pl.kurs.loyalty.dto.request.EarnPointsRequest;
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.response.BalanceResponse;
import pl.kurs.loyalty.dto.response.EarnPointsResponse;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.PointsHistoryResponse;
import pl.kurs.loyalty.exception.*;
import pl.kurs.loyalty.model.*;
import pl.kurs.loyalty.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PointsService {
    private final PointsTransactionJpaRepository pointsTransactionJpaRepository;
    private final LoyaltyProgramJpaRepository loyaltyProgramJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final MembershipJpaRepository membershipJpaRepository;
    private final EarningRuleJpaRepository earningRuleJpaRepository;

    @Transactional
    public EarnPointsResponse earnPoints(Long userId, EarnPointsRequest earnPointsRequest) {
        log.info("Attempting to earn points for user ID: {}. EventType: {}, RuleId: {}, Ref: {}",
                userId, earnPointsRequest.earningEventType(), earnPointsRequest.earningRuleId(), earnPointsRequest.referenceId());

        boolean hasProgramAndEvent = earnPointsRequest.programId() != null && earnPointsRequest.earningEventType() != null;
        boolean hasRuleId = earnPointsRequest.earningRuleId() != null;

        if (hasProgramAndEvent == hasRuleId) {
            throw new EarnPointsRequestInvalidFormatException();
        }

        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Membership membership;
        if (earnPointsRequest.programId() != null) {
            membership = membershipJpaRepository.findByUserIdAndProgramId(userId, earnPointsRequest.programId())
                    .orElseThrow(MembershipNotFoundException::new);
        } else {
            List<Membership> userMemberships = user.getMemberships();
            if (userMemberships.isEmpty()) {
                throw new MembershipNotFoundException();
            } else if (userMemberships.size() > 1) {
                log.warn("Failed to resolve membership for user ID: {}. User belongs to multiple programs, but no programId was provided.", userId);
                throw new AmbiguousMembershipException("User has multiple memberships");
            }
            membership = userMemberships.getFirst();
        }

        LoyaltyProgram loyaltyProgram = membership.getProgram();

        if (!loyaltyProgram.getValidityPeriod().isActiveAt(LocalDateTime.now())) {
            throw new ProgramExpiredException();
        }

        EarningRule earningRule;
        if (hasRuleId) {
            earningRule = earningRuleJpaRepository.findById(earnPointsRequest.earningRuleId())
                    .filter(r -> r.getProgram().getId().equals(loyaltyProgram.getId()))
                    .orElseThrow(NoEarningRuleException::new);
        } else {
            earningRule = earningRuleJpaRepository.findByProgramIdAndEventType(earnPointsRequest.programId(), earnPointsRequest.earningEventType())
                    .orElseThrow(NoEarningRuleException::new);
        }

        if (!earningRule.isStatus() || !earningRule.getValidityPeriod().isActiveAt(LocalDateTime.now())) {
            throw new NoEarningRuleException();
        }

        Long newBalance = membership.getPointsBalance() + earningRule.getNumberOfPoints();
        membership.setPointsBalance(newBalance);

        String description = "Rule: " + earningRule.getName() + ", reference number: " + earnPointsRequest.referenceId();

        PointsTransaction pointsTransaction = new PointsTransaction();
        pointsTransaction.setType(TransactionType.EARN);
        pointsTransaction.setNumberOfPoints(earningRule.getNumberOfPoints());
        pointsTransaction.setDescription(description);
        pointsTransaction.setBalanceAfterTransaction(newBalance.intValue());
        pointsTransaction.setMembership(membership);

        PointsTransaction saved = pointsTransactionJpaRepository.save(pointsTransaction);

        log.info("User ID: {} earned {} points in program ID: {} for '{}'. Ref: {}",
                userId, earningRule.getNumberOfPoints(), loyaltyProgram.getId(), earningRule.getName(), earnPointsRequest.referenceId());


        return new EarnPointsResponse(
                saved.getId(),
                saved.getNumberOfPoints(),
                newBalance.intValue(),
                description
        );
    }

    public List<BalanceResponse> getUserBalances(Long userId) {
        log.info("Fetching points balances for user ID: {}", userId);

        userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<Membership> memberships = membershipJpaRepository.findAllByUserId(userId);

        return memberships.stream()
                .map(membership -> new BalanceResponse(
                        membership.getProgram().getId(),
                        membership.getProgram().getName(),
                        membership.getPointsBalance().intValue(),
                        membership.getJoinTime()
                ))
                .toList();
    }

    public PageResponse<PointsHistoryResponse> getUserHistory(Long userId, GetPageRequest getPageRequest) {
        log.info("Fetching simple points history for user ID: {}", userId);

        userJpaRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Page<PointsTransaction> page = pointsTransactionJpaRepository
                .findByMembershipUserIdOrderByDateOfTransactionDesc(userId, getPageRequest.toPageable());

        Page<PointsHistoryResponse> mappedPage = page.map(pointsTransaction -> new PointsHistoryResponse(
                pointsTransaction.getMembership().getProgram().getName(),
                pointsTransaction.getDateOfTransaction(),
                pointsTransaction.getNumberOfPoints(),
                pointsTransaction.getType(),
                pointsTransaction.getDescription()
        ));

        return new PageResponse<>(mappedPage);
    }
}
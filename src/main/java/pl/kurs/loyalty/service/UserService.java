package pl.kurs.loyalty.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import pl.kurs.loyalty.dto.request.CreateUserRequest;
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.request.UpdateUserRequest;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.ProgramSummaryResponse;
import pl.kurs.loyalty.dto.response.UserResponse;
import pl.kurs.loyalty.exception.*;
import pl.kurs.loyalty.mapper.UserMapper;
import pl.kurs.loyalty.model.LoyaltyProgram;
import pl.kurs.loyalty.model.Membership;
import pl.kurs.loyalty.model.User;
import pl.kurs.loyalty.repository.LoyaltyProgramJpaRepository;
import pl.kurs.loyalty.repository.MembershipJpaRepository;
import pl.kurs.loyalty.repository.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserJpaRepository userJpaRepository;
    private final LoyaltyProgramJpaRepository loyaltyProgramJpaRepository;
    private final MembershipJpaRepository membershipJpaRepository;
    private final UserMapper userMapper;

    public PageResponse<UserResponse> getAllUsers(GetPageRequest getPageRequest) {
        Page<UserResponse> page = userJpaRepository.findAll(getPageRequest.toPageable())
                .map(userMapper::mapToResponse);
        return new PageResponse<>(page);
    }

    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return userMapper.mapToResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        try {
            User user = userMapper.mapToEntity(createUserRequest);
            User saved = userJpaRepository.save(user);

            if (createUserRequest.programId() != null) {
                LoyaltyProgram program = loyaltyProgramJpaRepository.findById(createUserRequest.programId())
                        .orElseThrow(() -> new ProgramNotFoundException(createUserRequest.programId()));

                if (!program.getValidityPeriod().isActiveAt(LocalDateTime.now())) {
                    throw new ProgramExpiredException();
                }

                Membership membership = new Membership();
                membership.setPointsBalance(0L);
                saved.addMembership(membership);
                program.addMembership(membership);
                membershipJpaRepository.save(membership);
            }

            return userMapper.mapToResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new UserAlreadyExistsException(createUserRequest.email());
        }
    }

    public void updateUser(UpdateUserRequest updateUserRequest, Long id) {
        User user = findUserById(id);
        user.update(updateUserRequest);
        userJpaRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);
        userJpaRepository.delete(user);
    }

    public List<ProgramSummaryResponse> getAllUserPrograms(Long id) {
        User user = findUserById(id);
        return user.getMemberships().stream()
                .map(userMapper::toProgramSummary)
                .toList();
    }

    public void assignProgram(Long userId, Long programId) {
        User user = findUserById(userId);
        LoyaltyProgram program = loyaltyProgramJpaRepository.findById(programId)
                .orElseThrow(() -> new ProgramNotFoundException(programId));

        if (!program.getValidityPeriod().isActiveAt(LocalDateTime.now())) {
            throw new ProgramExpiredException();
        }

        if (membershipJpaRepository.existsByUserIdAndProgramId(userId, programId)) {
            throw new MembershipAlreadyExistsException();
        }

        Membership membership = new Membership();
        membership.setPointsBalance(0L);
        user.addMembership(membership);
        program.addMembership(membership);
        membershipJpaRepository.save(membership);
    }

    public void unassignProgram(Long userId, Long programId) {
        Membership membership = membershipJpaRepository.findByUserIdAndProgramId(userId, programId)
                .orElseThrow(MembershipNotFoundException::new);

        if (membership.getPointsBalance() > 0) {
            throw new PositiveBalanceException();
        }
        User user = findUserById(userId);
        LoyaltyProgram program = loyaltyProgramJpaRepository.findById(programId)
                .orElseThrow(() -> new ProgramNotFoundException(programId));

        user.removeMembership(membership);
        program.removeMembership(membership);
        membershipJpaRepository.delete(membership);
    }

    private User findUserById(Long id) {
        return userJpaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}

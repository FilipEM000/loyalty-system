package pl.kurs.loyalty.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import pl.kurs.loyalty.dto.request.CreateUserRequest;
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.request.UpdateUserRequest;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.ProgramSummaryResponse;
import pl.kurs.loyalty.dto.response.UserResponse;
import pl.kurs.loyalty.exception.MembershipAlreadyExistsException;
import pl.kurs.loyalty.exception.PositiveBalanceException;
import pl.kurs.loyalty.exception.ProgramExpiredException;
import pl.kurs.loyalty.mapper.UserMapper;
import pl.kurs.loyalty.model.LoyaltyProgram;
import pl.kurs.loyalty.model.Membership;
import pl.kurs.loyalty.model.Period;
import pl.kurs.loyalty.model.User;
import pl.kurs.loyalty.repository.LoyaltyProgramJpaRepository;
import pl.kurs.loyalty.repository.MembershipJpaRepository;
import pl.kurs.loyalty.repository.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    UserService userService;
    UserJpaRepository userJpaRepository;
    LoyaltyProgramJpaRepository loyaltyProgramJpaRepository;
    MembershipJpaRepository membershipJpaRepository;
    UserMapper userMapper;

    @BeforeEach
    void setup() {
        this.userJpaRepository = Mockito.mock(UserJpaRepository.class);
        this.loyaltyProgramJpaRepository = Mockito.mock(LoyaltyProgramJpaRepository.class);
        this.membershipJpaRepository = Mockito.mock(MembershipJpaRepository.class);
        this.userMapper = Mappers.getMapper(UserMapper.class);
        this.userService = new UserService(userJpaRepository, loyaltyProgramJpaRepository, membershipJpaRepository, userMapper);
    }

    @Test
    void getAllUsers_dataCorrect_usersReturned() {
        GetPageRequest getPageRequest = new GetPageRequest(0, 10);
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), List.of());
        User user2 = new User(1L, "test_name_2", "test_lastName_2", "test_email_2", LocalDateTime.of(2025, 8, 5, 20, 0), List.of());
        Page<User> users = new PageImpl<>(List.of(user, user2));
        when(userJpaRepository.findAll(getPageRequest.toPageable())).thenReturn(users);

        PageResponse<UserResponse> result = userService.getAllUsers(getPageRequest);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, result.getTotalElements()),
                () -> Assertions.assertEquals(0L, result.getContent().getFirst().id()),
                () -> Assertions.assertEquals("test_name", result.getContent().getFirst().name()),
                () -> Assertions.assertEquals("test_lastName", result.getContent().getFirst().lastName()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 10, 5, 15, 15), result.getContent().getFirst().registrationDate()),
                () -> Assertions.assertEquals(0, result.getContent().get(1).programs().size()),
                () -> Assertions.assertEquals(1L, result.getContent().get(1).id()),
                () -> Assertions.assertEquals("test_name_2", result.getContent().get(1).name()),
                () -> Assertions.assertEquals("test_lastName_2", result.getContent().get(1).lastName()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 8, 5, 20, 0), result.getContent().get(1).registrationDate()),
                () -> Assertions.assertEquals(0, result.getContent().get(1).programs().size())
        );
    }

    @Test
    void getUserById_dataCorrect_userReturned() {
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), List.of());
        when(userJpaRepository.findById(0L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(0L);

        Assertions.assertAll(
                () -> Assertions.assertEquals(0L, result.id()),
                () -> Assertions.assertEquals("test_name", result.name()),
                () -> Assertions.assertEquals("test_lastName", result.lastName()),
                () -> Assertions.assertEquals("test_email", result.email()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 10, 5, 15, 15), result.registrationDate()),
                () -> Assertions.assertEquals(0, result.programs().size())
        );
    }

    @Test
    void createUser_dataCorrectWithoutProgramId_userCreated() {
        CreateUserRequest createUserRequest = new CreateUserRequest("test_name", "test_lastName", "test_email", null);
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), List.of());
        when(userJpaRepository.save(any())).thenReturn(user);

        UserResponse result = userService.createUser(createUserRequest);

        Assertions.assertAll(
                () -> Assertions.assertEquals(0L, result.id()),
                () -> Assertions.assertEquals("test_name", result.name()),
                () -> Assertions.assertEquals("test_lastName", result.lastName()),
                () -> Assertions.assertEquals("test_email", result.email()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 10, 5, 15, 15), result.registrationDate()),
                () -> Assertions.assertEquals(0, result.programs().size())
        );
    }

    @Test
    void createUser_dataCorrectWithProgramId_userCreated() {
        CreateUserRequest createUserRequest = new CreateUserRequest("test_name", "test_lastName", "test_email", 0L);
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), new ArrayList<>(List.of()));
        Period period = new Period();
        period.setStartDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        LoyaltyProgram loyaltyProgram = new LoyaltyProgram(0L, "test_loyalty_name", "test_description", period, true, new ArrayList<>(List.of()), new ArrayList<>(List.of()), new ArrayList<>(List.of()));
        when(userJpaRepository.save(any())).thenReturn(user);
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(loyaltyProgram));

        UserResponse result = userService.createUser(createUserRequest);

        Assertions.assertAll(
                () -> Assertions.assertEquals(0L, result.id()),
                () -> Assertions.assertEquals("test_name", result.name()),
                () -> Assertions.assertEquals("test_lastName", result.lastName()),
                () -> Assertions.assertEquals("test_email", result.email()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 10, 5, 15, 15), result.registrationDate()),
                () -> Assertions.assertEquals(1, result.programs().size()),
                () -> Assertions.assertEquals(0L, result.programs().getFirst().id()),
                () -> Assertions.assertEquals("test_loyalty_name", result.programs().getFirst().name()),
                () -> verify(membershipJpaRepository).save(any(Membership.class))
        );
    }

    @Test
    void createUser_programExpired_throwsProgramExpiredException() {
        CreateUserRequest createUserRequest = new CreateUserRequest("test_name", "test_lastName", "test_email", 0L);
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), new ArrayList<>(List.of()));
        Period period = new Period();
        period.setStartDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        period.setEndDate(LocalDateTime.of(2026, 9, 3, 0, 0));
        LoyaltyProgram loyaltyProgram = new LoyaltyProgram(0L, "test_loyalty_name", "test_description", period, true, new ArrayList<>(List.of()), new ArrayList<>(List.of()), new ArrayList<>(List.of()));
        when(userJpaRepository.save(any())).thenReturn(user);
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(loyaltyProgram));

        assertThatExceptionOfType(ProgramExpiredException.class)
                .isThrownBy(() -> userService.createUser(createUserRequest))
                .extracting(ProgramExpiredException::getMessage)
                .isEqualTo("Loyalty program already expired");
        verify(membershipJpaRepository, never()).save(any());
    }

    @Test
    void updateUser_dataCorrect_userUpdated() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("new_name", "new_lastName", "new_email");
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), List.of());
        when(userJpaRepository.findById(any())).thenReturn(Optional.of(user));

        userService.updateUser(updateUserRequest, 0L);

        Assertions.assertAll(
                () -> Assertions.assertEquals("new_name", user.getName()),
                () -> Assertions.assertEquals("new_lastName", user.getLastName()),
                () -> Assertions.assertEquals("new_email", user.getEmail()),
                () -> verify(userJpaRepository).findById(0L),
                () -> verify(userJpaRepository).save(user)
        );
    }

    @Test
    void deleteUser_dataCorrect_userDeleted() {
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), List.of());
        when(userJpaRepository.findById(any())).thenReturn(Optional.of(user));

        userService.deleteUser(0L);

        Assertions.assertAll(
                () -> verify(userJpaRepository).findById(0L),
                () -> verify(userJpaRepository).delete(user)
        );
    }

    @Test
    void getAllUserPrograms_dataCorrect_programReturned() {
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), new ArrayList<>(List.of()));
        Period period = new Period();
        period.setStartDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        LoyaltyProgram loyaltyProgram = new LoyaltyProgram(0L, "test_loyalty_name", "test_description", period, true, new ArrayList<>(List.of()), new ArrayList<>(List.of()), new ArrayList<>(List.of()));
        LoyaltyProgram loyaltyProgram2 = new LoyaltyProgram(1L, "test_loyalty_name_2", "test_description_2", period, true, new ArrayList<>(List.of()), new ArrayList<>(List.of()), new ArrayList<>(List.of()));
        Membership membership = new Membership(0L, null, null, LocalDateTime.of(2026, 9, 5, 20, 0), 0L, 0L);
        Membership membership2 = new Membership(0L, null, null, LocalDateTime.of(2026, 8, 17, 10, 0), 0L, 1L);
        user.addMembership(membership);
        loyaltyProgram.addMembership(membership);
        user.addMembership(membership2);
        loyaltyProgram2.addMembership(membership2);
        when(userJpaRepository.findById(0L)).thenReturn(Optional.of(user));

        List<ProgramSummaryResponse> result = userService.getAllUserPrograms(0L);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, result.size()),
                () -> Assertions.assertEquals(0L, result.getFirst().id()),
                () -> Assertions.assertEquals("test_loyalty_name", result.getFirst().name()),
                () -> Assertions.assertEquals(1L, result.get(1).id()),
                () -> Assertions.assertEquals("test_loyalty_name_2", result.get(1).name())
        );
    }

    @Test
    void assignProgram_dataCorrect_programAssigned() {
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), new ArrayList<>(List.of()));
        Period period = new Period();
        period.setStartDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        LoyaltyProgram loyaltyProgram = new LoyaltyProgram(0L, "test_loyalty_name", "test_description", period, true, new ArrayList<>(List.of()), new ArrayList<>(List.of()), new ArrayList<>(List.of()));
        when(userJpaRepository.findById(0L)).thenReturn(Optional.of(user));
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(loyaltyProgram));

        userService.assignProgram(0L, 0L);

        Assertions.assertAll(
                () -> verify(loyaltyProgramJpaRepository).findById(0L),
                () -> verify(membershipJpaRepository).save(any(Membership.class)),
                () -> verify(userJpaRepository).findById(0L),
                () -> Assertions.assertEquals(1, user.getMemberships().size()),
                () -> Assertions.assertEquals(0L, user.getMemberships().getFirst().getProgram().getId()),
                () -> Assertions.assertEquals("test_loyalty_name", user.getMemberships().getFirst().getProgram().getName()),
                () -> Assertions.assertEquals("test_description", user.getMemberships().getFirst().getProgram().getDescription()),
                () -> Assertions.assertEquals(period, user.getMemberships().getFirst().getProgram().getValidityPeriod()),
                () -> Assertions.assertTrue(user.getMemberships().getFirst().getProgram().isStatus()),
                () -> Assertions.assertEquals(1, user.getMemberships().getFirst().getProgram().getMembers().size()),
                () -> Assertions.assertEquals(0, user.getMemberships().getFirst().getProgram().getRewards().size()),
                () -> Assertions.assertEquals(0, user.getMemberships().getFirst().getProgram().getEarningRules().size())
        );
    }

    @Test
    void assignProgram_programExpired_throwsProgramExpiredException() {
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), new ArrayList<>(List.of()));
        Period period = new Period();
        period.setStartDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        period.setEndDate(LocalDateTime.of(2026, 9, 3, 0, 0));
        LoyaltyProgram loyaltyProgram = new LoyaltyProgram(0L, "test_loyalty_name", "test_description", period, true, new ArrayList<>(List.of()), new ArrayList<>(List.of()), new ArrayList<>(List.of()));
        when(userJpaRepository.findById(0L)).thenReturn(Optional.of(user));
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(loyaltyProgram));

        assertThatExceptionOfType(ProgramExpiredException.class)
                .isThrownBy(() -> userService.assignProgram(0L, 0L))
                .extracting(ProgramExpiredException::getMessage)
                .isEqualTo("Loyalty program already expired");
        verify(membershipJpaRepository, never()).save(any());
    }

    @Test
    void assignProgram_membershipAlreadyExists_throwsMembershipAlreadyExistsException() {
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), new ArrayList<>(List.of()));
        Period period = new Period();
        period.setStartDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        LoyaltyProgram loyaltyProgram = new LoyaltyProgram(0L, "test_loyalty_name", "test_description", period, true, new ArrayList<>(List.of()), new ArrayList<>(List.of()), new ArrayList<>(List.of()));
        when(userJpaRepository.findById(0L)).thenReturn(Optional.of(user));
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(loyaltyProgram));
        when(membershipJpaRepository.existsByUserIdAndProgramId(0L, 0L)).thenReturn(true);

        assertThatExceptionOfType(MembershipAlreadyExistsException.class)
                .isThrownBy(() -> userService.assignProgram(0L, 0L))
                .extracting(MembershipAlreadyExistsException::getMessage)
                .isEqualTo("Membership already exists");
        verify(membershipJpaRepository, never()).save(any());
    }

    @Test
    void unassignProgram_dataCorrect_programUnassigned() {
        User user = new User(0L, "test_name", "test_lastName", "test_email", LocalDateTime.of(2025, 10, 5, 15, 15), new ArrayList<>(List.of()));
        Period period = new Period();
        period.setStartDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        LoyaltyProgram loyaltyProgram = new LoyaltyProgram(0L, "test_loyalty_name", "test_description", period, true, new ArrayList<>(List.of()), new ArrayList<>(List.of()), new ArrayList<>(List.of()));
        Membership membership = new Membership(0L, user, loyaltyProgram, LocalDateTime.of(2025, 10, 10, 20, 0), 0L, 1L);
        when(userJpaRepository.findById(0L)).thenReturn(Optional.of(user));
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(loyaltyProgram));
        when(membershipJpaRepository.findByUserIdAndProgramId(0L, 0L)).thenReturn(Optional.of(membership));

        userService.unassignProgram(0L, 0L);

        Assertions.assertAll(
                () -> verify(loyaltyProgramJpaRepository).findById(0L),
                () -> verify(membershipJpaRepository).delete(any(Membership.class)),
                () -> verify(userJpaRepository).findById(0L),
                () -> Assertions.assertEquals(0, user.getMemberships().size())
        );
    }

    @Test
    void unassignProgram_balanceGreaterThanZero_throwsPositiveBalanceException() {
        Membership membership = new Membership();
        membership.setPointsBalance(50L);
        when(membershipJpaRepository.findByUserIdAndProgramId(0L, 0L))
                .thenReturn(Optional.of(membership));

        assertThatExceptionOfType(PositiveBalanceException.class)
                .isThrownBy(() -> userService.unassignProgram(0L, 0L))
                .extracting(PositiveBalanceException::getMessage)
                .isEqualTo("Cannot quit membership with positive balance");
        verify(membershipJpaRepository, never()).delete(any());
    }
}

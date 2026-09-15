package pl.kurs.loyalty.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.request.create.CreateLoyaltyProgramRequest;
import pl.kurs.loyalty.dto.request.update.UpdateLoyaltyProgramRequest;
import pl.kurs.loyalty.dto.response.LoyaltyProgramResponse;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.UserResponse;
import pl.kurs.loyalty.exception.ProgramAlreadyExistsException;
import pl.kurs.loyalty.exception.ProgramHasActiveMembersException;
import pl.kurs.loyalty.mapper.LoyaltyProgramMapper;
import pl.kurs.loyalty.mapper.UserMapper;
import pl.kurs.loyalty.model.LoyaltyProgram;
import pl.kurs.loyalty.model.Membership;
import pl.kurs.loyalty.model.Period;
import pl.kurs.loyalty.model.User;
import pl.kurs.loyalty.repository.LoyaltyProgramJpaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LoyaltyProgramServiceTest {
    LoyaltyProgramService loyaltyProgramService;
    LoyaltyProgramJpaRepository loyaltyProgramJpaRepository;
    LoyaltyProgramMapper loyaltyProgramMapper;
    UserMapper userMapper;

    @BeforeEach
    void setup() {
        this.loyaltyProgramJpaRepository = Mockito.mock(LoyaltyProgramJpaRepository.class);
        this.loyaltyProgramMapper = Mappers.getMapper(LoyaltyProgramMapper.class);
        this.userMapper = Mappers.getMapper(UserMapper.class);
        this.loyaltyProgramService = new LoyaltyProgramService(loyaltyProgramJpaRepository, loyaltyProgramMapper, userMapper);
    }

    @Test
    void getAllPrograms_dataCorrect_programsReturned() {
        GetPageRequest getPageRequest = new GetPageRequest(0, 10);
        Period period = new Period(LocalDateTime.of(2025, 1, 1, 0, 0), null);
        LoyaltyProgram program1 = new LoyaltyProgram(0L, "test_name", "test_description", period, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        LoyaltyProgram program2 = new LoyaltyProgram(1L, "test_name2", "test_description2", period, false, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Page<LoyaltyProgram> programs = new PageImpl<>(List.of(program1, program2));
        when(loyaltyProgramJpaRepository.findAll(getPageRequest.toPageable())).thenReturn(programs);

        PageResponse<LoyaltyProgramResponse> result = loyaltyProgramService.getAllPrograms(getPageRequest);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, result.getTotalElements()),
                () -> Assertions.assertEquals(0L, result.getContent().getFirst().id()),
                () -> Assertions.assertEquals("test_name", result.getContent().getFirst().name()),
                () -> Assertions.assertEquals("test_description", result.getContent().getFirst().description()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), result.getContent().getFirst().startDate()),
                () -> Assertions.assertNull(result.getContent().getFirst().endDate()),
                () -> Assertions.assertTrue(result.getContent().getFirst().isActive()),
                () -> Assertions.assertEquals(1L, result.getContent().get(1).id()),
                () -> Assertions.assertEquals("test_name2", result.getContent().get(1).name()),
                () -> Assertions.assertEquals("test_description2", result.getContent().get(1).description()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), result.getContent().get(1).startDate()),
                () -> Assertions.assertNull(result.getContent().get(1).endDate()),
                () -> Assertions.assertFalse(result.getContent().get(1).isActive())
        );
    }

    @Test
    void getProgramById_dataCorrect_programReturned() {
        Period period = new Period(LocalDateTime.of(2025, 1, 1, 0, 0), null);
        LoyaltyProgram program = new LoyaltyProgram(5L, "test_name", "test_description", period, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        when(loyaltyProgramJpaRepository.findById(5L)).thenReturn(Optional.of(program));

        LoyaltyProgramResponse result = loyaltyProgramService.getProgramById(5L);

        Assertions.assertAll(
                () -> Assertions.assertEquals(5L, result.id()),
                () -> Assertions.assertEquals("test_name", result.name()),
                () -> Assertions.assertEquals("test_description", result.description()),
                () -> Assertions.assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), result.startDate()),
                () -> Assertions.assertNull(result.endDate()),
                () -> Assertions.assertTrue(result.isActive())
        );
    }

    @Test
    void createProgram_dataCorrect_programCreated() {
        CreateLoyaltyProgramRequest request = new CreateLoyaltyProgramRequest("test_name", "test_description", LocalDateTime.of(2026, 1, 1, 0, 0), null);
        Period period = new Period(LocalDateTime.of(2026, 1, 1, 0, 0), null);
        LoyaltyProgram savedProgram = new LoyaltyProgram(10L, "test_name", "test_description", period, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        when(loyaltyProgramJpaRepository.save(any(LoyaltyProgram.class))).thenReturn(savedProgram);

        LoyaltyProgramResponse result = loyaltyProgramService.createProgram(request);

        Assertions.assertAll(
                () -> Assertions.assertEquals(10L, result.id()),
                () -> Assertions.assertEquals("test_name", result.name()),
                () -> Assertions.assertEquals("test_description", result.description()),
                () -> Assertions.assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.startDate()),
                () -> Assertions.assertNull(result.endDate()),
                () -> Assertions.assertTrue(result.isActive()),
                () -> verify(loyaltyProgramJpaRepository).save(any(LoyaltyProgram.class))
        );
    }

    @Test
    void createProgram_duplicateName_throwsProgramAlreadyExistsException() {
        CreateLoyaltyProgramRequest request = new CreateLoyaltyProgramRequest("test_name", "test_description", LocalDateTime.of(2026, 1, 1, 0, 0), null);
        when(loyaltyProgramJpaRepository.save(any(LoyaltyProgram.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate name"));

        assertThatExceptionOfType(ProgramAlreadyExistsException.class)
                .isThrownBy(() -> loyaltyProgramService.createProgram(request));
    }

    @Test
    void updateProgram_dataCorrect_programUpdated() {
        UpdateLoyaltyProgramRequest request = new UpdateLoyaltyProgramRequest("new_name", "new_description", LocalDateTime.of(2027, 1, 1, 0, 0), null);
        Period period = new Period(LocalDateTime.of(2025, 1, 1, 0, 0), null);
        LoyaltyProgram program = new LoyaltyProgram(0L, "test_name", "test_description", period, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(program));

        loyaltyProgramService.updateProgram(0L, request);

        Assertions.assertAll(
                () -> Assertions.assertEquals("new_name", program.getName()),
                () -> Assertions.assertEquals("new_description", program.getDescription()),
                () -> Assertions.assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0), program.getValidityPeriod().getStartDate()),
                () -> verify(loyaltyProgramJpaRepository).save(program)
        );
    }

    @Test
    void deleteProgram_noMembers_programDeleted() {
        Period period = new Period(LocalDateTime.of(2025, 1, 1, 0, 0), null);
        LoyaltyProgram program = new LoyaltyProgram(0L, "test_name", "test_description", period, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(program));

        loyaltyProgramService.deleteProgram(0L);
        verify(loyaltyProgramJpaRepository).delete(program);
    }

    @Test
    void deleteProgram_hasMembers_throwsProgramHasActiveMembersException() {
        Period period = new Period(LocalDateTime.of(2025, 1, 1, 0, 0), null);
        LoyaltyProgram program = new LoyaltyProgram(0L, "test_name", "test_description", period, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        program.addMembership(new Membership());
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(program));

        assertThatExceptionOfType(ProgramHasActiveMembersException.class)
                .isThrownBy(() -> loyaltyProgramService.deleteProgram(0L));

        verify(loyaltyProgramJpaRepository, never()).delete(any());
    }

    @Test
    void getProgramUsers_dataCorrect_usersReturned() {
        Period period = new Period(LocalDateTime.of(2025, 1, 1, 0, 0), null);
        LoyaltyProgram program = new LoyaltyProgram(0L, "Program", "Desc", period, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        User user1 = new User(10L, "test_name", "test_lastName", "test_email", LocalDateTime.now(), new ArrayList<>());
        User user2 = new User(20L, "test_name2", "test_lastName2", "test_email2", LocalDateTime.now(), new ArrayList<>());

        Membership membership1 = new Membership(); membership1.setUser(user1);
        Membership membership2 = new Membership(); membership2.setUser(user2);
        program.addMembership(membership1);
        program.addMembership(membership2);
        when(loyaltyProgramJpaRepository.findById(0L)).thenReturn(Optional.of(program));
        List<UserResponse> result = loyaltyProgramService.getProgramUsers(0L);
        Assertions.assertAll(
                () -> Assertions.assertEquals(2, result.size()),
                () -> Assertions.assertEquals(10L, result.getFirst().id()),
                () -> Assertions.assertEquals("test_name", result.getFirst().name()),
                () -> Assertions.assertEquals("test_lastName", result.getFirst().lastName()),
                () -> Assertions.assertEquals("test_email", result.getFirst().email()),
                () -> Assertions.assertEquals(20L, result.get(1).id()),
                () -> Assertions.assertEquals("test_name2", result.get(1).name()),
                () -> Assertions.assertEquals("test_lastName2", result.get(1).lastName()),
                () -> Assertions.assertEquals("test_email2", result.get(1).email())

                );
    }
}

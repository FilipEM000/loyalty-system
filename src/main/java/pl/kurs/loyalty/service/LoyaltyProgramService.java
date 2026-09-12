package pl.kurs.loyalty.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import pl.kurs.loyalty.dto.request.create.CreateLoyaltyProgramRequest;
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.request.update.UpdateLoyaltyProgramRequest;
import pl.kurs.loyalty.dto.response.LoyaltyProgramResponse;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.UserResponse;
import pl.kurs.loyalty.exception.ProgramAlreadyExistsException;
import pl.kurs.loyalty.exception.ProgramHasActiveMembersException;
import pl.kurs.loyalty.exception.ProgramNotFoundException;
import pl.kurs.loyalty.mapper.LoyaltyProgramMapper;
import pl.kurs.loyalty.mapper.UserMapper;
import pl.kurs.loyalty.model.LoyaltyProgram;
import pl.kurs.loyalty.model.Membership;
import pl.kurs.loyalty.repository.LoyaltyProgramJpaRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoyaltyProgramService {
    private final LoyaltyProgramJpaRepository loyaltyProgramJpaRepository;
    private final LoyaltyProgramMapper loyaltyProgramMapper;
    private final UserMapper userMapper;

    public PageResponse<LoyaltyProgramResponse> getAllPrograms(GetPageRequest getPageRequest) {
        Page<LoyaltyProgramResponse> page = loyaltyProgramJpaRepository.findAll(getPageRequest.toPageable())
                .map(loyaltyProgramMapper::toResponse);
        return new PageResponse<>(page);
    }

    public LoyaltyProgramResponse getProgramById(Long id) {
        LoyaltyProgram loyaltyProgram = findProgramById(id);
        return loyaltyProgramMapper.toResponse(loyaltyProgram);
    }

    @Transactional
    public LoyaltyProgramResponse createProgram(CreateLoyaltyProgramRequest createLoyaltyProgramRequest) {
        log.info("Attempting to create new program with name: {}", createLoyaltyProgramRequest.name());
        try {
            LoyaltyProgram loyaltyProgram = loyaltyProgramMapper.toEntity(createLoyaltyProgramRequest);
            LoyaltyProgram saved = loyaltyProgramJpaRepository.save(loyaltyProgram);
            log.info("Successfully created program with ID: {}", saved.getId());
            return loyaltyProgramMapper.toResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new ProgramAlreadyExistsException(createLoyaltyProgramRequest.name());
        }
    }

    public void updateProgram(Long id, UpdateLoyaltyProgramRequest updateLoyaltyProgramRequest) {
        log.info("Attempting to update program with ID: {}", id);
        LoyaltyProgram loyaltyProgram = findProgramById(id);
        loyaltyProgram.update(updateLoyaltyProgramRequest);
        loyaltyProgramJpaRepository.save(loyaltyProgram);
        log.info("Successfully updated program with ID: {}", id);
    }

    public void deleteProgram(Long id) {
        log.info("Attempting to delete program with ID: {}", id);
        LoyaltyProgram loyaltyProgram = findProgramById(id);
        if (!loyaltyProgram.getMembers().isEmpty()) {
            throw new ProgramHasActiveMembersException();
        }
        loyaltyProgramJpaRepository.delete(loyaltyProgram);
        log.info("Successfully deleted program with ID: {}", id);
    }

    public List<UserResponse> getProgramUsers(Long id) {
        LoyaltyProgram loyaltyProgram = findProgramById(id);
        return loyaltyProgram.getMembers().stream()
                .map(Membership::getUser)
                .map(userMapper::mapToResponse)
                .toList();
    }

    private LoyaltyProgram findProgramById(Long id) {
        return loyaltyProgramJpaRepository.findById(id)
                .orElseThrow(() -> new ProgramNotFoundException(id));
    }
}

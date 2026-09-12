package pl.kurs.loyalty.controller;

import jakarta.validation.Valid;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pl.kurs.loyalty.dto.request.create.CreateLoyaltyProgramRequest;
import pl.kurs.loyalty.dto.request.GetPageRequest;
import pl.kurs.loyalty.dto.request.update.UpdateLoyaltyProgramRequest;
import pl.kurs.loyalty.dto.response.LoyaltyProgramResponse;
import pl.kurs.loyalty.dto.response.PageResponse;
import pl.kurs.loyalty.dto.response.UserResponse;
import pl.kurs.loyalty.service.LoyaltyProgramService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/programs")
@RequiredArgsConstructor
public class LoyaltyProgramController {
    private final LoyaltyProgramService loyaltyProgramService;

    @Description("Get all loyalty programs with pagination")
    @GetMapping
    public PageResponse<LoyaltyProgramResponse> getAll(GetPageRequest getPageRequest) {
        log.info("Received request to get all programs with pagination");
        return loyaltyProgramService.getAllPrograms(getPageRequest);
    }

    @Description("Get loyalty program by ID")
    @GetMapping("/{id}")
    public LoyaltyProgramResponse getById(@PathVariable Long id) {
        log.info("Received request to get program with id {}", id);
        return loyaltyProgramService.getProgramById(id);
    }

    @Description("Get all loyalty program users by program ID")
    @GetMapping("/{id}/users")
    public List<UserResponse> getAllProgramUsers(@PathVariable Long id) {
        log.info("Received request to get all users from program with id {}", id);
        return loyaltyProgramService.getProgramUsers(id);
    }

    @Description("Create loyalty program")
    @PostMapping
    public LoyaltyProgramResponse create(@Valid @RequestBody CreateLoyaltyProgramRequest createLoyaltyProgramRequest) {
        log.info("Received request to create program [{}]", createLoyaltyProgramRequest.name());
        return loyaltyProgramService.createProgram(createLoyaltyProgramRequest);
    }

    @Description("Update loyalty program")
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @Valid @RequestBody UpdateLoyaltyProgramRequest updateLoyaltyProgramRequest) {
        log.info("Received request to update program with id {}", id);
        loyaltyProgramService.updateProgram(id, updateLoyaltyProgramRequest);
    }

    @Description("Delete loyalty program by ID")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Received request to delete program with id {}", id);
        loyaltyProgramService.deleteProgram(id);
    }
}

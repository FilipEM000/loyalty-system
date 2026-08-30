package pl.kurs.loyalty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kurs.loyalty.config.MapperCentralConfig;
import pl.kurs.loyalty.dto.request.CreateUserRequest;
import pl.kurs.loyalty.dto.response.ProgramSummaryResponse;
import pl.kurs.loyalty.dto.response.UserResponse;
import pl.kurs.loyalty.model.Membership;
import pl.kurs.loyalty.model.User;

@Mapper(config = MapperCentralConfig.class)
public interface UserMapper {
    @Mapping(target = "programs", source = "memberships")
    UserResponse mapToResponse(User user);

    @Mapping(target = "id", source = "program.id")
    @Mapping(target = "name", source = "program.name")
    ProgramSummaryResponse toProgramSummary(Membership membership);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "memberships", ignore = true)
    User mapToEntity(CreateUserRequest createUserRequest);
}

package pl.kurs.loyalty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kurs.loyalty.config.MapperCentralConfig;
import pl.kurs.loyalty.dto.request.create.CreateLoyaltyProgramRequest;
import pl.kurs.loyalty.dto.response.LoyaltyProgramResponse;
import pl.kurs.loyalty.model.LoyaltyProgram;

@Mapper(config = MapperCentralConfig.class)
public interface LoyaltyProgramMapper {
    @Mapping(target = "startDate", source = "validityPeriod.startDate")
    @Mapping(target = "endDate", source = "validityPeriod.endDate")
    @Mapping(target = "isActive", source = "status")
    LoyaltyProgramResponse toResponse(LoyaltyProgram loyaltyProgram);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "earningRules", ignore = true)
    @Mapping(target = "rewards", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "validityPeriod.startDate", source = "startDate")
    @Mapping(target = "validityPeriod.endDate", source = "endDate")
    LoyaltyProgram toEntity(CreateLoyaltyProgramRequest createLoyaltyProgramRequest);
}

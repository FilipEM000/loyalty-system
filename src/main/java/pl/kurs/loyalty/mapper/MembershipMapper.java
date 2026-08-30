package pl.kurs.loyalty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kurs.loyalty.config.MapperCentralConfig;
import pl.kurs.loyalty.dto.response.BalanceResponse;
import pl.kurs.loyalty.model.Membership;

@Mapper(config = MapperCentralConfig.class)
public interface MembershipMapper {
    @Mapping(target = "programId", source = "program.id")
    @Mapping(target = "programName", source = "program.name")
    BalanceResponse mapToBalanceResponse(Membership membership);
}

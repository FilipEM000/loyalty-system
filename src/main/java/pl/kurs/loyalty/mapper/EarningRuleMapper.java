package pl.kurs.loyalty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kurs.loyalty.config.MapperCentralConfig;
import pl.kurs.loyalty.dto.request.create.CreateEarningRuleRequest;
import pl.kurs.loyalty.dto.response.EarningRuleResponse;
import pl.kurs.loyalty.model.EarningRule;

@Mapper(config = MapperCentralConfig.class)
public interface EarningRuleMapper {
    @Mapping(target = "startDate", source = "validityPeriod.startDate")
    @Mapping(target = "endDate", source = "validityPeriod.endDate")
    @Mapping(target = "isActive", source = "status")
    EarningRuleResponse toResponse(EarningRule earningRule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "program", ignore = true)
    @Mapping(target = "validityPeriod.startDate", source = "startDate")
    @Mapping(target = "validityPeriod.endDate", source = "endDate")
    EarningRule toEntity(CreateEarningRuleRequest createEarningRuleRequest);

}

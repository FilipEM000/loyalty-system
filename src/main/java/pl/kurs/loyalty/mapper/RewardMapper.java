package pl.kurs.loyalty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kurs.loyalty.config.MapperCentralConfig;
import pl.kurs.loyalty.dto.request.create.CreateRewardRequest;
import pl.kurs.loyalty.dto.response.RewardResponse;
import pl.kurs.loyalty.model.Reward;

@Mapper(config = MapperCentralConfig.class)
public interface RewardMapper {
    @Mapping(target = "startDate", source = "validityPeriod.startDate")
    @Mapping(target = "endDate", source = "validityPeriod.endDate")
    RewardResponse toResponse(Reward reward);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "program", ignore = true)
    @Mapping(target = "validityPeriod.startDate", source = "startDate")
    @Mapping(target = "validityPeriod.endDate", source = "endDate")
    Reward toEntity(CreateRewardRequest createRewardRequest);
}

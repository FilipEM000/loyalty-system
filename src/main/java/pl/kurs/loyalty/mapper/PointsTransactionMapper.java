package pl.kurs.loyalty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kurs.loyalty.config.MapperCentralConfig;
import pl.kurs.loyalty.dto.response.PointsHistoryResponse;
import pl.kurs.loyalty.model.PointsTransaction;

@Mapper(config = MapperCentralConfig.class)
public interface PointsTransactionMapper {
    @Mapping(target = "date", source = "dateOfTransaction")
    @Mapping(target = "points", source = "numberOfPoints")
    @Mapping(target = "programName", source = "membership.program.name")
    PointsHistoryResponse mapToResponse(PointsTransaction pointsTransaction);
}

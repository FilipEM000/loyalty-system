package pl.kurs.loyalty.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.IdGeneratorType;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Getter
@Setter
@Embeddable
public class Period {
    private LocalDateTime startDate;
    @Nullable
    @Future
    private LocalDateTime endDate;

    public boolean isActiveAt(LocalDateTime moment) {
        if (endDate == null) {
            return moment.isAfter(startDate) || moment.isEqual(startDate);
        }
        return (moment.isAfter(startDate) || moment.isEqual(startDate)) && moment.isBefore(endDate);
    }
}

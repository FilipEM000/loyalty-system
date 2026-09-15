package pl.kurs.loyalty.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kurs.loyalty.dto.request.update.UpdateEarningRuleRequest;

@Getter
@Setter
@Entity
@Table(name = "earning-rules", uniqueConstraints = @UniqueConstraint(columnNames = {"program_id", "event_type"}))
@NoArgsConstructor
@AllArgsConstructor
public class EarningRule {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private EarningEventType eventType;
    @Min(1)
    private Integer numberOfPoints;
    @Embedded
    private Period validityPeriod;
    private boolean status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private LoyaltyProgram program;

    public void update(UpdateEarningRuleRequest updateEarningRuleRequest) {
        this.name = updateEarningRuleRequest.name();
        this.eventType = updateEarningRuleRequest.eventType();
        this.numberOfPoints = updateEarningRuleRequest.numberOfPoints();
        this.validityPeriod.setStartDate(updateEarningRuleRequest.startDate());
        this.validityPeriod.setEndDate(updateEarningRuleRequest.endDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EarningRule that = (EarningRule) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

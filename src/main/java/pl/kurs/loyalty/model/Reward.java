package pl.kurs.loyalty.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kurs.loyalty.dto.request.update.UpdateRewardRequest;

@Getter
@Setter
@Entity
@Table(name = "rewards")
@NoArgsConstructor
@AllArgsConstructor
public class Reward {
    @Id
    @GeneratedValue
    private Long id;
    @NotBlank
    private String name;
    private String description;
    @Min(1)
    private Integer cost;
    @Embedded
    private Period validityPeriod;
    private Integer availableQuantity;
    private boolean active;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private LoyaltyProgram program;

    public void update(UpdateRewardRequest updateRewardRequest) {
        this.name = updateRewardRequest.name();
        this.description = updateRewardRequest.description();
        this.cost = updateRewardRequest.cost();
        this.availableQuantity = updateRewardRequest.availableQuantity();
        this.validityPeriod.setStartDate(updateRewardRequest.startDate());
        this.validityPeriod.setEndDate(updateRewardRequest.endDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reward reward = (Reward) o;
        return id != null && id.equals(reward.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

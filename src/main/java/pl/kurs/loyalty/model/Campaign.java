package pl.kurs.loyalty.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "campaigns")
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {
    @Id
    @GeneratedValue
    private Long id;
    @NotBlank
    private String name;
    private String description;
    @Embedded
    private Period validityPeriod;
    private Float multiplayer;
    private Integer extraPoints;
    private EarningEventType targetEventType;
    @ManyToOne(fetch = FetchType.LAZY)
    private LoyaltyProgram program;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Campaign campaign = (Campaign) o;
        return id != null && id.equals(campaign.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

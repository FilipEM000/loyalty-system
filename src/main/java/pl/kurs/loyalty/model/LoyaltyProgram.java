package pl.kurs.loyalty.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "loyaltyPrograms")
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyProgram {
    @Id
    @GeneratedValue
    private Long id;
    @NotBlank
    @Column(unique = true)
    private String name;
    private String description;
    @Embedded
    private Period validityPeriod;
    private boolean status;
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL)
    private List<Membership> members;
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL)
    private List<EarningRule> earningRules;
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL)
    private List<Reward> rewards;

    public void addMembership(Membership membership) {
        members.add(membership);
        membership.setProgram(this);
    }

    public void removeMembership(Membership membership) {
        members.remove(membership);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoyaltyProgram that = (LoyaltyProgram) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
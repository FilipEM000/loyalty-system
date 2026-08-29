package pl.kurs.loyalty.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "point_transactions")
@NoArgsConstructor
@AllArgsConstructor
public class PointsTransaction {
    @Id
    @GeneratedValue
    private Long id;
    private TransactionType type;
    private Integer numberOfPoints;
    private String description;
    @CreationTimestamp
    private LocalDateTime dateOfTransaction;
    private Integer balanceAfterTransaction;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id")
    private Membership membership;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointsTransaction that = (PointsTransaction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

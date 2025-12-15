package travel.mytravelplan.domain.checklist.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CheckList extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    protected CheckList(String name, Trip trip) {
        this.name = name;
        this.trip = trip;
    }

    protected void update(String name) {
        this.name = name;
    }
}
